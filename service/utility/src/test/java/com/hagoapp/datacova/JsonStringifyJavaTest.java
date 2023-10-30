/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova;

import lombok.Getter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class JsonStringifyJavaTest {
    @Getter
    static class JsonStringifySample implements JsonStringify {
        private final int id;
        private final String name;

        public JsonStringifySample(int id, String name) {
            this.id = id;
            this.name = name;
        }
    }

    @Test
    void testJsonStringify() {
        var obj = new JsonStringifySample(1, "json");
        var json = obj.toJson();
        Assertions.assertNotNull(json);
        Assertions.assertTrue(json.indexOf(String.format("%d", obj.getId())) > 0);
        Assertions.assertTrue(json.indexOf(String.format("\"%s\"", obj.getName())) > 0);
    }
}
