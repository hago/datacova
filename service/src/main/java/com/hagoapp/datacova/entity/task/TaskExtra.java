package com.hagoapp.datacova.entity.task;

import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TaskExtra {
    private String tag = "";
    private Locale locale = Locale.getDefault();
    private List<String> mailRecipients = new ArrayList<>();
    private List<String> mailCCRecipients = new ArrayList<>();
    private List<String> mailBCCRecipients = new ArrayList<>();

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

    public String toString() {
        return new GsonBuilder().serializeNulls().setPrettyPrinting().create().toJson(this);
    }
}
