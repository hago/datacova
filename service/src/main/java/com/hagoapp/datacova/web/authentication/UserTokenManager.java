package com.hagoapp.datacova.web.authentication;

import com.hagoapp.datacova.user.UserInfo;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class UserTokenManager {
    private static UserTokenManager manager = new UserTokenManager();

    public static UserTokenManager getManager() {
        return manager;
    }

    private Map<String, UserInfo> tokenUserMap = new ConcurrentHashMap<>();
    private Map<UserInfo, List<String>> userTokensMap = new ConcurrentHashMap<>();

    public String createToken(UserInfo userInfo) {
        String token = "";
        tokenUserMap.put(token, userInfo);
        userTokensMap.merge(userInfo, List.of(token), (currentTokenList, newTokenList) -> {
            if (newTokenList.get(0) == null) {
                return currentTokenList;
            }
            String aToken = newTokenList.get(0);
            currentTokenList.add(aToken);
            return currentTokenList;
        });
        return token;
    }

    public void removeToken(String token) {
        UserInfo userInfo = tokenUserMap.remove(token);
        if (userInfo != null) {
            List<String> list = userTokensMap.get(userInfo);
            if (list != null) {
                list.remove(token);
                userTokensMap.put(userInfo, list);
            }
        }
    }

    public UserInfo findToken(String token) {
        return tokenUserMap.get(token);
    }
}
