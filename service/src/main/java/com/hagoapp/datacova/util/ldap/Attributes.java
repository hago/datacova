/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.util.ldap;

import com.hagoapp.datacova.CoVaLogger;
import org.slf4j.Logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;

/**
 * A definition of attributes of a specified LDAP server. Each LDAP server may have its own field conventions for
 * meaningful attributes, use this class to define which field is used for user id / name / ..., etc, for a given
 * LDAP server.
 */
public class Attributes {
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

    private Attributes() {

    }

    private static final Map<String, String> nameMap = new HashMap<>();
    private static final Logger logger = CoVaLogger.getLogger();

    public static Attributes loadDefault() {
        return loadFromMap(Map.of());
    }

    public static Attributes loadFromMap(Map<String, String> source) {
        if (source == null) {
            return null;
        }
        loadNames();
        Attributes x = new Attributes();
        nameMap.forEach((k, value) -> {
            x.map.put(k, source.getOrDefault(k, value));
        });
        return x;
    }

    private static void loadNames() {
        if (nameMap.isEmpty()) {
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
                    nameMap.put(attrIdentity, attrDefaultName);
                } catch (IllegalAccessException e) {
                    logger.error("LDAP attribute {} read error", field.getName());
                }
            }
        }
    }

    public static Attributes loadFromProperty(Properties props) {
        if (props == null) {
            return null;
        }
        return loadFromMap(props.entrySet().stream().collect(Collectors.toMap(Object::toString, Object::toString)));
    }

    public static Attributes loadFromPropertyFile(String fileName) {
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(fileName)) {
            props.load(fis);
            return loadFromProperty(props);
        } catch (IOException e) {
            return null;
        }
    }

    private final Map<String, String> map = new HashMap<>();

    public String getAttributeName(String attributeIdentity) {
        return map.get(attributeIdentity);
    }

    public List<String> getAttributeNames(List<String> attributeIdentities) {
        return attributeIdentities.stream().map(this::getAttributeIdentity).collect(Collectors.toList());
    }

    public String getAttributeIdentity(String attributeName) {
        for (var entry : map.entrySet()) {
            if (entry.getValue().equals(attributeName)) {
                return entry.getKey();
            }
        }
        return null;
    }
}
