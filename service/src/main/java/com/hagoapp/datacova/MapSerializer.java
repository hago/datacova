/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.jr.ob.JSON;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class MapSerializer {
    public static String serializeMap(Map<String, Object> map) throws IOException {
        return JSON.std.asString(map);
    }

    public static Map<String, Object> deserializeMap(String s) throws IOException {
        return JSON.std.mapFrom(s);
    }

    public static String serializeMap(List<Map<String, Object>> list) throws IOException {
        return JSON.std.asString(list);
    }

    public static List<Map<String, Object>> deserializeList(String s) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(s, new TypeReference<>() {
        });
    }
}
