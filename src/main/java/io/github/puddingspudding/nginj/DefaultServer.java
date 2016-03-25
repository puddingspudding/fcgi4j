package io.github.puddingspudding.nginj;

import io.github.puddingspudding.fcgi.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.channels.spi.SelectorProvider;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Created by pudding on 19.03.16.
 */
public class DefaultServer implements Server {

    private class RequestHelper {

        private final Predicate<String> requestMethod;
        private final Predicate<String> requestUri;
        private final Function<Request, Response> handler;

        public RequestHelper(final Predicate<String> requestMethod, final Predicate<String> requestUri, final Function<Request, Response> handler) {
            this.requestMethod = requestMethod;
            this.requestUri = requestUri;
            this.handler = handler;
        }

        public final Predicate<String> getRequestMethod() {
            return this.requestMethod;
        }

        public final Predicate<String> getRequestUri() {
            return this.requestUri;
        }

        public final Function<Request, Response> getHandler() {
            return this.handler;
        }
    }

    private final List<RequestHelper> handler = new ArrayList<>();

    private final SocketAddress socketAddress;

    private final ServerSocketChannel serverSocketChannel;

    public DefaultServer(SocketAddress socketAddress) {
        this.socketAddress = socketAddress;

        try {
            this.serverSocketChannel = ServerSocketChannel.open();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }

    @Override
    public Server on(Predicate<String> requestMethod, Predicate<String> requestURI, Function<Request, Response> req2res) {
        this.handler.add(new RequestHelper(
            requestMethod,
            requestURI,
            req2res
        ));
        return this;
    }

    @Override
    public Server start() {
        final int POOL_SIZE = 50;

        try {
            this.serverSocketChannel.bind(this.socketAddress);
            IntStream.rangeClosed(0, POOL_SIZE).forEach(i -> {
                final Thread t = new Thread(() -> {
                    SocketChannel socketChannel;
                    final ByteBuffer bb = ByteBuffer.allocate(0x1ffff);
                    final ByteBuffer bbout = ByteBuffer.allocate(0xffff);
                    final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    while (!Thread.currentThread().isInterrupted()) {
                        Header header;
                        byte type;
                        Map<String, String> httpHeader = new HashMap<>(20);
                        Map<String, String> fcgiHeader = new HashMap<>(20);

                        try {
                            byteArrayOutputStream.reset();
                            socketChannel = this.serverSocketChannel.accept();
                            socketChannel.finishConnect();

                            bb.clear();
                            socketChannel.read(bb);
                            bb.flip();
                            short id = 0;

                            while (!Thread.currentThread().isInterrupted()) {
                                header = FCGI.read(FCGI.read(socketChannel, bb)).get();
                                id = header.getId();
                                type = header.getType();
                                System.out.println(type);
                                if (type == FCGI.BEGIN_REQUEST) {
                                    BeginRequestBody beginRequestBody = FCGI.readBegingRequestBody(FCGI.read(socketChannel, bb)).get();
                                } else if (type == FCGI.PARAMS) {
                                    if (header.getContentLength() == 0) {
                                        continue;
                                    }
                                    int pos = bb.position();

                                    NameValuePair nameValuePair = FCGI.readNameValuePair(FCGI.read(socketChannel, bb)).get();
                                    if (nameValuePair.getName().startsWith("HTTP_")) {
                                        httpHeader.put(nameValuePair.getName().substring(5), nameValuePair.getValue());
                                    }

                                    while ((bb.position() - pos) != header.getContentLength()) {
                                        nameValuePair = FCGI.readNameValuePair(FCGI.read(socketChannel, bb)).get();
                                        if (nameValuePair.getName().startsWith("HTTP_")) {
                                            httpHeader.put(nameValuePair.getName().substring(5), nameValuePair.getValue());
                                        } else {
                                            fcgiHeader.put(nameValuePair.getName(), nameValuePair.getValue());
                                        }
                                    }

                                } else if (type == FCGI.STDIN) {
                                    if (header.getContentLength() == 0) {
                                        break;
                                    }
                                    while (bb.remaining() < Short.toUnsignedInt(header.getContentLength())) {
                                        bb.compact();
                                        socketChannel.read(bb);
                                        bb.flip();
                                    }
                                    byte[] tmp = new byte[Short.toUnsignedInt(header.getContentLength())];
                                    bb.get(tmp);
                                    byteArrayOutputStream.write(tmp);
                                }
                                if (header.getPaddingLength() > 0) {
                                    bb.get(new byte[header.getPaddingLength()]);
                                }
                            }
                            socketChannel.shutdownInput();


                            httpHeader.forEach((key, value) -> {
                                System.out.println(key + ":" + value);
                            });
                            fcgiHeader.forEach((key, value) -> {
                                System.out.println(key + ":" + value);
                            });


                            Optional<RequestHelper> requestHelperOptional = this.handler.stream()
                                .filter(
                                    requestHelper -> requestHelper.getRequestMethod().test(fcgiHeader.get("REQUEST_METHOD"))
                                ).filter(
                                    requestHelper -> requestHelper.getRequestUri().test(fcgiHeader.get("DOCUMENT_URI"))
                                ).findFirst();

                            Response response = requestHelperOptional.get().getHandler().apply(
                                new Request() {
                                    @Override
                                    public Map<String, String> getHeader() {
                                        return httpHeader;
                                    }

                                    @Override
                                    public ByteBuffer getBody() {
                                        return ByteBuffer.wrap(byteArrayOutputStream.toByteArray());
                                    }
                                }
                            );


                            byte[] outputData = response.getBody().array();

                            socketChannel.write(
                                FCGI.toByteBuffer(new Header(FCGI.VERSION, FCGI.STDOUT, id, (short) 25, (byte)0, (byte)0))
                            );
                            byte[] content = ("Content-type: text/html" + System.lineSeparator() + System.lineSeparator()).getBytes();
                            socketChannel.write(
                                ByteBuffer.wrap(content)
                            );

                            for (int x = 0; x < Math.max(0xffff, outputData.length); x+= 0xffff) {
                                bbout.clear();
                                bbout.put(outputData, x, Math.min(outputData.length - x, 0xffff));
                                socketChannel.write(
                                    FCGI.toByteBuffer(new Header(FCGI.VERSION, FCGI.STDOUT, id, (short) bbout.position(), (byte)0, (byte)0))
                                );
                                bbout.flip();
                                socketChannel.write(
                                    bbout
                                );
                            }


                            socketChannel.write(
                                    FCGI.toByteBuffer(new Header(FCGI.VERSION, FCGI.STDOUT, id, (short) 0, (byte)0, (byte)0))
                            );
                            socketChannel.write(
                                FCGI.toByteBuffer(new Header(FCGI.VERSION, FCGI.END_REQUEST, id, (short) 8, (byte) 0, (byte)0))
                            );
                            socketChannel.write(
                                FCGI.toByteBuffer(new EndRequestBody(0, (byte) 0, new byte[3]))
                            );



                            socketChannel.shutdownOutput();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                });
                t.setUncaughtExceptionHandler((t1, e) -> {
                    e.printStackTrace();
                    //t.start();
                });
                t.setName("Worker " + i);
                t.start();
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

        return this;
    }


}
