/*
 * Copyright (c) 2020.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 */

package com.hagoapp.datacova.command;

import com.hagoapp.datacova.utility.CoVaException;
import com.hagoapp.datacova.ShutDownManager;
import com.hagoapp.datacova.config.CoVaConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private final Logger logger = LoggerFactory.getLogger(Stop.class);

    @Override
    public Integer call() throws CoVaException {
        CoVaConfig.loadConfig(configFile);
        shutdownWeb();
        ShutDownManager.closeWatchers();
        return super.call();
    }

    private void shutdownWeb() {
        var config = CoVaConfig.getConfig().getWeb();
        if (config == null) {
            return;
        }
        var url = String.format("http://%s:%d/api/service/web/shutdown",
                config.getBindIp(), config.getPort());
        var http = HttpClient.newHttpClient();
        var request = HttpRequest.newBuilder(URI.create(url)).GET().build();
        try {
            var rsp = http.send(request, HttpResponse.BodyHandlers.ofString());
            logger.info("Notification of stop has been sent to service: {}", rsp.statusCode());
        } catch (IOException e) {
            logger.error("web service to stop failed: {}", e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
