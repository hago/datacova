/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.file;

import org.jetbrains.annotations.NotNull;

/**
 * Config file for a FileStore type.
 *
 * @author suncjs
 * @since 0.5
 */
public abstract class FsConfig {
    public String createConnectString() {
        var anno = this.getClass().getAnnotation(FsScheme.class);
        if (anno == null) {
            throw new UnsupportedOperationException("Not annotated, can't find scheme");
        }
        return String.format("%s:%s", anno.name(), serialize());
    }

    protected abstract String serialize();

    public abstract void loadConnectionString(@NotNull String input);
}
