/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 */

package com.hagoapp.datacova.dispatcher.handler;

import com.hagoapp.datacova.dispatcher.Application;
import com.hagoapp.datacova.dispatcher.ClientMessageHandler;
import com.hagoapp.datacova.dispatcher.server.ServerState;
import com.hagoapp.datacova.dispatcher.server.WorkerSpeaker;
import com.hagoapp.datacova.lib.data.TaskExecutionData;
import com.hagoapp.datacova.lib.execution.ExecutionDetail;
import com.hagoapp.datacova.lib.execution.TaskExecution;
import com.hagoapp.datacova.message.WorkerDoneMessage;
import com.hagoapp.datacova.utility.CoVaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The message handler for the message that notifies a task execution is done running.
 *
 * @author suncjs
 * @since 0.5
 */
public class WorkerDoneHandler implements ClientMessageHandler.MessageHandler {

    private static final Logger logger = LoggerFactory.getLogger(WorkerDoneHandler.class);

    @Override
    public Object handle(WorkerSpeaker speaker, Object message) {
        if (!(message instanceof WorkerDoneMessage)) {
            throw new UnsupportedOperationException("Not a worker done message!");
        }
        var msg = (WorkerDoneMessage) message;
        try {
            var te = TaskExecution.loadFromJson(msg.getTaskExecutionJson());
            logger.debug("done message for {} received", te.getId());
            var result = ExecutionDetail.fromString(msg.getResultJson());
            ServerState.INSTANCE.jobDone(te);
            try (var db = new TaskExecutionData(Application.getConfig().getDb())) {
                db.completeTaskExecution(result);
            }
        } catch (CoVaException ex) {
            logger.error("invalid task execution in worker done message: {}", msg.getTaskExecutionJson());
        }
        return null;
    }
}
