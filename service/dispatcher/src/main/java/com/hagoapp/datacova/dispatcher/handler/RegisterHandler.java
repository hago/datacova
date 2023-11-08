/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.dispatcher.handler;

import com.hagoapp.datacova.dispatcher.Application;
import com.hagoapp.datacova.dispatcher.ClientMessageHandler;
import com.hagoapp.datacova.message.RegisterMessage;
import com.hagoapp.datacova.message.RegisterResponseMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

/**
 * The handler to accept registration from web socket client, which is a worker.
 *
 * @author suncjs
 * @since 0.5
 */
public class RegisterHandler implements ClientMessageHandler.MessageHandler {

    private final Logger logger = LoggerFactory.getLogger(RegisterHandler.class);
    @Override
    public Object handle(Object message) {
        if (message.getClass() != RegisterMessage.class) {
            throw new UnsupportedOperationException("Not a register message!");
        }
        var reg = (RegisterMessage) message;
        var groupKeys = Application.getConfig().getAuthKeys();
        if (groupKeys.stream().noneMatch(gk ->
                gk.getGroup().equals(reg.getGroup()) && gk.getAuthKey().equals(reg.getAuthKey()))) {
            return new RegisterResponseMessage(false, "access denied");
        }
        var name = reg.getName() != null ? reg.getName() : UUID.randomUUID().toString();
        logger.info("Worker {} registered in group {}", name, reg.getGroup());
        return new RegisterResponseMessage(true, name);
    }
}
