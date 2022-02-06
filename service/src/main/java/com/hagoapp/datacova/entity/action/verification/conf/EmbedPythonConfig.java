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
import com.hagoapp.datacova.util.text.TextResourceManager;
import org.stringtemplate.v4.ST;

import java.util.List;
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
        String format = TextResourceManager.getManager().getString(locale, "/validators/pythonscript");
        if (format == null) {
            throw new CoVaException("Description for EmbedPythonConfig class not found");
        }
        List<String> fields = getFields();
        if (fields.size() == 0) {
            throw new CoVaException("No fields defined in EmbedPythonConfig class");
        }
        ST st = new ST(format);
        st.add("fields", fields);
        st.add("snippet", snippet);
        return st.render();
    }
}