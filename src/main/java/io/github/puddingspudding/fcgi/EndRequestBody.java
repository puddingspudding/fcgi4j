package io.github.puddingspudding.fcgi;

import java.nio.ByteBuffer;

/**
 * Created by pudding on 24.03.16.
 */
public class EndRequestBody {

    private final int appStatus;
    private final byte protocalStatus;
    private final byte[] reserved;

    public EndRequestBody(final int appStatus, final byte protocalStatus, final byte[] reserved) {
        if (reserved.length != 3) {
            throw new RuntimeException("reserved size is not 3");
        }
        this.appStatus = appStatus;
        this.protocalStatus = protocalStatus;
        this.reserved = reserved;
    }

    public final int getAppStatus() {
        return this.appStatus;
    }

    public final byte getProtocalStatus() {
        return this.protocalStatus;
    }

    public final byte[] getReserved() {
        return this.reserved;
    }


}
