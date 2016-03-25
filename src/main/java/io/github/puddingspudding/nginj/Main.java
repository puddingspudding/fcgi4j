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

        new DefaultServer(new InetSocketAddress("192.168.1.140", 8080))
            .on(
                Request.isGET,
                uri -> true, // match any
                request -> {
                    Map<String, String> header = new HashMap<>();
                    header.put("Auth", Integer.toHexString(0xbfe123cd));
                    header.put("test", "woot");
                    Response response = HttpResponse
                        .notFound()
                        .setHeader(header)
                        .setBody(ByteBuffer.wrap("Hello World GET".getBytes()));
                    return response;
                }
            )
            .on(
                Request.isPOST,
                uri -> true,
                request -> {
                    Response response = HttpResponse
                        .ok()
                        .setHeader(new HashMap<>())
                        .setBody(ByteBuffer.wrap("Hello World POST".getBytes()));
                    return response;
                }
            )
            .setPoolSize(25)
            .start();


    }
}
