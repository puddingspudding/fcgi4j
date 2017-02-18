package io.github.puddingspudding.fcgi.server;

import io.github.puddingspudding.fcgi.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.IntStream;

/**
 * Created by pudding on 19.03.16.
 */
public class DefaultServer implements Server {

    public static final int DEFAULT_POOL_SIZE = 50;

    private int poolSize = DEFAULT_POOL_SIZE;

    private Function<Request, Response> defaultNotFoundHandler = req -> HttpResponse.notFound();

    private Map<String, Class> map;

    public DefaultServer setMap(final Map<String, Class> map) {
        this.map = map;
        return this;
    }

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

    /**
     *
     * @param socketAddress ip/host and port to bind
     */
    public DefaultServer(SocketAddress socketAddress) {
        this.socketAddress = socketAddress;

        try {
            this.serverSocketChannel = ServerSocketChannel.open();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }

    public DefaultServer setPoolSize(final int size) {
        this.poolSize = size;
        return this;
    }

    @Override
    public DefaultServer on(Predicate<String> requestMethod, Predicate<String> requestURI, Function<Request, Response> req2res) {
        this.handler.add(new RequestHelper(
            requestMethod,
            requestURI,
            req2res
        ));
        return this;
    }

    @Override
    public DefaultServer start() {


        try {
            this.serverSocketChannel.bind(this.socketAddress);
            IntStream.rangeClosed(0, this.poolSize).forEach(i -> {
                final Thread t = new Thread(() -> {
                    SocketChannel socketChannel;
                    final ByteBuffer bb = ByteBuffer.allocate(FCGI.MAX_CONTENT_LENGTH);
                    final ByteBuffer bbout = ByteBuffer.allocate(FCGI.MAX_CONTENT_LENGTH);
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

                            /*bb.clear();
                            bb.compact();*
/*                            */
                            bb.flip();

                            short id = 0;

                            while (!Thread.currentThread().isInterrupted()) {
                                header = FCGI.readHeader(FCGI.read(socketChannel, bb)).get();
                                id = header.getId();
                                type = header.getType();

                                if (type == FCGI.BEGIN_REQUEST) {
                                    BeginRequestBody beginRequestBody = FCGI.readBegingRequestBody(FCGI.read(socketChannel, bb)).get();
                                } else if (type == FCGI.PARAMS) {
                                    if (header.getContentLength() == 0) {
                                        continue;
                                    }
                                    int pos = bb.position();

                                    NameValuePair nameValuePair = FCGI.readNameValuePair(FCGI.read(socketChannel, bb)).get();
                                    if (nameValuePair.getName().startsWith("HTTP_")) {
                                        httpHeader.put(
                                            nameValuePair.getName().substring(5).toLowerCase().replace('_', '-'),
                                            nameValuePair.getValue()
                                        );
                                    }

                                    while ((bb.position() - pos) != header.getContentLength()) {
                                        nameValuePair = FCGI.readNameValuePair(FCGI.read(socketChannel, bb)).get();
                                        if (nameValuePair.getName().startsWith("HTTP_")) {
                                            httpHeader.put(
                                                nameValuePair.getName().substring(5).toLowerCase().replace('_', '-'),
                                                nameValuePair.getValue()
                                            );
                                        } else {
                                            fcgiHeader.put(nameValuePair.getName(), nameValuePair.getValue());
                                        }
                                    }

                                } else if (type == FCGI.STDIN) {
                                    if (header.getContentLength() == 0) {
                                        break;
                                    }
                                    while (bb.remaining() < header.getContentLength()) {
                                        bb.compact();
                                        socketChannel.read(bb);
                                        bb.flip();
                                    }
                                    byte[] tmp = new byte[header.getContentLength()];
                                    bb.get(tmp);
                                    byteArrayOutputStream.write(tmp);
                                }
                                if (header.getPaddingLength() > 0) {
                                    bb.get(new byte[header.getPaddingLength()]);
                                }
                            }
                            //socketChannel.shutdownInput();


                            String fileName = "/tmp/www" + fcgiHeader.get("SCRIPT_NAME");
                            Object o = this.map.get(fileName).newInstance();
                            HttpResponse response = (HttpResponse) this.map.get(fileName).getDeclaredMethod("get", HttpRequest.class, HttpResponse.class)
                                .invoke(o, new HttpRequest()
                                        .setHeader(httpHeader)
                                        .setBody(ByteBuffer.wrap(byteArrayOutputStream.toByteArray())), new HttpResponse());

                            byte[] outputData = new byte[0];
                            ByteBuffer data = response.getBody();
                            if (data != null) {
                                outputData = data.array();
                            }

                            StringBuilder stringBuilder = response.getHeader().entrySet().stream().collect(
                                () -> new StringBuilder(),
                                (sb, entry) -> sb.append(entry.getKey()).append(":").append(entry.getValue()).append(System.lineSeparator()),
                                (sb1, sb2) -> {}
                            );
                            stringBuilder.append(System.lineSeparator());

                            socketChannel.write(
                                FCGI.toByteBuffer(new Header(FCGI.VERSION, FCGI.STDOUT, id, (short) stringBuilder.length(), (byte)0, (byte)0))
                            );

                            socketChannel.write(
                                ByteBuffer.wrap(stringBuilder.toString().getBytes())
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
                            socketChannel.close();
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
