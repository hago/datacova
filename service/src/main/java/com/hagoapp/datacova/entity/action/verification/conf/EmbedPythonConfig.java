/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 */

package com.hagoapp.datacova.entity.action.verification.conf;

import com.hagoapp.datacova.CoVaException;
import com.hagoapp.datacova.entity.action.verification.Configuration;

import java.util.Locale;

public class EmbedPythonConfig extends Configuration {
    public static final int EMBED_Python_CONFIGURATION_TYPE = 7;
    private String snippet;

    public String getSnippet() {
        return snippet;
    }

    public void setSnippet(String snippet) {
        this.snippet = snippet;
    }

    public EmbedPythonConfig() {
        super();
        type = EMBED_Python_CONFIGURATION_TYPE;
        setFieldsCountLimit(-1);
    }

    @Override
    public boolean isValid() {
        return (snippet != null) && super.isValid();
    }

    @Override
    protected String createDescription(Locale locale) throws CoVaException {
        return super.createDescription(locale);
    }
}
