package com.chl.gbo.util.token;


import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import com.google.common.base.Preconditions;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;
import org.apache.commons.lang3.mutable.MutableObject;

@ThreadSafe
public final class TokenVerifier {
    private TokenVerifier() {
    }

    public static boolean tryVerifyToken(@Nonnull String tokenString, @Nonnull MutableObject<TokenInfo> result) {
        Preconditions.checkNotNull(tokenString);
        Preconditions.checkNotNull(result);

        try {
            result.setValue(null);
            TokenInfo token = verifyToken(tokenString);
            result.setValue(token);
            return true;
        } catch (TokenVerifyException var3) {
            return false;
        }
    }

    @Nonnull
    public static TokenInfo verifyToken(@Nonnull String tokenString) throws TokenVerifyException {
        Preconditions.checkNotNull(tokenString);
        String plainToken = (new TokenDecrypter()).decrypt(tokenString);
        return TokenParser.parseToken(plainToken);
    }

    public static boolean tryVerifyCooperatorToken(@Nonnull String tokenString, @Nonnull MutableObject<TokenInfo> result) {
        Preconditions.checkNotNull(tokenString);
        Preconditions.checkNotNull(result);

        try {
            result.setValue(null);
            TokenInfo token = verifyCooperatorToken(tokenString);
            result.setValue(token);
            return true;
        } catch (TokenVerifyException var3) {
            return false;
        }
    }

    @Nonnull
    public static TokenInfo verifyCooperatorToken(@Nonnull String tokenString) throws TokenVerifyException {
        Preconditions.checkNotNull(tokenString);
        String plainToken = (new TokenDecrypter()).decrypt(tokenString);
        return TokenParser.parseCooperatorToken(plainToken);
    }
}
