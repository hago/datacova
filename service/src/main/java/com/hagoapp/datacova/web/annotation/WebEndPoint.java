package com.hagoapp.datacova.web.annotation;

import com.hagoapp.datacova.web.authentication.AuthType;
import io.vertx.core.http.HttpMethod;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * This annotation is used to mark methods that can deal with web request. The method should take one
 * {@code RoutingContext} parameter and output response. The return value(if any) is ignored. If the method has
 * different parameter signature, it will not be recognized.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface WebEndPoint {
    HttpMethod[] methods() default {HttpMethod.GET};

    String path();

    boolean isPathRegex() default false;

    String[] headers() default {};

    boolean isBlocking() default false;

    AuthType[] authTypes() default AuthType.Anonymous;
}
