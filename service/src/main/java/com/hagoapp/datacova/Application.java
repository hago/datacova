package com.hagoapp.datacova;

import com.hagoapp.datacova.command.Configure;
import com.hagoapp.datacova.command.Execute;
import com.hagoapp.datacova.command.Start;
import com.hagoapp.datacova.command.Stop;
import picocli.CommandLine;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The entry of application. Based on command line arguments, user can start / stop services or execute a task.
 */
@CommandLine.Command(name = "DataCoVa", version = "0.1", description = "Service of DataCoVa", subcommands = {
        Start.class, Stop.class, Execute.class, Configure.class
})
public class Application {

    private static Map<String, Object> internalData = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        Application app = new Application();
        CommandLine cli = new CommandLine(app);
        cli.setExecutionStrategy(app::executionStrategy).execute(args);
    }

    public static void setData(String key, Object data) {
        internalData.put(key, data);
    }

    public static Object getData(String key) {
        return internalData.get(key);
    }

    private int executionStrategy(CommandLine.ParseResult parseResult) {
        return new CommandLine.RunLast().execute(parseResult); // default execution strategy
    }
}
