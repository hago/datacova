package com.hagoapp.datacova.web.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * This annotation is used on methods which will be invoked automatically while service starting. The method should
 * take no parameter and return an object, or it will be ignored.
 * The returned object will be stored in memory and is accessible with its identifier.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface PreLoad {
    String identifier();
}
