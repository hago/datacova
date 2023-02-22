/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.web.websocket;

import io.vertx.core.http.ServerWebSocket;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface IMessageHandler {
    List<Integer> getHandledMessageTypes();

    ServerMessage handleMessage(@NotNull ServerWebSocket serverWebSocket, @NotNull ClientMessage message);

    Class<? extends ClientMessage> getMessageClass(int type);
}
