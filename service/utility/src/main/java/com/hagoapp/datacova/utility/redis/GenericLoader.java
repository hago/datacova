/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.utility.redis;

import java.util.List;
import java.util.stream.Collectors;

public interface GenericLoader<T> {
    T perform(Object... params);

    default List<T> performBatch(List<List<Object>> paramsBatch) {
        return paramsBatch.stream().map(params -> perform(params.toArray())).collect(Collectors.toList());
    }
}
