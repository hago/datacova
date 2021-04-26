/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.execution.executor

import com.hagoapp.datacova.CoVaException
import com.hagoapp.datacova.entity.action.TaskAction
import com.hagoapp.datacova.entity.action.verification.TaskActionVerify
import com.hagoapp.datacova.execution.executor.validator.ValidatorFactory
import com.hagoapp.f2t.DataTable
import com.hagoapp.f2t.ProgressNotify
import com.hagoapp.f2t.datafile.ParseResult

class VerifyExecutor : BaseTaskActionExecutor(), ProgressNotify {
    override fun execute(action: TaskAction, data: DataTable) {
        if (action !is TaskActionVerify) {
            val ex = CoVaException("Not an TaskActionVerify: ${action.name}")
            watcher?.onError(action, ex)
            throw ex
        }
        val validators = action.configurations.map { conf -> ValidatorFactory.createValidator(conf) }
        data.rows.forEachIndexed { index, row ->
            validators.forEach { validator ->
                try {
                    validator.verify(row)
                } catch (e: Exception) {
                    TODO()
                }
            }
        }
    }

    override fun getActionType(): Int {
        return TaskActionVerify.TASK_ACTION_TYPE_VERIFY
    }

    override fun onStart() {
        TODO("Not yet implemented")
    }

    override fun onComplete(p0: ParseResult?) {
        TODO("Not yet implemented")
    }

    override fun onProgress(p0: Float) {
        TODO("Not yet implemented")
    }
}