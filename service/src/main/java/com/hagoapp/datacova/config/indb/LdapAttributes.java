/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.config.indb;

import com.hagoapp.datacova.JsonStringify;
import com.hagoapp.datacova.util.ldap.Attributes;

import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

public class LdapAttributes implements JsonStringify {
    public static final String ATTRIBUTE_DISPLAY_NAME = "displayName";
    public static final String ATTRIBUTE_USERID = "cn";
    public static final String ATTRIBUTE_EMPLOYEE_NUMBER = "employeeNumber";
    public static final String ATTRIBUTE_EMPLOYEE_TYPE = "employeeType";
    public static final String ATTRIBUTE_GIVEN_NAME = "givenName";
    public static final String ATTRIBUTE_MAIL = "mail";
    public static final String ATTRIBUTE_MANAGER = "manager";
    public static final String ATTRIBUTE_TELEPHONE_NUMBER = "telephoneNumber";
    public static final String ATTRIBUTE_SN = "sn";
    public static final String ATTRIBUTE_PROXY_ADDRESS = "proxyAddresses";
    public static final String ATTRIBUTE_THUMBNAIL_PHOTO = "thumbnailPhoto";
    public static final String ATTRIBUTE_MEMBER_OF = "memberOf";
    public static final String ATTRIBUTE_NATIVE_NAME = "ATTRIBUTE_NATIVE_NAME";
    public static final String ATTRIBUTE_TITLE = "title";
    public static final String ATTRIBUTE_DISTINGUISHED_NAME = "distinguishedName";
    public static final String ATTRIBUTE_DN = "dn";

    private static final Map<String, String> items = new HashMap<>();

    public static Map<String, String> getItems() {
        return items;
    }

    public void normalize() {
        var fields = Attributes.class.getDeclaredFields();
        for (var field : fields) {
            int modifiers = field.getModifiers();
            if (!Modifier.isStatic(modifiers) || !Modifier.isPublic(modifiers) || !Modifier.isFinal(modifiers)) {
                continue;
            }
            if (!field.getName().startsWith("ATTRIBUTE_") || !field.getType().isAssignableFrom(String.class)) {
                continue;
            }
            try {
                var attrIdentity = field.getName();
                var attrDefaultName = field.get(null).toString();
                items.putIfAbsent(attrIdentity, attrDefaultName);
            } catch (IllegalAccessException e) {
                System.err.printf("LDAP attribute %s read error\r\n", field.getName());
            }
        }
    }
}
