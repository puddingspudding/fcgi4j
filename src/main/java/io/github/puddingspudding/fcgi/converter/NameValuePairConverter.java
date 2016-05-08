package io.github.puddingspudding.fcgi.converter;

import io.github.puddingspudding.fcgi.ByteBufferConverter;
import io.github.puddingspudding.fcgi.NameValuePair;

import java.nio.ByteBuffer;

/**
 * Created by pudding on 07.05.16.
 */
public class NameValuePairConverter implements ByteBufferConverter<NameValuePair> {


    @Override
    public ByteBuffer toByteBuffer(NameValuePair nameValuePair) {
        return null;
    }

    @Override
    public ByteBuffer toByteBuffer(NameValuePair nameValuePair, ByteBuffer byteBuffer) {
        return null;
    }

    @Override
    public NameValuePair fromByteBuffer(ByteBuffer byteBuffer) {
        if (!this.validate(byteBuffer)) {
            throw new RuntimeException();
        }

        int nameLength = byteBuffer.get();

        if (nameLength >> 7 == 1) {
            nameLength = ((nameLength & 0x7f) << 24) + (byteBuffer.get() << 16) + (byteBuffer.get() << 8) + byteBuffer.get();
        }

        int valueLength = byteBuffer.get();
        if (valueLength >> 7 == 1) {
            valueLength = ((valueLength & 0x7f) << 24) + (byteBuffer.get() << 16) + (byteBuffer.get() << 8) + byteBuffer.get();
        }

        byte[] nameBa = new byte[nameLength];
        byteBuffer.get(nameBa);
        String name = new String(nameBa);

        byte[] valueBa = new byte[valueLength];
        byteBuffer.get(valueBa);
        String value = new String(valueBa);

        return new NameValuePair(
                name,
                value
        );
    }

    @Override
    public boolean validate(ByteBuffer byteBuffer) {
        byteBuffer.mark();
        try {
            if (byteBuffer.remaining() < 1) {
                return false;
            }
            int nameLength = byteBuffer.get();
            if (nameLength >> 7 == 1) {
                if (byteBuffer.remaining() < 3) {
                    return false;
                }
                nameLength = ((nameLength & 0x7f) << 24) + (byteBuffer.get() << 16) + (byteBuffer.get() << 8) + byteBuffer.get();
            }
            if (byteBuffer.remaining() < 1) {
                return false;
            }
            int valueLength = byteBuffer.get();
            if (valueLength >> 7 == 1) {
                if (byteBuffer.remaining() < 3) {
                    return false;
                }
                valueLength = ((valueLength & 0x7f) << 24) + (byteBuffer.get() << 16) + (byteBuffer.get() << 8) + byteBuffer.get();
            }

            if (byteBuffer.remaining() < nameLength + valueLength) {
                return false;
            }
        } finally {
            byteBuffer.reset();
        }
        return true;
    }
}
