package io.github.puddingspudding.fcgi.parser;

import io.github.puddingspudding.fcgi.BeginRequestBody;
import io.github.puddingspudding.fcgi.Parser;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by pudding on 16.04.16.
 */
public class BeginRequestBodyParser implements Parser<BeginRequestBody> {

    @Override
    public BeginRequestBody parseBigEndian(byte[] bytes) {
        return null;
    }

    @Override
    public BeginRequestBody parseLittleEndian(byte[] bytes) {
        return null;
    }

    @Override
    public BeginRequestBody parse(ByteBuffer byteBuffer) {
        return null;
    }

}
