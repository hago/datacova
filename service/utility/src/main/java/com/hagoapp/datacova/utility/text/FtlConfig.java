/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.utility.text;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Configuration to define where to load template files(using freemarker).
 *
 * @author suncjs
 * @since 0.1
 */
public class FtlConfig {
    private String directory;
    private boolean useResource = false;
    private final Map<String, String> aliases = new HashMap<>();

    public String getDirectory() {
        return directory;
    }

    public void setDirectory(String directory) {
        this.directory = directory;
    }

    public boolean isUseResource() {
        return useResource;
    }

    public void setUseResource(boolean useResource) {
        this.useResource = useResource;
    }

    public Map<String, String> getAliases() {
        return aliases;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FtlConfig that = (FtlConfig) o;

        if (useResource != that.useResource) return false;
        if (!Objects.equals(directory, that.directory)) return false;
        return aliases.equals(that.aliases);
    }

    @Override
    public int hashCode() {
        int result = directory != null ? directory.hashCode() : 0;
        result = 31 * result + (useResource ? 1 : 0);
        result = 31 * result + aliases.hashCode();
        return result;
    }
}
