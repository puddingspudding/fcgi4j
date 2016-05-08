package io.github.puddingspudding.fcgi.converter;

import io.github.puddingspudding.fcgi.BeginRequestBody;
import io.github.puddingspudding.fcgi.ByteBufferConverter;

import java.nio.ByteBuffer;

/**
 * Created by pudding on 07.05.16.
 */
public class BeginRequestBodyConverter implements ByteBufferConverter<BeginRequestBody> {

    @Override
    public ByteBuffer toByteBuffer(BeginRequestBody beginRequestBody) {
        return this.toByteBuffer(beginRequestBody, ByteBuffer.allocate(8));
    }

    @Override
    public ByteBuffer toByteBuffer(BeginRequestBody beginRequestBody, ByteBuffer byteBuffer) {
        byteBuffer.putShort(beginRequestBody.getRole());
        byteBuffer.put(beginRequestBody.getFlags());
        byteBuffer.put(beginRequestBody.getReserved());
        return byteBuffer;
    }

    @Override
    public BeginRequestBody fromByteBuffer(ByteBuffer byteBuffer) {
        if (!this.validate(byteBuffer)) {
            throw new RuntimeException();
        }
        return new BeginRequestBody(
            (short) (byteBuffer.get() + (byteBuffer.get() << 8)),
            byteBuffer.get(),
            new byte[]{
                byteBuffer.get(), byteBuffer.get(), byteBuffer.get(), byteBuffer.get(), byteBuffer.get()
            }
        );
    }

    @Override
    public boolean validate(ByteBuffer byteBuffer) {
        return byteBuffer.remaining() >= 8;
    }
}
