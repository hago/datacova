/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 */

package com.hagoapp.datacova.worker.execution;

import com.hagoapp.datacova.lib.execution.ExecutionActionDetail;
import com.hagoapp.datacova.lib.execution.ExecutionDetail;
import com.hagoapp.datacova.lib.execution.TaskExecution;
import org.jetbrains.annotations.NotNull;

/**
 * The delegate interface to be invoked at specific moment by the worker.
 *
 * @author suncjs
 * @since 0.1
 */
public interface TaskExecutionWatcher {
    default void onStart(@NotNull TaskExecution te) {
    }

    default void onComplete(@NotNull TaskExecution te, @NotNull ExecutionDetail result) {
    }

    default void onError(@NotNull TaskExecution te, @NotNull Exception error) {
    }

    default void onActionStart(@NotNull TaskExecution te, int actionIndex) {
    }

    default void onActionComplete(@NotNull TaskExecution te, int actionIndex, @NotNull ExecutionActionDetail result) {
    }

    default void onActionError(@NotNull TaskExecution te, int actionIndex, @NotNull Exception error) {
    }

    default void onDataLoadStart(@NotNull TaskExecution te) {
    }

    default void onDataLoadComplete(@NotNull TaskExecution te, boolean isLoadingSuccessful) {
    }
}
