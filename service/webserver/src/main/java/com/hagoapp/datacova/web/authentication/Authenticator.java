package com.hagoapp.datacova.web.authentication;

import com.hagoapp.datacova.user.UserInfo;
import io.vertx.ext.web.RoutingContext;

import java.util.HashMap;
import java.util.Map;

public abstract class Authenticator {
    public static final String AUTHENTICATION_USER_ID = "AUTHENTICATION_USER_ID";
    public static final String AUTHENTICATION_EXTRA_PARAMS = "AUTHENTICATION_EXTRA_PARAMS";

    protected RoutingContext context;

    protected abstract boolean validateAuthentication(RoutingContext routingContext);

    public boolean authenticate(RoutingContext routingContext) {
        boolean ret = this.validateAuthentication(routingContext);
        if (ret) {
            routingContext.put(AUTHENTICATION_USER_ID, this.getUserIdentity());
            Map<String, Object> params = this.getExtraParams();
            if ((params != null) && !params.isEmpty()) {
                routingContext.put(AUTHENTICATION_EXTRA_PARAMS, params);
            }
        }
        return ret;
    }

    public abstract UserInfo getUserIdentity();

    public Map<String, Object> getExtraParams() {
        return new HashMap<>();
    }

    public String[] getHeaders() { return new String[0]; }

    public static UserInfo getUser(RoutingContext routingContext) {
        return routingContext.get(AUTHENTICATION_USER_ID);
    }
}
