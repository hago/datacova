/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.dispatcher.handler;

import com.hagoapp.datacova.dispatcher.ClientMessageHandler;
import com.hagoapp.datacova.dispatcher.server.WorkerSpeaker;
import com.hagoapp.datacova.message.HeartBeatResponseMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The message handler for the response message for a heartbeat sent earlier.
 *
 * @author suncjs
 * @since 0.5
 */
public class HeartbeatResponseHandler implements ClientMessageHandler.MessageHandler {
    private final Logger logger = LoggerFactory.getLogger(HeartbeatResponseHandler.class);

    @Override
    public Object handle(WorkerSpeaker speaker, Object message) {
        if (!(message instanceof HeartBeatResponseMessage)) {
            logger.error("Not a HeartBeatResponseMessage!!");
            return null;
        }
        var hbMsg = (HeartBeatResponseMessage) message;
        speaker.heartbeatResponded(hbMsg);
        return null;
    }
}
