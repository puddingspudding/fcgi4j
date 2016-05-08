package io.github.puddingspudding.fcgi.converter;

import io.github.puddingspudding.fcgi.ByteBufferConverter;
import io.github.puddingspudding.fcgi.EndRequestBody;

import java.nio.ByteBuffer;

/**
 * Created by pudding on 07.05.16.
 */
public class EngRequestBodyConverter implements ByteBufferConverter<EndRequestBody> {

    @Override
    public ByteBuffer toByteBuffer(EndRequestBody endRequestBody) {
        return this.toByteBuffer(endRequestBody, ByteBuffer.allocate(8));
    }

    @Override
    public ByteBuffer toByteBuffer(EndRequestBody endRequestBody, ByteBuffer byteBuffer) {
        byteBuffer.putInt(endRequestBody.getAppStatus());
        byteBuffer.put(endRequestBody.getProtocalStatus());
        byteBuffer.put(endRequestBody.getReserved());
        return byteBuffer;
    }

    @Override
    public EndRequestBody fromByteBuffer(ByteBuffer byteBuffer) {
        return new EndRequestBody(
            byteBuffer.getInt(),
            byteBuffer.get(),
            new byte[]{
                byteBuffer.get(),
                byteBuffer.get(),
                byteBuffer.get()
            }
        );
    }

    @Override
    public boolean validate(ByteBuffer byteBuffer) {
        return byteBuffer.remaining() >= 8;
    }
}
