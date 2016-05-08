package io.github.puddingspudding.fcgi.converter;

import io.github.puddingspudding.fcgi.ByteBufferConverter;
import io.github.puddingspudding.fcgi.FCGI;
import io.github.puddingspudding.fcgi.Header;

import java.nio.ByteBuffer;

/**
 * Created by pudding on 07.05.16.
 */
public class HeaderConverter implements ByteBufferConverter<Header> {

    @Override
    public ByteBuffer toByteBuffer(Header header) {
        return this.toByteBuffer(header, ByteBuffer.allocate(8));
    }

    @Override
    public ByteBuffer toByteBuffer(Header header, ByteBuffer byteBuffer) {
        if (!this.validate(byteBuffer)) {
            throw new RuntimeException();
        }
        byteBuffer.put(header.getVersion());
        byteBuffer.put(header.getType().toByte());
        byteBuffer.putShort((short) header.getId());
        byteBuffer.putShort((short) header.getContentLength());
        byteBuffer.put(header.getPaddingLength());
        byteBuffer.put(header.getReserved());
        return byteBuffer;
    }

    @Override
    public Header fromByteBuffer(ByteBuffer byteBuffer) {
        if (!this.validate(byteBuffer)) {
            throw new RuntimeException();
        }
        return new Header(
            byteBuffer.get(),
            FCGI.Type.valueOf(byteBuffer.get()),
            byteBuffer.getShort(),
            byteBuffer.getShort(),
            byteBuffer.get(),
            byteBuffer.get()
        );
    }

    @Override
    public boolean validate(ByteBuffer byteBuffer) {
        return byteBuffer.remaining() >= 8;
    }
}
