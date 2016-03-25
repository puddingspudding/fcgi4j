package io.github.puddingspudding.fcgi;

import java.nio.ByteBuffer;
import java.util.function.BiFunction;
import java.util.function.Supplier;

/**
 * Created by pudding on 24.03.16.
 */
public class Header {

    private final byte version;
    private final byte type;
    private final short id;
    private final short contentLength;
    private final byte paddingLength;
    private final byte reserved;

    public Header(
        final byte version,
        final byte type,
        final short id,
        final short contentLength,
        final byte paddingLength,
        final byte reserved
    ) {
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

    public final byte getType() {
        return this.type;
    }

    public final short getId() {
        return this.id;
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
