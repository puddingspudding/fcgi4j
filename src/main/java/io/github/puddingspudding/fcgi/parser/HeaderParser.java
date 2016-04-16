package io.github.puddingspudding.fcgi.parser;

import io.github.puddingspudding.fcgi.Header;
import io.github.puddingspudding.fcgi.Parser;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Creates {@link Header} from various byte sources.
 */
public class HeaderParser implements Parser<Header> {

    @Override
    public Header parseBigEndian(byte[] bytes) {
        if (bytes.length != 8) {
            throw new RuntimeException();
        }
        return new Header(
            bytes[0],
            bytes[1],
            (short) ((bytes[2] << 8) + bytes[3]),
            (short) ((bytes[4] << 8) + bytes[5]),
            bytes[6],
            bytes[7]
        );
    }

    @Override
    public Header parseLittleEndian(byte[] bytes) {
        if (bytes.length != 8) {
            throw new RuntimeException();
        }
        return new Header(
            bytes[0],
            bytes[1],
            (short) ((bytes[3] << 8) + bytes[2]),
            (short) ((bytes[5] << 8) + bytes[4]),
            bytes[6],
            bytes[7]
        );
    }

    @Override
    public Header parse(ByteBuffer byteBuffer) {
        if (byteBuffer.remaining() < 8) {
            throw new RuntimeException();
        }
        return new Header(
            byteBuffer.get(),
            byteBuffer.get(),
            byteBuffer.getShort(),
            byteBuffer.getShort(),
            byteBuffer.get(),
            byteBuffer.get()
        );
    }

    @Override
    public Header parse(ByteArrayInputStream byteArrayInputStream) throws IOException {
        if (byteArrayInputStream.available() < 8) {
            throw new RuntimeException();
        }
        byte[] data = new byte[8];
        byteArrayInputStream.read(data);
        return this.parseBigEndian(data);
    }

}
