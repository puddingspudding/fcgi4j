package io.github.puddingspudding.nginj;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Created by pudding on 19.03.16.
 */
public interface Response {

    Response setHeader(Map<String, String> header);

    Response setBody(ByteBuffer body);

    Map<String, String> getHeader();

    ByteBuffer getBody();

}
