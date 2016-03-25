package io.github.puddingspudding.fcgi.server;

import java.nio.ByteBuffer;
import java.util.Map;

/**
 * Created by pudding on 25.03.16.
 */
public class HttpRequest implements Request {

    private Map<String, String> header;

    private ByteBuffer body;

    @Override
    public Map<String, String> getHeader() {
        return this.header;
    }

    @Override
    public ByteBuffer getBody() {
        return this.body;
    }

    @Override
    public HttpRequest setHeader(Map<String, String> header) {
        if (this.header != null) {
            this.header.putAll(header);
        } else {
            this.header = header;
        }
        return this;
    }

    @Override
    public HttpRequest setBody(ByteBuffer byteBuffer) {
        this.body = byteBuffer;
        return this;
    }
}
