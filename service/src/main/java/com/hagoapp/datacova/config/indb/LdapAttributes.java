/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.config.indb;

import com.hagoapp.datacova.JsonStringify;

import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class LdapAttributes implements JsonStringify {
    public static final String ATTRIBUTE_DISPLAY_NAME = "ATTRIBUTE_DISPLAY_NAME";
    public static final String ATTRIBUTE_USERID = "ATTRIBUTE_USERID";
    public static final String ATTRIBUTE_EMPLOYEE_NUMBER = "ATTRIBUTE_EMPLOYEE_NUMBER";
    public static final String ATTRIBUTE_EMPLOYEE_TYPE = "ATTRIBUTE_EMPLOYEE_TYPE";
    public static final String ATTRIBUTE_GIVEN_NAME = "ATTRIBUTE_GIVEN_NAME";
    public static final String ATTRIBUTE_MAIL = "ATTRIBUTE_MAIL";
    public static final String ATTRIBUTE_MANAGER = "ATTRIBUTE_MANAGER";
    public static final String ATTRIBUTE_TELEPHONE_NUMBER = "ATTRIBUTE_TELEPHONE_NUMBER";
    public static final String ATTRIBUTE_SN = "ATTRIBUTE_SN";
    public static final String ATTRIBUTE_PROXY_ADDRESS = "ATTRIBUTE_PROXY_ADDRESS";
    public static final String ATTRIBUTE_THUMBNAIL_PHOTO = "ATTRIBUTE_THUMBNAIL_PHOTO";
    public static final String ATTRIBUTE_MEMBER_OF = "ATTRIBUTE_MEMBER_OF";
    public static final String ATTRIBUTE_NATIVE_NAME = "ATTRIBUTE_NATIVE_NAME";
    public static final String ATTRIBUTE_TITLE = "ATTRIBUTE_TITLE";
    public static final String ATTRIBUTE_DISTINGUISHED_NAME = "ATTRIBUTE_DISTINGUISHED_NAME";
    public static final String ATTRIBUTE_DN = "ATTRIBUTE_DN";

    public static final String DEFAULT_ATTRIBUTE_DISPLAY_NAME = "displayName";
    public static final String DEFAULT_ATTRIBUTE_USERID = "cn";
    public static final String DEFAULT_ATTRIBUTE_EMPLOYEE_NUMBER = "employeeNumber";
    public static final String DEFAULT_ATTRIBUTE_EMPLOYEE_TYPE = "employeeType";
    public static final String DEFAULT_ATTRIBUTE_GIVEN_NAME = "givenName";
    public static final String DEFAULT_ATTRIBUTE_MAIL = "mail";
    public static final String DEFAULT_ATTRIBUTE_MANAGER = "manager";
    public static final String DEFAULT_ATTRIBUTE_TELEPHONE_NUMBER = "telephoneNumber";
    public static final String DEFAULT_ATTRIBUTE_SN = "sn";
    public static final String DEFAULT_ATTRIBUTE_PROXY_ADDRESS = "proxyAddresses";
    public static final String DEFAULT_ATTRIBUTE_THUMBNAIL_PHOTO = "thumbnailPhoto";
    public static final String DEFAULT_ATTRIBUTE_MEMBER_OF = "memberOf";
    public static final String DEFAULT_ATTRIBUTE_NATIVE_NAME = "ATTRIBUTE_NATIVE_NAME";
    public static final String DEFAULT_ATTRIBUTE_TITLE = "title";
    public static final String DEFAULT_ATTRIBUTE_DISTINGUISHED_NAME = "distinguishedName";
    public static final String DEFAULT_ATTRIBUTE_DN = "dn";

    private static final Map<String, String> defaults = new HashMap<>();

    static {
        var fields = LdapAttributes.class.getDeclaredFields();
        for (var field : fields) {
            int modifiers = field.getModifiers();
            if (!Modifier.isStatic(modifiers) || !Modifier.isPublic(modifiers) || !Modifier.isFinal(modifiers)) {
                continue;
            }
            if (!field.getName().startsWith("ATTRIBUTE_") || !field.getType().isAssignableFrom(String.class)) {
                continue;
            }
            try {
                var defaultNameField = LdapAttributes.class.getDeclaredField(String.format("DEFAULT_%s", field.getName()));
                var attrIdentity = field.getName();
                var attrDefaultName = defaultNameField.get(null).toString();
                defaults.putIfAbsent(attrIdentity, attrDefaultName);
            } catch (IllegalAccessException e) {
                System.err.printf("LDAP attribute %s read error\r\n", field.getName());
            } catch (NoSuchFieldException e) {
                System.err.printf("LDAP attribute DEFAULT_%s is absent\r\n", field.getName());
            }
        }
    }

    public static LdapAttributes defaultAttributes() {
        var instance = new LdapAttributes();
        instance.normalize();
        return instance;
    }

    public static Map<String, String> getDefaults() {
        return defaults;
    }

    private final Map<String, String> items = new HashMap<>();

    public void normalize() {
        defaults.forEach((k, v) -> {
            if (!items.containsKey(k)) {
                items.put(k, v);
            }
        });
    }

    public Map<String, String> getItems() {
        return items;
    }

    public Map<String, String> getAttributes() {
        return getItems();
    }

    public String getActualAttribute(String name) {
        return items.get(name);
    }

    public List<String> getActualAttributes(List<String> names) {
        return names.stream().map(name -> {
            var k = getActualAttribute(name);
            if (k == null) {
                return name;
            } else {
                return k;
            }
        }).collect(Collectors.toList());
    }
}
