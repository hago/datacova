package com.hagoapp.datacova.user;

public class UserInfo {
    private String userId;
    private String provider;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserInfo userInfo = (UserInfo) o;

        if (!userId.equals(userInfo.userId)) return false;
        return provider.equals(userInfo.provider);
    }

    @Override
    public int hashCode() {
        int result = userId.hashCode();
        result = 31 * result + provider.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "UserInfo{"
                + "userId='" + userId + '\''
                + ", provider='" + provider + '\''
                + '}';
    }
}
