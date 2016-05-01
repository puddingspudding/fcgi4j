package io.github.puddingspudding.fcgi;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Interface for parser.
 */
public interface Parser<T> {

    /**
     * Creates {@link T} from byte buffer.
     *
     * @param byteBuffer byte buffer convert into {@link T}
     * @return {@link T}
     */
    T parse(final ByteBuffer byteBuffer);

    /**
     * Checks ByteBuffer for available bytes. Returns true if enough bytes are available
     * and false if not.
     *
     * @param byteBuffer byte buffer to check
     * @return enough byte are available to read
     */
    boolean checkByteBuffer(final ByteBuffer byteBuffer);

}
