package com.chl.gbo.util.token;


import com.google.common.base.Preconditions;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Period;

@ThreadSafe
class TokenParser {
    static final int FixedExpireSecondsOnGeneration = 1209600;

    private TokenParser() {
    }

    @CheckForNull
    public static TokenInfo tryParseToken(@Nonnull String plainToken) {
        Preconditions.checkNotNull(plainToken);

        try {
            return parseToken(plainToken);
        } catch (TokenFormatException var2) {
            return null;
        }
    }

    private static boolean NOT(boolean expr) {
        return !expr;
    }

    @Nonnull
    public static TokenInfo parseToken(@Nonnull String plainToken) throws TokenFormatException {
        Preconditions.checkNotNull(plainToken);
        String[] tokenFields = plainToken.split("&");
        if (tokenFields.length >= 3 && !NOT(tokenFields[2].length() == 8)) {
            String userName = tokenFields[0];

            long expireTimeLong;
            try {
                expireTimeLong = Long.parseLong(tokenFields[1]);
            } catch (NumberFormatException var14) {
                throw new TokenFormatException("token format error: " + plainToken, var14);
            }

            DateTime expireTime = new DateTime(expireTimeLong, DateTimeZone.UTC);
            DateTime generateTime = expireTime.minus(Period.seconds(1209600));
            if (tokenFields.length >= 8) {
                String ip = tokenFields[3];
                String plt = tokenFields[4];

                long generateTimeLong;
                try {
                    generateTimeLong = Long.parseLong(tokenFields[5]);
                } catch (NumberFormatException var13) {
                    throw new TokenFormatException("token format error: " + plainToken, var13);
                }

                generateTime = new DateTime(generateTimeLong, DateTimeZone.UTC);
                String uid = tokenFields[6];
                String version = tokenFields[7];
                return TokenInfo.create(userName, generateTime, expireTime, ip, plt, uid, version);
            } else {
                return TokenInfo.create(userName, generateTime, expireTime, "", "", "", "");
            }
        } else {
            throw new TokenFormatException("token format error: " + plainToken);
        }
    }

    @Nonnull
    public static TokenInfo parseCooperatorToken(@Nonnull String plainToken) {
        Preconditions.checkNotNull(plainToken);
        String[] tokenFields = StringUtils.split(plainToken, '&');
        if (tokenFields.length >= 3 && !NOT(tokenFields[0].length() == 8)) {
            String userName = tokenFields[1];

            long expireTimeLong;
            try {
                expireTimeLong = Long.parseLong(tokenFields[2]);
            } catch (NumberFormatException var14) {
                throw new TokenFormatException("token format error: " + plainToken, var14);
            }

            DateTime expireTime = new DateTime(expireTimeLong, DateTimeZone.UTC);
            DateTime generateTime = expireTime.minus(Period.seconds(1209600));
            if (tokenFields.length >= 8) {
                String ip = tokenFields[4];
                String plt = tokenFields[5];

                long generateTimeLong;
                try {
                    generateTimeLong = Long.parseLong(tokenFields[3]);
                } catch (NumberFormatException var13) {
                    throw new TokenFormatException("token format error: " + plainToken, var13);
                }

                generateTime = new DateTime(generateTimeLong, DateTimeZone.UTC);
                String uid = tokenFields[6];
                String version = tokenFields[7];
                return TokenInfo.create(userName, generateTime, expireTime, ip, plt, uid, version);
            } else {
                return TokenInfo.create(userName, generateTime, expireTime, "", "", "", "");
            }
        } else {
            throw new TokenFormatException("token format error: " + plainToken);
        }
    }
}
