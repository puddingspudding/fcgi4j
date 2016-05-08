package io.github.puddingspudding.fcgi;

import java.nio.ByteBuffer;

/**
 * Created by pudding on 07.05.16.
 */
public interface ByteBufferConverter<T> {


    ByteBuffer toByteBuffer(final T t);

    ByteBuffer toByteBuffer(final T t, final ByteBuffer byteBuffer);

    T fromByteBuffer(final ByteBuffer byteBuffer);

    boolean validate(final ByteBuffer byteBuffer);

}
