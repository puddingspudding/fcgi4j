package io.github.puddingspudding.fcgi;

/**
 * Header for every message.
 *
 * byte version
 * byte type
 * short id
 * short content length
 * byte padding length
 * byte reserved
 */
public class Header {

    private final byte version;
    private final FCGI.Type type;
    private final short id;
    private final short contentLength;
    private final byte paddingLength;
    private final byte reserved;

    public Header(
        final byte version,
        final FCGI.Type type,
        final short id,
        final short contentLength,
        final byte paddingLength,
        final byte reserved
    ) {
        if (version != 1) {
            throw new IllegalArgumentException();
        }
        this.version = version;
        this.type = type;
        this.id = id;
        this.contentLength = contentLength;
        this.paddingLength = paddingLength;
        this.reserved = reserved;
    }

    public final byte getVersion() {
        return this.version;
    }

    public final FCGI.Type getType() {
        return this.type;
    }

    public final int getId() {
        return Short.toUnsignedInt(this.id);
    }

    public final int getContentLength() {
        return Short.toUnsignedInt(this.contentLength);
    }

    public final byte getPaddingLength() {
        return this.paddingLength;
    }

    public final byte getReserved() {
        return this.reserved;
    }

}
