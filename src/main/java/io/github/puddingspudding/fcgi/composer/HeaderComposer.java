package io.github.puddingspudding.fcgi.composer;

import io.github.puddingspudding.fcgi.Composer;
import io.github.puddingspudding.fcgi.Header;

import java.nio.ByteBuffer;

/**
 * Composes {@link Header} into {@link ByteBuffer}.
 */
public class HeaderComposer implements Composer<Header> {

    @Override
    public ByteBuffer compose(Header header) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(8);
        byteBuffer.put(header.getVersion());
        byteBuffer.put(header.getType().toByte());
        byteBuffer.putShort((short) header.getId());
        byteBuffer.putShort((short) header.getContentLength());
        byteBuffer.put(header.getPaddingLength());
        byteBuffer.put(header.getReserved());
        return byteBuffer;
    }

}
