package util;

import harvester.Crawler;

import java.io.*;

/**
 * Created by ydubale on 2/17/15.
 */
public class CommandLineParser {

    private static final int MIN_VALID_PORT = 1024;
    private static final int MAX_VALID_PORT = 65535;

    public static int portNum;
    public static int threadPoolSize;
    public static String rootUrl;
    public static String pathToConfigFile;
    public static Crawler[] crawlers;

    /**
     * Expected input
     * cs455.harvester.Crawler portnum thread-pool-size root-url path-to-config-file
     * @param args - list of arguments
     */
    public CommandLineParser(String args[]) {
        this.portNum = parsePort(args[0]);
        parseThreadPoolSize(args[1]);
        parseRootUrl(args[2]);
        this.crawlers = parseConfigFile(args[3]);
    }

    public Crawler[] getCrawlers(){
        return crawlers;
    }

    /**
     * @param pathToConfigFile
     */
    private Crawler[] parseConfigFile(String pathToConfigFile) {
        this.pathToConfigFile = pathToConfigFile;
        File file = new File(pathToConfigFile);
        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new FileReader(file));
        } catch (FileNotFoundException e) {
            Util.printFail("configFile not found!");
        }

        Crawler[] crawlers = new Crawler[8];

        String line;
        int count = 0;
        try {
            while((line = bufferedReader.readLine()) != null){
                crawlers[count] = extractCrawler(line);
                count++;
            }
        } catch (IOException e) {
            Util.printFail("CommandLineParser - parsing file config file");
        }

        return crawlers;
    }

    private Crawler extractCrawler(String line) throws IOException{
        String[] hostportURL = line.split(":");

        String hostName = hostportURL[0];

        String[] portURL = hostportURL[1].split(",");

        int port = parsePort(portURL[0]);
        String url = portURL[1] + ":" +hostportURL[2];

        return new Crawler(hostName, port, url);
    }

    /**
     * @param rootUrl
     */
    private void parseRootUrl(String rootUrl) {
        this.rootUrl = rootUrl;
    }

    /**
     * @param threadPoolSizeString
     */
    private void parseThreadPoolSize(String threadPoolSizeString) {
        try {
            this.threadPoolSize = Integer.parseInt(threadPoolSizeString);
        }
        catch (NumberFormatException notValidNumber){
            Util.printErrorExit(Util.INVALID_THREAD_POOL_SIZE);
        }
    }

    private int parsePort(String portString) {
        try{
            int portNum = Integer.parseInt(portString);

            if(portNum < MIN_VALID_PORT || portNum > MAX_VALID_PORT){
                Util.printErrorExit(Util.INVALID_PORT);
            }
            return portNum;
        }
        catch (NumberFormatException notValidPort){
            Util.printErrorExit(Util.INVALID_PORT_FORMAT);
        }
        return -1;
    }

}
