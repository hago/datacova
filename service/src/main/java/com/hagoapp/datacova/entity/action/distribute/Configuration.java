/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.entity.action.distribute;

import com.hagoapp.datacova.CoVaException;
import com.hagoapp.datacova.JsonStringify;

/**
 * Configuration for distribution.
 */
public class Configuration implements JsonStringify {
    protected String type;
    protected boolean copyOriginal = false;
    protected boolean overwriteExisted = false;
    protected String targetFileName;

    public String getType() {
        return type;
    }

    public boolean isCopyOriginal() {
        return copyOriginal;
    }

    public void setCopyOriginal(boolean copyOriginal) {
        this.copyOriginal = copyOriginal;
    }

    public boolean isOverwriteExisted() {
        return overwriteExisted;
    }

    public void setOverwriteExisted(boolean overwriteExisted) {
        this.overwriteExisted = overwriteExisted;
    }

    public void checkValidity() throws CoVaException {
        //
    }

    public String getTargetFileName() {
        return targetFileName;
    }

    public void setTargetFileName(String targetFileName) {
        this.targetFileName = targetFileName;
    }
}
