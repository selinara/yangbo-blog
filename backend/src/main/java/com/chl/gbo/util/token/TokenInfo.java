package com.chl.gbo.util.token;


import com.google.common.base.Preconditions;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import org.joda.time.DateTime;
import org.joda.time.Period;

@Immutable
public final class TokenInfo {
    private final String userName;
    private final DateTime generateTime;
    private final DateTime expireTime;

    static TokenInfo create(@Nonnull String userName, @Nonnull DateTime generateTime, @Nonnull DateTime expireTime, @Nonnull String ip, @Nonnull String plt, @Nonnull String uid, @Nonnull String version) {
        return new TokenInfo(userName, generateTime, expireTime, ip, plt, uid, version);
    }

    TokenInfo(@Nonnull String userName, @Nonnull DateTime generateTime, @Nonnull DateTime expireTime, @Nonnull String ip, @Nonnull String plt, @Nonnull String uid, @Nonnull String version) {
        Preconditions.checkNotNull(userName);
        Preconditions.checkNotNull(generateTime);
        Preconditions.checkNotNull(expireTime);
        Preconditions.checkNotNull(ip);
        Preconditions.checkNotNull(plt);
        Preconditions.checkNotNull(uid);
        Preconditions.checkNotNull(version);
        this.userName = userName;
        this.generateTime = generateTime;
        this.expireTime = expireTime;
    }

    @Nonnull
    public String getUsername() {
        return this.userName;
    }

    @Nonnull
    public DateTime getGenerateTime() {
        return this.generateTime;
    }

    @Nonnull
    public DateTime getExpireTime() {
        return this.expireTime;
    }

    public boolean isExpired() {
        return this.expireTime.isBeforeNow();
    }

    /** @deprecated */
    @Deprecated
    public boolean isExpired(@Nonnull Period validDuration) {
        return this.generateTime.plus(validDuration).isBeforeNow();
    }

    private static boolean NOT(boolean expr) {
        return !expr;
    }

    public boolean isStillValid() {
        return NOT(this.isExpired());
    }

    /** @deprecated */
    @Deprecated
    public boolean isStillValid(@Nonnull Period validDuration) {
        return NOT(this.isExpired(validDuration));
    }

    public String toString() {
        return String.format("{%s, %s}", this.userName, this.generateTime.toString("yyyy-MM-dd HH:mm:ss.SSS ZZ"));
    }

    public int hashCode() {
        return this.generateTime.hashCode();
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj == null) {
            return false;
        } else {
            return obj instanceof TokenInfo ? this.equals((TokenInfo)obj) : false;
        }
    }

    public boolean equals(TokenInfo other) {
        if (this == other) {
            return true;
        } else if (other == null) {
            return false;
        } else {
            return this.userName.equals(other.userName) && this.generateTime.equals(other.generateTime);
        }
    }
}
