package io.github.puddingspudding.fcgi;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link Header}.
 */
public class HeaderTest {

    /**
     * class to test.
     */
    private Header header;

    /**
     * values for tests.
     */
    private final static byte VERSION = 1;
    private final static FCGI.Type TYPE = FCGI.Type.BEGIN_REQUEST;
    private final static short ID = 0x1111;
    private final static short CONTENT_LENGTH = 0x1111;
    private final static byte PADDING_LENGTH = 0x1;
    private final static byte RESERVED = 0x1;

    @Before
    public void setUp() throws Exception {
        this.header = new Header(
            VERSION,
            TYPE,
            ID,
            CONTENT_LENGTH,
            PADDING_LENGTH,
            RESERVED
        );
    }

    @Test
    public void getVersion() throws Exception {
        Assert.assertEquals(VERSION, this.header.getVersion());
    }

    @Test
    public void getType() throws Exception {
        Assert.assertEquals(TYPE, this.header.getType());
    }

    @Test
    public void getId() throws Exception {
        Assert.assertEquals(ID, this.header.getId());
    }

    @Test
    public void getContentLength() throws Exception {
        Assert.assertEquals(CONTENT_LENGTH, this.header.getContentLength());
    }

    @Test
    public void getPaddingLength() throws Exception {
        Assert.assertEquals(PADDING_LENGTH, this.header.getPaddingLength());
    }

    @Test
    public void getReserved() throws Exception {
        Assert.assertEquals(RESERVED, this.header.getReserved());
    }

    @Test
    public void getContentLengthUnsignedShort() {
        short positiveLength = (short) 0xffff; // 65535

        Header header = new Header(
            VERSION,
            TYPE,
            ID,
            positiveLength,
            PADDING_LENGTH,
            RESERVED
        );

        Assert.assertEquals(0xffff, header.getContentLength());
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidVersion() {
        new Header(
            (byte) 0x2,
            TYPE,
            ID,
            CONTENT_LENGTH,
            PADDING_LENGTH,
            RESERVED
        );
    }

}