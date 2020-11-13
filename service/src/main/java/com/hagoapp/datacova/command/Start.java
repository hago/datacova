package com.hagoapp.datacova.command;

import com.hagoapp.datacova.CoVaException;
import picocli.CommandLine;

@CommandLine.Command(name = "start", description = "start service")
public class Start extends CommandWithConfig {
    @Override
    public Integer call() throws CoVaException {
        return super.call();
    }
}
