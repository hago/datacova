package com.hagoapp.datacova.command;

import com.hagoapp.datacova.CoVaException;
import com.hagoapp.datacova.CoVaLogger;
import com.hagoapp.datacova.config.CoVaConfig;
import com.hagoapp.datacova.execution.Service;
import com.hagoapp.datacova.web.WebManager;
import org.apache.logging.log4j.Logger;
import picocli.CommandLine;

import java.util.List;

@CommandLine.Command(name = "start", description = "start service")
public class Start extends CommandWithConfig {

    @CommandLine.Option(names = {"--config", "-c"},
            description = "specify config file, ./config.json by default ")
    private String configFile = "config.json";

    private Logger logger;

    @Override
    public Integer call() throws CoVaException {
        CoVaConfig.loadConfig(configFile);
        logger = CoVaLogger.getLogger();
        WebManager.getManager().createWebServer(CoVaConfig.getConfig().getWeb(), List.of(
                "com.hagoapp.datacova.web"
        ));
        logger.info("web server created");
        Service.Companion.getExecutor().startExecutionService();
        logger.info("builtin execution service created");
        return super.call();
    }
}
