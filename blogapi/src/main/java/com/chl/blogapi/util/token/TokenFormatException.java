package com.chl.blogapi.util.token;

public class TokenFormatException extends TokenVerifyException {
    private static final long serialVersionUID = -3857600614428480359L;

    public TokenFormatException() {
    }

    public TokenFormatException(String message) {
        super(message);
    }

    public TokenFormatException(Throwable cause) {
        super(cause);
    }

    public TokenFormatException(String message, Throwable cause) {
        super(message, cause);
    }
}
