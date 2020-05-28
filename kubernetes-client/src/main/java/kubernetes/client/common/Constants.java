package kubernetes.client.common;

public class Constants {
    public static final String PATTERN_URL = "^(https?)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";
    public static final String PATTERN_TYPE = "^[a-zA-Z0-9\\.\\-]*$";
    public static final String NETTY_PREFIX = "netty-http:";

    public static final String X_CORRELATION_ID = "X-Correlation-Id";
    public static final String X_REQUEST_ID = "X-Request-Id";
    public static final String X_BUSINESS_CONTEXT = "X-Business-Context";

    private Constants() {}
}
