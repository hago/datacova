/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.utility.text

/**
 * Text utilities.
 *
 * @author suncjs
 * @since 0.5
 */
class TextUtils {
    companion object {
        @JvmStatic
        fun isHtml(input: String): Boolean {
            val singleQuoteStart = input.indexOf("'")
            val doubleQuoteStart = input.indexOf('"')
            val r = Regex("<[a-zA-Z\\\\]+>", setOf(RegexOption.DOT_MATCHES_ALL))
            val tagStart = r.find(input)?.range?.start ?: -1
            return when {
                tagStart < 0 -> false
                (singleQuoteStart < 0) && (doubleQuoteStart < 0) -> true
                singleQuoteStart < 0 -> doubleQuoteStart > tagStart
                doubleQuoteStart < 0 -> singleQuoteStart > tagStart
                singleQuoteStart > doubleQuoteStart -> doubleQuoteStart > tagStart
                else -> singleQuoteStart > tagStart
            }
        }
    }
}
