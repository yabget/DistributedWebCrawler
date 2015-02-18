package util;

/**
 * Created by ydubale on 2/17/15.
 */
public class CommandLineParser {

    private static final String INVALID_PORT = "Invalid port range. Must be between 1024 - 65535";
    private static final String INVALID_PORT_FORMAT = "Improper port number format.";
    private static final String INVALID_THREAD_POOL_SIZE = "Invalid thread pool size given.";

    private static final int MIN_VALID_PORT = 1024;
    private static final int MAX_VALID_PORT = 65535;

    public static int portNum;
    public static int threadPoolSize;
    public static String rootUrl;
    public static String pathToConfigFile;

    /**
     * Expected input
     * cs455.harvester.Crawler portnum thread-pool-size root-url path-to-config-file
     * @param args - list of arguments
     */
    public CommandLineParser(String args[]) {
        parsePort(args[0]);
        parseThreadPoolSize(args[1]);
        parseRootUrl(args[2]);
        parsePathToConfigFile(args[3]);
    }

    /**
     * TODO: What is a valid path? Will it include fileName?
     * @param pathToConfigFile
     */
    private void parsePathToConfigFile(String pathToConfigFile) {
        this.pathToConfigFile = pathToConfigFile;
    }

    /**
     * TODO: What format is accepted?
     * @param rootUrl
     */
    private void parseRootUrl(String rootUrl) {
        this.rootUrl = rootUrl;
    }

    /**
     * TODO: What is an upper limit to thread pool size?
     * @param threadPoolSizeString
     */
    private void parseThreadPoolSize(String threadPoolSizeString) {
        try {
            this.threadPoolSize = Integer.parseInt(threadPoolSizeString);
        }
        catch (NumberFormatException notValidNumber){
            printErrorExit(INVALID_THREAD_POOL_SIZE);
        }
    }

    private void printErrorExit(String error){
        System.out.println("[ERROR]: " + error);
        System.exit(1);
    }

    private void parsePort(String portString) {
        try{
            int portNum = Integer.parseInt(portString);

            if(portNum < MIN_VALID_PORT || portNum > MAX_VALID_PORT){
                printErrorExit(INVALID_PORT);
            }
            this.portNum = portNum;
        }
        catch (NumberFormatException notValidPort){
            printErrorExit(INVALID_PORT_FORMAT);
        }
    }

}
