/*
 * Copyright 2017 Philip Strecker
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.puddingspudding.nginj;

import io.github.puddingspudding.fcgi.*;
import io.github.puddingspudding.nginj.http.*;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Created by pudding on 18.02.17.
 */
class SocketChannelConsumer implements Consumer<SocketChannel> {

    private final Map<String, Class> classMap = new ConcurrentHashMap<>();

    private final JavaFileCompiler javaFileCompiler;

    private final Path rootDir;

    private final ByteBuffer inputByteBuffer = ByteBuffer.allocate(1024 * 8);

    private final ByteBuffer outputByteBuffer = ByteBuffer.allocate(1024 * 8);

    private final Logger errorLogger;

    private final Logger traceLogger;

    public SocketChannelConsumer(
        final Path rootDir,
        final JavaFileCompiler javaFileCompiler,
        final Logger errorLgger,
        final Logger traceLogger
    ) {
        this.rootDir = rootDir;
        this.javaFileCompiler = javaFileCompiler;
        this.errorLogger = errorLgger;
        this.traceLogger = traceLogger;

        new Thread(() -> {
            try {
                this.javaFileCompiler.watch(this.classMap);
            } catch (Exception e) {
                errorLgger.catching(e);
            }
        }).start();
    }

    @Override
    public void accept(SocketChannel socketChannel) {
        try {

            Map<String, String> httpHeaders = new HashMap<>();
            Map<String, String> cgiHeaders = new HashMap<>();
            ByteArrayOutputStream data = new ByteArrayOutputStream();

            inputByteBuffer.clear();
            socketChannel.read(inputByteBuffer);
            inputByteBuffer.flip();
            while (true) {


                Header header = FCGI.readHeader(i -> inputByteBuffer).get();
                byte type = header.getType();

                if (type == FCGI.BEGIN_REQUEST) {
                    BeginRequestBody beginRequestBody = FCGI.readBegingRequestBody(i -> inputByteBuffer).get();

                } else if (type == FCGI.PARAMS) {
                    if (header.getContentLength() == 0) {
                        continue;
                    }
                    int start = inputByteBuffer.position();
                    do {
                        NameValuePair nameValuePair = FCGI.readNameValuePair(i -> inputByteBuffer).get();
                        if (nameValuePair.getName().startsWith("HTTP_")) {
                            httpHeaders.put(nameValuePair.getName().replace("HTTP_", ""), nameValuePair.getValue());
                        } else {
                            cgiHeaders.put(nameValuePair.getName(), nameValuePair.getValue());
                        }
                    } while (header.getContentLength() > inputByteBuffer.position() - start);
                } else if (type == FCGI.STDIN) {
                    if (header.getContentLength() == 0) {
                        break;
                    }
                    while (inputByteBuffer.remaining() < header.getContentLength()) {
                        inputByteBuffer.compact();
                        socketChannel.read(inputByteBuffer);
                        inputByteBuffer.flip();
                    }
                    byte[] tmp = new byte[header.getContentLength()];
                    inputByteBuffer.get(tmp);
                    data.write(tmp);
                }
                if (header.getPaddingLength() > 0) {
                    inputByteBuffer.get(new byte[header.getPaddingLength()]);
                }
            }

            HttpRequest request = new HttpRequest()
                .setHeader(httpHeaders)
                .setBody(ByteBuffer.wrap(data.toByteArray()));

            String fileName = this.rootDir + cgiHeaders.get("SCRIPT_NAME");

            Path file = Paths.get(fileName);

            if (!file.toFile().exists()) {
                HttpResponse response = new HttpResponse();
                response.setStatus(404);
                response.setHeader(httpHeaders);
                response.setBody(ByteBuffer.wrap("Not Found".getBytes()));
                this.writeToSocket(response, socketChannel);
                return;
            }

            this.classMap.computeIfAbsent(fileName, s -> this.javaFileCompiler.compile(file));

            Class cl = this.classMap.get(fileName);

            Predicate<Method> methodPredicate = method -> false;
            String requestMethod = cgiHeaders.get("REQUEST_METHOD");
            if ("POST".equalsIgnoreCase(requestMethod)) {
                methodPredicate = method -> method.isAnnotationPresent(POST.class);
            } else if ("GET".equalsIgnoreCase(requestMethod)) {
                methodPredicate = method -> method.isAnnotationPresent(GET.class);
            } else if ("PUT".equalsIgnoreCase(requestMethod)) {
                methodPredicate = method -> method.isAnnotationPresent(PUT.class);
            } else if ("DELETE".equalsIgnoreCase(requestMethod)) {
                methodPredicate = method -> method.isAnnotationPresent(DELETE.class);
            } else if ("HEAD".equalsIgnoreCase(requestMethod)) {
                methodPredicate = method -> method.isAnnotationPresent(HEAD.class);
            } else if ("OPTIONS".equalsIgnoreCase(requestMethod)) {
                methodPredicate = method -> method.isAnnotationPresent(OPTIONS.class);
            }

            Method method = Arrays.stream(cl.getDeclaredMethods())
                .filter(methodPredicate)
                .findFirst()
                .orElseThrow(RuntimeException::new);

            Response response = (Response) method.invoke(null, request);

            this.writeToSocket(response, socketChannel);
        } catch (Exception e) {
            Response errResponse = new HttpResponse();
            errResponse.setStatus(500);
            errResponse.setHeader(new HashMap<>());
            errResponse.setBody(ByteBuffer.allocate(0));
            try {
                this.writeToSocket(errResponse, socketChannel);
            } catch (Exception e1) {
                this.errorLogger.catching(e);
            }
            this.errorLogger.catching(e);
        }
    }

    private void writeToSocket(Response response, SocketChannel socketChannel) throws Exception {
        response.getHeader().put("Status", String.valueOf(response.getStatus()));

        StringBuilder stringBuilder = response.getHeader().entrySet().stream().collect(
            StringBuilder::new,
            (sb, entry) -> sb.append(entry.getKey()).append(":").append(entry.getValue()).append(System.lineSeparator()),
            (sb1, sb2) -> {}
        );


        stringBuilder.append(System.lineSeparator());

        socketChannel.write(
            FCGI.toByteBuffer(new Header(FCGI.VERSION, FCGI.STDOUT, (short) 1, (short) stringBuilder.length(), (byte)0, (byte)0))
        );

        socketChannel.write(
            ByteBuffer.wrap(stringBuilder.toString().getBytes())
        );

        byte[] outputData = response.getBody().array();
        for (int x = 0; x < Math.max(0xffff, outputData.length); x+= 0xffff) {
            outputByteBuffer.clear();
            outputByteBuffer.put(outputData, x, Math.min(outputData.length - x, 0xffff));
            socketChannel.write(
                FCGI.toByteBuffer(new Header(FCGI.VERSION, FCGI.STDOUT, (short) 1, (short) outputByteBuffer.position(), (byte)0, (byte)0))
            );
            outputByteBuffer.flip();
            socketChannel.write(
                outputByteBuffer
            );
        }

        socketChannel.write(
            FCGI.toByteBuffer(new Header(FCGI.VERSION, FCGI.STDOUT, (short) 1, (short) 0, (byte)0, (byte)0))
        );
        socketChannel.write(
            FCGI.toByteBuffer(new Header(FCGI.VERSION, FCGI.END_REQUEST, (short) 1, (short) 8, (byte) 0, (byte)0))
        );
        socketChannel.write(
            FCGI.toByteBuffer(new EndRequestBody(0, (byte) 0, new byte[3]))
        );
    }
}
