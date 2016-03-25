package io.github.puddingspudding.nginj;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.function.Predicate;

/**
 * Created by pudding on 19.03.16.
 */
public interface Request {

    Predicate<String> isGET = method -> "GET".equalsIgnoreCase(method);
    Predicate<String> isPOST = method -> "POST".equalsIgnoreCase(method);
    Predicate<String> isPUT = method -> "PUT".equalsIgnoreCase(method);
    Predicate<String> isDELETE = method -> "DELETE".equalsIgnoreCase(method);

    Map<String, String> getHeader();

    ByteBuffer getBody();
}
