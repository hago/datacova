package com.hagoapp.datacova.command;

import com.hagoapp.datacova.CoVaException;
import picocli.CommandLine;

@CommandLine.Command(name = "stop", description = "stop service")
public class Stop extends CommandWithConfig {
    @Override
    public Integer call() throws CoVaException {
        return super.call();
    }
}
