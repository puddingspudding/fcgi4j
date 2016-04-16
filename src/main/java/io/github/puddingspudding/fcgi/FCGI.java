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

    public static final int MAX_CONTENT_LENGTH = 0xffff;

    public static final ByteBuffer toByteBuffer(final Header header) {
        /*ByteBuffer bb = ByteBuffer.allocate(8);
        bb.put(header.getVersion());
        bb.put(header.getType());
        bb.putShort(header.getId());
        bb.putShort((short) header.getContentLength());
        bb.put(header.getPaddingLength());
        bb.put(header.getReserved());
        bb.flip();
        return bb;*/
        return null;
    }

    public static final ByteBuffer toByteBuffer(final EndRequestBody endRequestBody) {
        ByteBuffer bb = ByteBuffer.allocate(8);
        bb.putInt(endRequestBody.getAppStatus());
        bb.put(endRequestBody.getProtocalStatus());
        bb.put(endRequestBody.getReserved());
        bb.flip();
        return bb;
    }

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

    public static final Supplier<Header> readHeader(Function<Integer, ByteBuffer> supplier) {
        return () -> {
            return null;
            /*final ByteBuffer byteBuffer = supplier.apply(8);
            return new Header(
                byteBuffer.get(),
                byteBuffer.get(),
                byteBuffer.getShort(),
                byteBuffer.getShort(),
                byteBuffer.get(),
                byteBuffer.get()
            );*/
        };
    }

    public static final Supplier<BeginRequestBody> readBegingRequestBody(Function<Integer, ByteBuffer> supplier) {
        return () -> {
            final ByteBuffer byteBuffer = supplier.apply(8);
            return new BeginRequestBody(
                    (short) (byteBuffer.get() + (byteBuffer.get() << 8)),
                    byteBuffer.get(),
                    new byte[]{
                            byteBuffer.get(), byteBuffer.get(), byteBuffer.get(), byteBuffer.get(), byteBuffer.get()
                    }
            );
        };
    }

    public static final Supplier<NameValuePair> readNameValuePair(Function<Integer, ByteBuffer> supplier) {
        return () -> {
            ByteBuffer byteBuffer = supplier.apply(2);
            int nameLength = byteBuffer.get();

            if (nameLength == 0) return null;

            if (nameLength >> 7 == 1) {
                byteBuffer = supplier.apply(3);
                nameLength = ((nameLength & 0x7f) << 24) + (byteBuffer.get() << 16) + (byteBuffer.get() << 8) + byteBuffer.get();
            }


            int valueLength = byteBuffer.get();
            if (valueLength >> 7 == 1) {
                byteBuffer = supplier.apply(3);
                valueLength = ((valueLength & 0x7f) << 24) + (byteBuffer.get(0) << 16) + (byteBuffer.get(1) << 8) + byteBuffer.get(2);
            }

            byteBuffer = supplier.apply(nameLength);
            byte[] nameBa = new byte[nameLength];
            byteBuffer.get(nameBa);
            String name = new String(nameBa);

            byteBuffer = supplier.apply(valueLength);
            byte[] valueBa = new byte[valueLength];
            byteBuffer.get(valueBa);
            String value = new String(valueBa);

            return new NameValuePair(
                name,
                value
            );
        };
    }

}
