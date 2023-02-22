package com.hagoapp.datacova.web.authentication;

import com.hagoapp.datacova.user.UserInfo;
import com.hagoapp.datacova.util.web.AuthUtils;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserTokenAuthenticator extends Authenticator {

    private UserInfo userInfo;
    private final Logger logger = LoggerFactory.getLogger(UserTokenAuthenticator.class);

    @Override
    protected boolean validateAuthentication(RoutingContext routingContext) {
        String token = AuthUtils.Companion.getCurrentToken(routingContext);
        if (token == null) {
            return false;
        }
        userInfo = UserTokenManager.getManager().findToken(token);
        if (userInfo == null) {
            logger.warn("User for token {} not found", token);
        }
        return userInfo != null;
    }

    @Override
    public UserInfo getUserIdentity() {
        return userInfo;
    }
}
