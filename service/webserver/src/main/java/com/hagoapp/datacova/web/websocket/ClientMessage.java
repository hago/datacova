/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.web.websocket;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

public class ClientMessage {
    protected int messageType;
    protected int id;

    public int getMessageType() {
        return messageType;
    }

    public void setMessageType(int messageType) {
        this.messageType = messageType;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public static ClientMessage fromJson(String text) {
        try {
            return new Gson().fromJson(text, ClientMessage.class);
        } catch (JsonSyntaxException e) {
            return null;
        }
    }
}
