package io.github.puddingspudding.nginj;

import io.github.puddingspudding.fcgi.*;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.channels.spi.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Created by pudding on 19.03.16.
 */
public class Main {

    public static void main(String[] args) throws Exception {


        Server server = new DefaultServer(new InetSocketAddress("192.168.1.140", 8080));


        server.on(
            method -> "GET".equals(method),
            uri -> true,
            request -> {
                return new Response() {
                    @Override
                    public Response setHeader(Map<String, String> header) {
                        return null;
                    }

                    @Override
                    public Response setBody(ByteBuffer body) {
                        return null;
                    }

                    @Override
                    public Map<String, String> getHeader() {
                        return null;
                    }

                    @Override
                    public ByteBuffer getBody() {
                        return ByteBuffer.wrap("HALLO WELT".getBytes());
                    }
                };
            }
        );

        server.start();


    }
}
