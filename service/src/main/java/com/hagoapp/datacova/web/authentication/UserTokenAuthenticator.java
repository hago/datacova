package com.hagoapp.datacova.web.authentication;

import com.hagoapp.datacova.CoVaLogger;
import com.hagoapp.datacova.user.UserInfo;
import com.hagoapp.datacova.util.web.AuthUtils;
import io.vertx.ext.web.RoutingContext;
import org.apache.logging.log4j.Logger;

public class UserTokenAuthenticator extends Authenticator {

    private UserInfo userInfo;
    private final Logger logger = CoVaLogger.getLogger();

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
