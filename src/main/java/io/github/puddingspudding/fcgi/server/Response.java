package io.github.puddingspudding.fcgi.server;

import java.nio.ByteBuffer;
import java.util.Map;

/**
 * Created by pudding on 19.03.16.
 */
public interface Response {

    int getStatus();

    Response setStatus(int status);

    Response setHeader(Map<String, String> header);

    Response setBody(ByteBuffer body);

    Map<String, String> getHeader();

    ByteBuffer getBody();

}
