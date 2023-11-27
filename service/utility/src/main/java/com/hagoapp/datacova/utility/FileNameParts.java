/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.utility;

import java.util.Objects;

/**
 * Each parts of a file name.
 *
 * @author suncjs
 * @since 0.1
 */
public class FileNameParts {
    private String path;
    private String name;
    private String ext;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getExt() {
        return ext;
    }

    public void setExt(String ext) {
        this.ext = ext;
    }

    public FileNameParts(String path, String name, String ext) {
        this.path = path;
        this.name = name;
        this.ext = ext;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FileNameParts that = (FileNameParts) o;

        if (!Objects.equals(path, that.path)) return false;
        if (!Objects.equals(name, that.name)) return false;
        return Objects.equals(ext, that.ext);
    }

    @Override
    public int hashCode() {
        int result = path != null ? path.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (ext != null ? ext.hashCode() : 0);
        return result;
    }

    public String nameWithExt() {
        return String.format("%s.%s", name, ext.isBlank() ? "" : ext);
    }
}
