package io.github.puddingspudding.fcgi.parser;

import io.github.puddingspudding.fcgi.BeginRequestBody;
import io.github.puddingspudding.fcgi.Parser;

import java.nio.ByteBuffer;

/**
 * Creates {@link BeginRequestBody} from various byte sources.
 */
public class BeginRequestBodyParser implements Parser<BeginRequestBody> {

    @Override
    public BeginRequestBody parse(ByteBuffer byteBuffer) {
        if (!this.checkByteBuffer(byteBuffer)) {
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
    public boolean checkByteBuffer(ByteBuffer byteBuffer) {
        return byteBuffer.remaining() >= 8;
    }

}
