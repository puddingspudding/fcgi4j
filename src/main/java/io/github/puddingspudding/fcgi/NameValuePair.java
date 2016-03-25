package io.github.puddingspudding.fcgi;

/**
 * Created by pudding on 24.03.16.
 */
public class NameValuePair {

    private final String name;

    private final String value;

    public NameValuePair(final String name, final String value) {
        this.name = name;
        this.value = value;
    }

    public final String getName() { return this.name; }

    public final String getValue() {
        return this.value;
    }
}
