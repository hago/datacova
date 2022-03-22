package com.hagoapp.datacova.web.authentication;

import com.hagoapp.datacova.config.CoVaConfig;
import com.hagoapp.datacova.user.UserInfo;
import com.hagoapp.datacova.util.Utils;
import com.hagoapp.datacova.web.authentication.usertoken.UserTokenStore;
import com.hagoapp.datacova.web.authentication.usertoken.UserTokenStoreMemory;
import com.hagoapp.datacova.web.authentication.usertoken.UserTokenStoreRedis;

import java.util.ArrayList;
import java.util.List;

public class UserTokenManager {
    private static final UserTokenManager manager = new UserTokenManager();

    static {
        manager.stores.add(new UserTokenStoreMemory());
        manager.stores.add(new UserTokenStoreRedis(CoVaConfig.getConfig().getRedis()));
    }

    public static UserTokenManager getManager() {
        return manager;
    }

    private UserTokenManager() {
        //
    }

    private final List<UserTokenStore> stores = new ArrayList<>();

    public void addStore(UserTokenStore store) {
        stores.add(store);
    }

    public String createToken(UserInfo userInfo) {
        String token = Utils.genRandomString(12, null);
        stores.forEach(store -> store.storeUserToken(token, userInfo));
        return token;
    }

    public void removeToken(String token) {
        stores.forEach(store -> store.removeUserToken(token));
    }

    public UserInfo findToken(String token) {
        for (var store : stores) {
            UserInfo u = store.findUserToken(token);
            if (u != null) {
                return u;
            }
        }
        return null;
    }
}
