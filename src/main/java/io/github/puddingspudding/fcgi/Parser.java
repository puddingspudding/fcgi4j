package io.github.puddingspudding.fcgi;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Interface for parser.
 */
public interface Parser<T> {

    /**
     * Creates {@link T} from bytes in big endian order.
     *
     * @param bytes byte to convert into {@link T}
     * @return {@link T}
     */
    @Deprecated
    T parseBigEndian(final byte[] bytes);

    /**
     * Creates {@link T} from bytes in little endian order.
     *
     * @param bytes byte to convert into {@link T}
     * @return {@link T}
     */
    @Deprecated
    T parseLittleEndian(final byte[] bytes);

    /**
     * Creates {@link T} from byte buffer.
     *
     * @param byteBuffer byte buffer convert into {@link T}
     * @return {@link T}
     */
    T parse(final ByteBuffer byteBuffer);

    /**
     * Creates {@link T} from byte array input stream.
     *
     * @param byteArrayInputStream byte array input stream convert into {@link T}
     * @return {@link T}
     */
    T parse(final ByteArrayInputStream byteArrayInputStream) throws IOException;

}
