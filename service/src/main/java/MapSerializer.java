/*
 * Copyright (c) 2020.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

import com.fasterxml.jackson.jr.ob.JSON;

import java.io.IOException;
import java.util.Map;

public class MapSerializer {
    public static String serializeMap(Map<String, Object> map) throws IOException {
        return JSON.std.asString(map);
    }

    public static Map<String, Object> deserializeMap(String s) throws IOException {
        return JSON.std.mapFrom(s);
    }
}
