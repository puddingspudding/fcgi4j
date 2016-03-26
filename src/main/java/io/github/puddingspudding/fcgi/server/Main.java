package io.github.puddingspudding.fcgi.server;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by pudding on 19.03.16.
 */
public class Main {

    public static void main(String[] args) throws Exception {

        DefaultServer fcgiServer = new DefaultServer(new InetSocketAddress("127.0.0.1", 8080));

        fcgiServer
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
            .on(
                method -> "ANY_HTTP_METHOD".equalsIgnoreCase(method),
                uri -> true,
                req -> HttpResponse.ok().setBody(req.getBody())
            )
            .setPoolSize(25)
            .start();


    }
}
