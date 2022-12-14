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
//import java.util.List;
//import java.util.Locale;
//import java.util.regex.Pattern;
//import java.util.regex.PatternSyntaxException;
//
//public class RegexConfig extends Configuration {
//    public static final int REGEX_CONFIGURATION_TYPE = 1;
//    private String pattern;
//    private boolean ignoreCase = true;
//    private boolean dotAll = false;
//    private boolean allowEmpty = false;
//
//    public RegexConfig() {
//        super();
//        type = REGEX_CONFIGURATION_TYPE;
//    }
//
//    public String getPattern() {
//        return pattern;
//    }
//
//    public boolean isIgnoreCase() {
//        return ignoreCase;
//    }
//
//    public boolean isDotAll() {
//        return dotAll;
//    }
//
//    public boolean isAllowEmpty() {
//        return allowEmpty;
//    }
//
//    public void setPattern(String pattern) {
//        this.pattern = pattern;
//    }
//
//    public void setIgnoreCase(boolean ignoreCase) {
//        this.ignoreCase = ignoreCase;
//    }
//
//    public void setDotAll(boolean dotAll) {
//        this.dotAll = dotAll;
//    }
//
//    public void setAllowEmpty(boolean allowEmpty) {
//        this.allowEmpty = allowEmpty;
//    }
//
//    @Override
//    public boolean isValid() {
//        if (pattern == null) {
//            return false;
//        }
//        try {
//            int flag = ignoreCase ? Pattern.CASE_INSENSITIVE : 0;
//            flag += dotAll ? Pattern.DOTALL : 0;
//            Pattern.compile(pattern, flag);
//            return super.isValid();
//        } catch (PatternSyntaxException ex) {
//            return false;
//        }
//    }
//
//    @Override
//    protected String createDescription(Locale locale) throws CoVaException {
//        String format = TextResourceManager.getManager().getString(locale, "/validators/regex");
//        if (format == null) {
//            throw new CoVaException("Description for RegexConfig class not found");
//        }
//        List<String> fields = getFields();
//        return String.format(format, fields.size() > 0 ? fields.get(0) : "", pattern);
//    }
//}
