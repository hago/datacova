/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.utility.redis;

/**
 * The deserializer that pare content fetched from redis into object in specified type.
 *
 * @param <T> returned type
 * @author suncjs
 * @since 0.5
 */
public interface Deserializer<T> {
    T deserialize(String json);
}
