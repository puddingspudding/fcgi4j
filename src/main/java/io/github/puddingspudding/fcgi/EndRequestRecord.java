package io.github.puddingspudding.fcgi;

/**
 * Created by pudding on 24.03.16.
 */
public class EndRequestRecord {

    private final Header header;
    private final EndRequestBody body;

    public EndRequestRecord(final Header header, final EndRequestBody body) {
        this.header = header;
        this.body = body;
    }

}
