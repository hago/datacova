/*
 * Copyright (c) 2020.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.web.annotation;

import com.hagoapp.datacova.web.MethodName;
import com.hagoapp.datacova.web.authentication.AuthType;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * This annotation is used to mark methods that can deal with web request. The method should take one
 * {@code RoutingContext} parameter and output response. The return value(if any) is ignored. If the method has
 * different parameter signature, it will not be recognized.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface WebEndPoint {
    String[] methods() default {MethodName.GET};

    String path();

    boolean isPathRegex() default false;

    String[] headers() default {};

    boolean isBlocking() default true;

    AuthType[] authTypes() default AuthType.Anonymous;
}
