package com.hagoapp.datacova.web.authentication;

public class AuthenticatorFactory {
    public static Authenticator createAuthenticator(AuthType authType) {
        switch (authType) {
            case ANONYMOUS:
                return new AnonymousAuthenticator();
            case USER_TOKEN:
                return new UserTokenAuthenticator();
            default:
                return null;
        }
    }
}
