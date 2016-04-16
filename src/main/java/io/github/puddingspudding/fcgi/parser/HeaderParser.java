package io.github.puddingspudding.fcgi.parser;

import io.github.puddingspudding.fcgi.FCGI;
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
            FCGI.Type.valueOf(bytes[1]),
            (short) (((bytes[2] & 0xff) << 8) + (bytes[3] & 0xff)),
            (short) (((bytes[4] & 0xff) << 8) + (bytes[5] & 0xff)),
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
            FCGI.Type.valueOf(bytes[1]),
            (short) (((bytes[3] & 0xff) << 8) + (bytes[2] & 0xff)),
            (short) (((bytes[5] & 0xff) << 8) + (bytes[4] & 0xff)),
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
            FCGI.Type.valueOf(byteBuffer.get()),
            byteBuffer.getShort(),
            byteBuffer.getShort(),
            byteBuffer.get(),
            byteBuffer.get()
        );
    }

}
