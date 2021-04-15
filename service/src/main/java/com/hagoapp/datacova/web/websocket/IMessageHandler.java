/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.web.websocket;

import io.vertx.core.http.ServerWebSocket;

import java.util.List;

public interface IMessageHandler {
    List<Integer> getHandledMessageTypes();
    ServerMessage handleMessage(ServerWebSocket serverWebSocket, ClientMessage message);
    Class<? extends ClientMessage> getMessageType(int type);
}
