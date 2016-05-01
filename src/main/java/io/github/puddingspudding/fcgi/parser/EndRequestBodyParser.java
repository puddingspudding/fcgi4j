package io.github.puddingspudding.fcgi.parser;

import io.github.puddingspudding.fcgi.EndRequestBody;
import io.github.puddingspudding.fcgi.Parser;

import java.nio.ByteBuffer;

/**
 * Creates {@link EndRequestBody} from various byte sources.
 */
public class EndRequestBodyParser implements Parser<EndRequestBody> {

    @Override
    public EndRequestBody parse(ByteBuffer byteBuffer) {
        if (!this.checkByteBuffer(byteBuffer)) {
            throw new RuntimeException();
        }
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
    public boolean checkByteBuffer(ByteBuffer byteBuffer) {
        return byteBuffer.remaining() >= 8;
    }
}
