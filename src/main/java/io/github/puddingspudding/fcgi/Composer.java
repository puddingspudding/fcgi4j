package io.github.puddingspudding.fcgi;

import java.nio.ByteBuffer;

/**
 * Created by pudding on 01.05.16.
 */
public interface Composer<T> {

    /**
     * composes a ByteBuffer from given {@link T}.
     *
     * @param object FCGI object
     * @return ByteBuffer
     */
    ByteBuffer compose(final T object);

}
