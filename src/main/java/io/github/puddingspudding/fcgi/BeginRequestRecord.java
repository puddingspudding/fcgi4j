package io.github.puddingspudding.fcgi;

/**
 * Created by pudding on 24.03.16.
 */
public class BeginRequestRecord {

    private final Header header;

    private final BeginRequestBody body;

    public BeginRequestRecord(final Header header, final BeginRequestBody body) {
        this.header = header;
        this.body = body;
    }

    public final BeginRequestBody getBody() {
        return this.body;
    }

    public final Header getHeader() {
        return this.header;
    }
}
