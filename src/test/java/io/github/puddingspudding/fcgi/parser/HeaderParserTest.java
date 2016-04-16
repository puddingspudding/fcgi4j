package io.github.puddingspudding.fcgi.parser;

import io.github.puddingspudding.fcgi.FCGI;
import io.github.puddingspudding.fcgi.Header;
import org.junit.Assert;
import org.junit.Test;

import java.nio.ByteBuffer;

import static org.junit.Assert.*;

/**
 * Tests for {@link HeaderParser}.
 */
public class HeaderParserTest {

    /**
     * class to test.
     */
    private final HeaderParser headerParser = new HeaderParser();

    private static final byte VERSION = 1;
    private static final FCGI.Type TYPE = FCGI.Type.PARAMS;
    private static final int ID = 0xffff;
    private static final int CONTENT_LENGTH = 0x1111;
    private static final byte PADDING_LENGTH = 0;
    private static final byte RESERVED = 1;

    @Test
    public void parseBigEndian() throws Exception {
        byte[] byteArray = new byte[8];
        byteArray[0] = VERSION;
        byteArray[1] = TYPE.toByte();
        byteArray[2] = (byte) ((ID & 0xff00) >> 8);
        byteArray[3] = (byte) (ID & 0x00ff);
        byteArray[4] = (byte) ((CONTENT_LENGTH & 0xff00) >> 8);
        byteArray[5] = (byte) (CONTENT_LENGTH & 0x00ff);
        byteArray[6] = PADDING_LENGTH;
        byteArray[7] = RESERVED;

        Header header = this.headerParser.parseBigEndian(byteArray);

        Assert.assertEquals(VERSION, header.getVersion());
        Assert.assertEquals(TYPE, header.getType());
        Assert.assertEquals(ID, header.getId());
        Assert.assertEquals(CONTENT_LENGTH, header.getContentLength());
        Assert.assertEquals(PADDING_LENGTH, header.getPaddingLength());
        Assert.assertEquals(RESERVED, header.getReserved());
    }

    @Test
    public void parseLittleEndian() throws Exception {
        byte[] byteArray = new byte[8];
        byteArray[0] = VERSION;
        byteArray[1] = TYPE.toByte();
        byteArray[2] = (byte) (ID & 0x00ff);
        byteArray[3] = (byte) ((ID & 0xff00) >> 8);
        byteArray[4] = (byte) (CONTENT_LENGTH & 0x00ff);
        byteArray[5] = (byte) ((CONTENT_LENGTH & 0xff00) >> 8);
        byteArray[6] = PADDING_LENGTH;
        byteArray[7] = RESERVED;

        Header header = this.headerParser.parseBigEndian(byteArray);

        Assert.assertEquals(VERSION, header.getVersion());
        Assert.assertEquals(TYPE, header.getType());
        Assert.assertEquals(ID, header.getId());
        Assert.assertEquals(CONTENT_LENGTH, header.getContentLength());
        Assert.assertEquals(PADDING_LENGTH, header.getPaddingLength());
        Assert.assertEquals(RESERVED, header.getReserved());
    }

    @Test
    public void parse() throws Exception {
        ByteBuffer byteBuffer = ByteBuffer.allocate(8);
        byteBuffer.put(VERSION);
        byteBuffer.put(TYPE.toByte());
        byteBuffer.putShort((short) ID);
        byteBuffer.putShort((short) CONTENT_LENGTH);
        byteBuffer.put(PADDING_LENGTH);
        byteBuffer.put(RESERVED);
        byteBuffer.flip();

        Header header = this.headerParser.parse(byteBuffer);

        Assert.assertEquals(VERSION, header.getVersion());
        Assert.assertEquals(TYPE, header.getType());
        Assert.assertEquals(ID, header.getId());
        Assert.assertEquals(CONTENT_LENGTH, header.getContentLength());
        Assert.assertEquals(PADDING_LENGTH, header.getPaddingLength());
        Assert.assertEquals(RESERVED, header.getReserved());
    }

}