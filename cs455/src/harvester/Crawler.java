package harvester;

import util.CommandLineParser;
import util.URLExtractor;

/**
 * Created by ydubale on 2/17/15.
 */
public class Crawler {


    public static void main(String[] args){
        CommandLineParser clp = new CommandLineParser(args);

        /*
        System.out.println("Port: " + clp.portNum);
        System.out.println("TP-s: " + clp.threadPoolSize);
        System.out.println("Root: " + clp.rootUrl);
        System.out.println("Path: " + clp.pathToConfigFile);
        */

        for(String url : URLExtractor.parseURL(clp.rootUrl)){
            System.out.println(url);
        }


    }

}
