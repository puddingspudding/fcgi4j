package io.github.puddingspudding.fcgi.parser;

import io.github.puddingspudding.fcgi.FCGI;
import io.github.puddingspudding.fcgi.Header;
import io.github.puddingspudding.fcgi.Parser;

import java.nio.ByteBuffer;

/**
 * Creates {@link Header} from various byte sources.
 */
public class HeaderParser implements Parser<Header> {

    @Override
    public Header parse(ByteBuffer byteBuffer) {
        if (!this.checkByteBuffer(byteBuffer)) {
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
    public boolean checkByteBuffer(ByteBuffer byteBuffer) {
        return byteBuffer.remaining() >= 8;
    }

}
