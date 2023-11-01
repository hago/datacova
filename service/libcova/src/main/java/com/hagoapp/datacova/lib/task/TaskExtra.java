/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.lib.task;

import com.google.gson.GsonBuilder;
import com.hagoapp.datacova.JsonStringify;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TaskExtra implements JsonStringify {
    protected String tag = "";
    protected Locale locale = Locale.getDefault();
    protected final List<String> mailRecipients = new ArrayList<>();
    protected final List<String> mailCCRecipients = new ArrayList<>();
    protected final List<String> mailBCCRecipients = new ArrayList<>();

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public Locale getLocale() {
        return locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    public List<String> getMailRecipients() {
        return mailRecipients;
    }

    public List<String> getMailCCRecipients() {
        return mailCCRecipients;
    }

    public List<String> getMailBCCRecipients() {
        return mailBCCRecipients;
    }

    public static TaskExtra fromJson(String s) {
        return new GsonBuilder().create().fromJson(s, TaskExtra.class);
    }

}
