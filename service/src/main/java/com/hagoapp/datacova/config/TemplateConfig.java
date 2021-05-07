/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 */

package com.hagoapp.datacova.config;

import java.util.HashMap;
import java.util.Map;

public class TemplateConfig {
    private String directory;

    private final Map<String, String> aliases = new HashMap<>();

    public String getDirectory() {
        return directory;
    }

    public void setDirectory(String directory) {
        this.directory = directory;
    }

    public Map<String, String> getAliases() {
        return aliases;
    }
}
