package io.github.puddingspudding.nginj;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.Map;

/**
 * Created by pudding on 19.03.16.
 */
public interface Request {

    Map<String, String> getHeader();

    ByteBuffer getBody();
}
