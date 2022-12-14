///*
// * Copyright (c) 2021.
// * This Source Code Form is subject to the terms of the Mozilla Public
// * License, v. 2.0. If a copy of the MPL was not distributed with this
// * file, You can obtain one at https://mozilla.org/MPL/2.0/.
// *
// */
//
//package com.hagoapp.datacova.verification.conf;
//
//import com.hagoapp.datacova.CoVaException;
//import com.hagoapp.datacova.verification.Configuration;
//import com.hagoapp.datacova.util.text.TextResourceManager;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Locale;
//import java.util.stream.Collectors;
//
//public class OptionsConfig extends Configuration {
//    public static final int OPTIONS_CONFIG_TYPE = 3;
//    private final List<String> options = new ArrayList<>();
//    private boolean ignoreCase = false;
//    private boolean allowEmpty = false;
//
//    public OptionsConfig() {
//        type = OPTIONS_CONFIG_TYPE;
//    }
//
//    public List<String> getOptions() {
//        return options;
//    }
//
//    public boolean isAllowEmpty() {
//        return allowEmpty;
//    }
//
//    public boolean isIgnoreCase() {
//        return ignoreCase;
//    }
//
//    public void setIgnoreCase(boolean ignoreCase) {
//        this.ignoreCase = ignoreCase;
//    }
//
//    public void setAllowEmpty(boolean allowEmpty) {
//        this.allowEmpty = allowEmpty;
//    }
//
//    public boolean isAnOption(String s) {
//        if ((s == null) || allowEmpty) {
//            return options.contains(null);
//        }
//        if (!ignoreCase) {
//            return this.options.contains(s);
//        } else {
//            return this.options.stream().anyMatch(item -> item != null && item.compareToIgnoreCase(s) == 0);
//        }
//    }
//
//    @Override
//    public boolean isValid() {
//        if (options.size() == 0) {
//            return false;
//        }
//        long uniqueSize = ignoreCase ?
//                options.stream().map(String::toLowerCase).distinct().count() :
//                options.stream().distinct().count();
//        return uniqueSize == options.size() && super.isValid();
//    }
//
//    @Override
//    protected String createDescription(Locale locale) throws CoVaException {
//        String format = TextResourceManager.getManager().getString(locale, "/validators/options");
//        if (format == null) {
//            throw new CoVaException("Description for OptionsConfig class not found");
//        }
//        List<String> fields = getFields();
//        if (fields.size() == 0) {
//            throw new CoVaException("No fields defined in OptionsConfig class");
//        }
//        var fieldStr = fields.stream().map(f -> String.format("\"%s\"", f))
//                .collect(Collectors.joining(", "));
//        var optionStr = options.stream().map(f -> String.format("\"%s\"", f))
//                .collect(Collectors.joining(", "));
//        return String.format(format, fieldStr, optionStr);
//    }
//}
