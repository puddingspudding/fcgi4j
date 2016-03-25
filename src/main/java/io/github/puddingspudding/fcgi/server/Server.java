package io.github.puddingspudding.fcgi.server;

import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Created by pudding on 19.03.16.
 */
public interface Server {

    Server on(Predicate<String> requestMethod, Predicate<String> requestURI, Function<Request, Response> req2res);


    Server start();


}
