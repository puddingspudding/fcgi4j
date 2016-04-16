package io.github.puddingspudding.fcgi.parser;

import io.github.puddingspudding.fcgi.NameValuePair;
import io.github.puddingspudding.fcgi.Parser;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Creates {@link NameValuePair} from various byte sources.
 */
public class NameValuePairParser implements Parser<NameValuePair> {

    @Override
    public NameValuePair parseBigEndian(byte[] bytes) {
        return null;
    }

    @Override
    public NameValuePair parseLittleEndian(byte[] bytes) {
        return null;
    }

    @Override
    public NameValuePair parse(ByteBuffer byteBuffer) {
        int nameLength = byteBuffer.get();

        if (nameLength == 0) return null;

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

}
