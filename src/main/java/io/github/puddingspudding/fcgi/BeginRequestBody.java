package io.github.puddingspudding.fcgi;

/**
 * Created by pudding on 24.03.16.
 */
public class BeginRequestBody {

    private final short role;
    private final byte flags;
    private final byte[] reserved;

    public BeginRequestBody(final short role, final byte flags, final byte[] reserved) {
        this.role = role;
        this.flags = flags;
        this.reserved = reserved;
    }

    public final short getRole() {
        return this.role;
    }

    public final byte getFlags() {
        return this.flags;
    }

    public final byte[] getReserved() {
        return this.reserved;
    }
}
