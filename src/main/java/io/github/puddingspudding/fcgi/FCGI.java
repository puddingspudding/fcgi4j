package io.github.puddingspudding.fcgi;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.function.*;

/**
 * Constants.
 */
public class FCGI {

    public enum Type {
        BEGIN_REQUEST ((byte) 1),
        ABORT_REQUEST ((byte) 2),
        END_REQUEST ((byte) 2),
        PARAMS ((byte) 4),
        STDIN ((byte) 5),
        STDOUT ((byte) 6),
        STDERR ((byte) 7),
        DATA ((byte) 8),
        GET_VALUES ((byte) 9),
        GET_VALUES_RESULT ((byte) 10),
        UNKNOWN ((byte) 11);

        private final byte type;

        Type(final byte i) {
            this.type = i;
        }

        public byte toByte() {
            return this.type;
        }

        public static Type valueOf(final byte value) {
            for (Type type : values()) {
                if (type.toByte() == value) {
                    return type;
                }
            }
            throw new RuntimeException();
        }

    }

    public static final byte VERSION = 1;

    public static final int MAX_CONTENT_LENGTH = 0xffff; //
/*
    public static final Function<Integer, ByteBuffer> read(SocketChannel socketChannel, ByteBuffer byteBuffer) {
        return size -> {
            try {
                if (byteBuffer.remaining() < size) {
                    byteBuffer.compact();
                    socketChannel.read(byteBuffer);
                    byteBuffer.flip();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return byteBuffer;
        };
    }
*/

}
