package io.github.puddingspudding.nginj;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by pudding on 25.03.16.
 */
public class HttpResponse implements Response {

    private int status;

    private ByteBuffer body;

    private Map<String, String> header;

    public static final Response ok() {
        Map<String, String> header = new HashMap<>();
        header.put("Status", "200 OK");
        return new HttpResponse().setStatus(200).setHeader(header);
    }
    public static final Response notFound() {
        Map<String, String> header = new HashMap<>();
        header.put("Status", "404 Not Found");
        return new HttpResponse().setStatus(404).setHeader(header);
    }
    public static final Response error() {
        return new HttpResponse().setStatus(500);
    }

    @Override
    public int getStatus() {
        return this.status;
    }

    @Override
    public Response setStatus(int status) {
        this.status = status;
        return this;
    }

    @Override
    public Response setHeader(Map<String, String> header) {
        if(this.header != null) {
            this.header.putAll(header);
        } else {
            this.header = header;
        }
        return this;
    }

    @Override
    public Response setBody(ByteBuffer body) {
        this.body = body;
        return this;
    }

    @Override
    public Map<String, String> getHeader() {
        return this.header;
    }

    @Override
    public ByteBuffer getBody() {
        return this.body;
    }
}
