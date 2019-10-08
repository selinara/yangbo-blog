package com.chl.blogapi.util.token;


public class TokenVerifyException extends RuntimeException {
    private static final long serialVersionUID = -7614267060757223203L;

    public TokenVerifyException() {
    }

    public TokenVerifyException(String message) {
        super(message);
    }

    public TokenVerifyException(Throwable cause) {
        super(cause);
    }

    public TokenVerifyException(String message, Throwable cause) {
        super(message, cause);
    }
}
