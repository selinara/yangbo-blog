package com.chl.blogapi.util.token;


import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.security.PublicKey;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;

import org.apache.commons.codec.Charsets;
import org.apache.commons.codec.binary.Base64;

import com.google.common.base.Preconditions;

class TokenDecrypter {
    private static final PublicKey _PublicKey;
    private Cipher decryptCipher;

    public TokenDecrypter() {
        try {
            this.decryptCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            this.decryptCipher.init(2, _PublicKey);
        } catch (Exception var2) {
            throw new RuntimeException(var2);
        }
    }

    @CheckForNull
    public String tryDecrypt(@Nonnull String tokenString) {
        Preconditions.checkNotNull(tokenString);

        try {
            return this.decrypt(tokenString);
        } catch (TokenVerifyException var3) {
            return null;
        }
    }

    @Nonnull
    public String decrypt(@Nonnull String tokenString) throws TokenCipherException {
        Preconditions.checkNotNull(tokenString);

        try {
            byte[] dec = (new Base64(true)).decode(tokenString);
            byte[] utf8 = this.decryptCipher.doFinal(dec);
            return new String(utf8, Charsets.UTF_8);
        } catch (BadPaddingException var4) {
            throw new TokenCipherException(var4);
        } catch (IllegalBlockSizeException var5) {
            throw new TokenCipherException(var5);
        }
    }

    static {
        try {
            String publicKeyFile = "publicKey";
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            InputStream inStream = cl.getResourceAsStream(publicKeyFile);

            try {
                ObjectInputStream publicInput = new ObjectInputStream(inStream);

                try {
                    _PublicKey = (PublicKey)publicInput.readObject();
                } finally {
                    publicInput.close();
                    inStream = null;
                }
            } finally {
                if (inStream != null) {
                    inStream.close();
                }

            }

        } catch (IOException var16) {
            throw new RuntimeException(var16);
        } catch (ClassNotFoundException var17) {
            throw new RuntimeException(var17);
        }
    }
}
