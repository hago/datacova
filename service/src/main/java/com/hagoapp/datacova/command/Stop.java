/*
 * Copyright (c) 2020.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 */

package com.hagoapp.datacova.command;

import com.hagoapp.datacova.CoVaException;
import com.hagoapp.datacova.CoVaLogger;
import com.hagoapp.datacova.config.CoVaConfig;
import org.slf4j.Logger;
import picocli.CommandLine;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@CommandLine.Command(name = "stop", description = "stop service")
public class Stop extends CommandWithConfig {

    @CommandLine.Option(names = {"-f", "--force"}, description = "force service to stop", arity = "0")
    private boolean force = false;

    private final Logger logger = CoVaLogger.getLogger();

    @Override
    public Integer call() throws CoVaException {
        var config = CoVaConfig.getConfig().getWeb();
        var url = String.format("http://%s:%d/api/service/shutdown%s",
                config.getBindIp(), config.getPort(), force ? "/force" : "");
        var http = HttpClient.newHttpClient();
        var request = HttpRequest.newBuilder(URI.create(url)).GET().build();
        try {
            http.send(request, HttpResponse.BodyHandlers.ofString());
            logger.info("Notification of stop has been sent to service");
        } catch (IOException | InterruptedException e) {
            logger.error("Messaging service to stop failed: {}", e.getMessage());
        }
        return super.call();
    }
}
