/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.web.websocket;

import com.hagoapp.datacova.user.UserInfo;
import io.vertx.core.http.ServerWebSocket;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

public class WebSocketManager {

    private final static WebSocketManager instance = new WebSocketManager();

    private WebSocketManager() {

    }

    public static WebSocketManager getManager() {
        return instance;
    }

    private final ConcurrentHashMap<UserInfo, List<ServerWebSocket>> userMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<ServerWebSocket, UserInfo> socketMap = new ConcurrentHashMap<>();

    public void addUserSession(UserInfo userInfo, ServerWebSocket socket) {
        userMap.compute(userInfo, (key, existing) -> {
            if (existing == null) {
                var newValue = new ArrayList<ServerWebSocket>();
                newValue.add(socket);
                return newValue;
            } else {
                existing.add(socket);
                return existing;
            }
        });
        socketMap.put(socket, userInfo);
    }

    public void removeUserSessions(UserInfo userInfo) {
        List<ServerWebSocket> sockets = userMap.remove(userInfo);
        if (sockets != null) {
            sockets.forEach(socketMap::remove);
        }
    }

    public List<ServerWebSocket> getUserSessions(UserInfo userInfo) {
        return userMap.get(userInfo);
    }

    public UserInfo getUser(ServerWebSocket webSocket) {
        return socketMap.get(webSocket);
    }

    public void removeSession(ServerWebSocket webSocket) {
        UserInfo userInfo = socketMap.remove(webSocket);
        if (userInfo != null) {
            userMap.compute(userInfo, (key, existing) -> {
                if (existing == null) {
                    return null;
                } else {
                    existing.remove(webSocket);
                    return existing;
                }
            });
        }
    }

    public void broadcastMessage(ServerMessage message) {
        String text = message.toJson();
        socketMap.forEach((key, value) -> key.writeTextMessage(text));
    }

    public void broadcastMessage(ServerMessage message, Predicate<UserInfo> predicate) {
        String text = message.toJson();
        userMap.keySet().stream().filter(predicate).forEach(user ->
                userMap.get(user).forEach(socket -> socket.writeTextMessage(text))
        );
    }

    public void sendUserMessage(ServerMessage message, UserInfo user) {
        String text = message.toJson();
        sendUserMessage(text, user);
    }

    public void sendUserMessage(String rawMessage, UserInfo user) {
        var sockets = userMap.get(user);
        if (sockets != null) {
            sockets.forEach(socket -> socket.writeTextMessage(rawMessage));
        }
    }
}
