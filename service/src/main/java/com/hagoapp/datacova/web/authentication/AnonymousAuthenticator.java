package com.hagoapp.datacova.web.authentication;

import com.hagoapp.datacova.user.UserInfo;
import io.vertx.ext.web.RoutingContext;

public class AnonymousAuthenticator extends Authenticator {
    @Override
    protected boolean validateAuthentication(RoutingContext routingContext) {
        return true;
    }

    @Override
    public UserInfo getUserIdentity() {
        return null;
    }
}
