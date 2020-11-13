package com.hagoapp.datacova.web.authentication;

public class AuthenticatorFactory {
    public static Authenticator createAuthenticator(AuthType authType) {
        switch (authType) {
            case Anonymous:
                return new AnonymousAuthenticator();
            case UserToken:
                return new UserTokenAuthenticator();
            default:
                return null;
        }
    }
}
