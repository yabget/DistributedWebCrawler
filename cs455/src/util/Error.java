package util;

/**
 * Created by ydubale on 2/17/15.
 */
public final class Error {

    // Command line errors
    public static final String INVALID_PORT = "Invalid port range. Must be between 1024 - 65535.";
    public static final String INVALID_PORT_FORMAT = "Improper port number format.";
    public static final String INVALID_THREAD_POOL_SIZE = "Invalid thread pool size given.";

    // URLExtractor errors
    public static final String COULD_NOT_READ_URL = "Problem during url extraction.";


    public static void printErrorExit(String error){
        System.out.println("[ERROR]: " + error);
        System.exit(1);
    }

}
