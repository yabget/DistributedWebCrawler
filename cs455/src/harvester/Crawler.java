package harvester;

import util.CommandLineParser;
import util.PrintHelper;
import util.Util;

import java.io.IOException;

/**
 * Created by ydubale on 2/17/15.
 */
public class Crawler {

    protected final String hostName;
    protected final int port;
    protected final String rootURL;

    public Crawler(String hostName, int portNum, String rootURL){
        this.rootURL = rootURL;
        this.port = portNum;
        this.hostName = hostName;
    }

    public String toString(){
        return this.hostName + "\t\t" + this.port + "\t\t" + this.rootURL;
    }


    public static void main(String[] args){
        CommandLineParser clp = new CommandLineParser(args);

        System.out.println("Port: " + clp.portNum);
        System.out.println("TP-s: " + clp.threadPoolSize);
        System.out.println("Root: " + clp.rootUrl);
        System.out.println("Path: " + clp.pathToConfigFile);
        System.out.println();

        try {
            PageCrawler crawler = new PageCrawler(clp.threadPoolSize, clp.rootUrl, clp.portNum, clp.getCrawlers());
            crawler.startServerThread();
            //Wait for all servers to start on other machines
            Util.sleepSeconds(10);
            crawler.setupConnectionWithOtherCrawlers();
            //Wait to make sure all connections have been made
            Util.sleepSeconds(10);
            crawler.startThreadPoolManager();

        } catch (IOException e) {
            //e.printStackTrace();
            PrintHelper.printErrorExit("Could not start server on " + clp.portNum);
        }
        //crawler.initializeDirectories();
    }
}
