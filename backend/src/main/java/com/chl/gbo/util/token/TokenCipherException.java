package com.chl.gbo.util.token;

public class TokenCipherException extends TokenVerifyException {
    private static final long serialVersionUID = -6260476498438131761L;

    public TokenCipherException() {
    }

    public TokenCipherException(String message) {
        super(message);
    }

    public TokenCipherException(Throwable cause) {
        super(cause);
    }

    public TokenCipherException(String message, Throwable cause) {
        super(message, cause);
    }
}
