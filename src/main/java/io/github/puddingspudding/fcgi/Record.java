package io.github.puddingspudding.fcgi;

/**
 * Created by pudding on 09.04.16.
 */
public class Record {

    private Header header;

    private byte[] content;

    private byte[] padding;

    public final Header getHeader() {
        return this.header;
    }

    public Record setHeader(Header header) {
        this.header = header;
        return this;
    }

    public final byte[] getContent() {
        return this.content;
    }

    public Record setContent(byte[] content) {
        this.content = content;
        return this;
    }

    public final byte[] getPadding() {
        return this.padding;
    }

    public Record setPadding(byte[] padding) {
        this.padding = padding;
        return this;
    }
}
