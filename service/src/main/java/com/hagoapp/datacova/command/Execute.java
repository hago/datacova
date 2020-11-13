package com.hagoapp.datacova.command;

import com.hagoapp.datacova.CoVaException;
import picocli.CommandLine;

@CommandLine.Command(name = "exec", description = "execute a task")
public class Execute extends CommandWithConfig {
    @Override
    public Integer call() throws CoVaException {
        return super.call();
    }
}
