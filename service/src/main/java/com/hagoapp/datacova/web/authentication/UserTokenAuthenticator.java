package com.hagoapp.datacova.web.authentication;

import com.hagoapp.datacova.user.UserInfo;
import io.vertx.ext.web.RoutingContext;

public class UserTokenAuthenticator extends Authenticator {

    private static final String HEADER_TOKEN = "USER_TOKEN";

    private UserInfo userInfo;

    @Override
    protected boolean validateAuthentication(RoutingContext routingContext) {
        String token = routingContext.request().getHeader(HEADER_TOKEN);
        if (token == null) {
            return false;
        }
        userInfo = UserTokenManager.getManager().findToken(token);
        return userInfo != null;
    }

    @Override
    public UserInfo getUserIdentity() {
        return userInfo;
    }
}
