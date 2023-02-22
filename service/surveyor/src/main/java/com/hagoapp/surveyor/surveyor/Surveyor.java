/*
 *  Copyright (c) 2021.
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.surveyor.surveyor;

import com.hagoapp.surveyor.RuleConfig;
import org.jetbrains.annotations.NotNull;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;

/**
 * The operations that a surveyor should support.
 *
 * @author Chaojun Sun
 * @since 0.1
 */
public interface Surveyor extends Closeable {
    /**
     * This method should return a string indicating type of configuration that it can handle, the return value should
     * match the return vale of <code>getConfigType</code> method of a <code>RuleConfig</code> instance.
     *
     * @return surveyor type identity string
     */
    String getSupportedConfigType();

    /**
     * Accept the configuration information to define behaviors in processing.
     *
     * @param ruleConfig the configuration
     * @return itself
     */
    Surveyor acceptConfiguration(@NotNull RuleConfig ruleConfig);

    /**
     * Start surveyor processing.
     *
     * @param params the values to be surveyed.
     * @return true if survey is successful for all params, otherwise false
     */
    boolean process(@NotNull List<Object> params);

    default boolean process(Object... params) {
        return process(List.of(params));
    }

    /**
     * Provide an empty implementation for <code>close</code> method from <code>Closeable</code>, in case some
     * implementation of this interface need to use some un-managed resources.
     *
     * @throws IOException follow parent's signature
     */
    @Override
    default void close() throws IOException {
        // do nothing by default.
    }
}
