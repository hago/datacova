/*
 * Copyright (c) 2020.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 */

package com.hagoapp.datacova.user;

import com.hagoapp.datacova.JsonStringify;

public class UserInfo implements JsonStringify {

    public static final int HIDE_USERINFO_DEFAULT = 0;
    public static final int HIDE_USERINFO_ID = 0x1;
    public static final int HIDE_USERINFO_NAME = 0x2;
    public static final int HIDE_USERINFO_DESCRIPTION = 0x4;
    public static final int HIDE_USERINFO_ADD_BY = 0x8;
    public static final int HIDE_USERINFO_ADD_TIME = 0x10;
    public static final int HIDE_USERINFO_MODIFY_BY = 0x20;
    public static final int HIDE_USERINFO_MODIFY_TIME = 0x40;
    public static final int HIDE_USERINFO_STATUS = 0x80;
    public static final int HIDE_USERINFO_THUMBNAIL = 0x100;
    public static final int HIDE_USERINFO_EMAIL = 0x200;
    public static final int HIDE_USERINFO_MOBILE = 0x400;
    public static final String DEFAULT_STRING_MASK = "********";
    public static final long DEFAULT_LONG_MASK = 0L;
    public static final UserStatus DEFAULT_USER_STATUS_MASK = UserStatus.Unknown;

    private long id;
    private String userId;
    private String pwdHash;
    private String provider;
    private String name;
    private String description;
    private long addBy;
    private long addTime;
    private Long modifyBy;
    private Long modifyTime;
    private String thumbnail;
    private UserStatus status;
    private UserType userType;
    private String mobile;
    private String email;

    public UserInfo() {

    }

    public UserInfo(String userId, String provider) {
        this.userId = userId;
        this.provider = provider;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPwdHash() {
        return pwdHash;
    }

    public void setPwdHash(String pwdHash) {
        this.pwdHash = pwdHash;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getAddBy() {
        return addBy;
    }

    public void setAddBy(long addBy) {
        this.addBy = addBy;
    }

    public long getAddTime() {
        return addTime;
    }

    public void setAddTime(long addTime) {
        this.addTime = addTime;
    }

    public Long getModifyBy() {
        return modifyBy;
    }

    public void setModifyBy(Long modifyBy) {
        this.modifyBy = modifyBy;
    }

    public Long getModifyTime() {
        return modifyTime;
    }

    public void setModifyTime(Long modifyTime) {
        this.modifyTime = modifyTime;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }

    public UserType getUserType() {
        return userType;
    }

    public void setUserType(UserType userType) {
        this.userType = userType;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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
        return "UserInfo{" +
                "id='" + id + '\'' +
                "userId='" + userId + '\'' +
                ", provider='" + provider + '\'' +
                ", userType=" + userType +
                '}';
    }

    public UserInfo maskUserInfo() {
        return maskUserInfo(HIDE_USERINFO_DEFAULT + HIDE_USERINFO_MOBILE + HIDE_USERINFO_EMAIL);
    }

    public UserInfo maskUserInfo(int mask) {
        return maskUserInfo(mask, DEFAULT_STRING_MASK);
    }

    public UserInfo maskUserInfo(int mask, String maskString) {
        this.pwdHash = maskString;
        if ((mask & HIDE_USERINFO_ID) != 0) {
            this.id = DEFAULT_LONG_MASK;
        }
        if ((mask & HIDE_USERINFO_ADD_BY) != 0) {
            this.addBy = DEFAULT_LONG_MASK;
        }
        if ((mask & HIDE_USERINFO_ADD_TIME) != 0) {
            this.addTime = DEFAULT_LONG_MASK;
        }
        if ((mask & HIDE_USERINFO_DESCRIPTION) != 0) {
            this.description = maskString;
        }
        if ((mask & HIDE_USERINFO_NAME) != 0) {
            this.name = maskString;
        }
        if ((mask & HIDE_USERINFO_MODIFY_BY) != 0) {
            this.modifyBy = DEFAULT_LONG_MASK;
        }
        if ((mask & HIDE_USERINFO_MODIFY_TIME) != 0) {
            this.modifyTime = DEFAULT_LONG_MASK;
        }
        if ((mask & HIDE_USERINFO_STATUS) != 0) {
            this.status = DEFAULT_USER_STATUS_MASK;
        }
        if ((mask & HIDE_USERINFO_THUMBNAIL) != 0) {
            this.thumbnail = null;
        }
        if ((mask & HIDE_USERINFO_EMAIL) != 0) {
            this.email = maskString;
        }
        if ((mask & HIDE_USERINFO_MOBILE) != 0) {
            this.mobile = maskString;
        }
        return this;
    }
}
