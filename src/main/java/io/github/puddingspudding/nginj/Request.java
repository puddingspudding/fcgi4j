package io.github.puddingspudding.nginj;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.function.Predicate;

/**
 * Created by pudding on 19.03.16.
 */
public interface Request {

    Map<String, String> getHeader();

    ByteBuffer getBody();

    Request setHeader(Map<String, String> header);

    Request setBody(ByteBuffer byteBuffer);


}
