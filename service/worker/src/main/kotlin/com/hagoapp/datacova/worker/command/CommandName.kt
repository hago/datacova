/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.worker.command

/**
 * An annotation for <code>Command</code> descendants.
 *
 * @param names names handled by the command
 * @param help help message for the command
 * @author suncjs
 * @since 0.5
 */
@Target(AnnotationTarget.CLASS)
@Retention()
annotation class CommandName(
    val names: Array<String>, val help: String = ""
)
