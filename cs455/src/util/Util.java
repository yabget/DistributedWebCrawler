package util;

import Graph.Node;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;

/**
 * Created by ydubale on 3/5/15.
 */
public final class Util {

    // Command line errors
    public static final String INVALID_PORT = "Invalid port range. Must be between 1024 - 65535.";
    public static final String INVALID_PORT_FORMAT = "Improper port number format.";
    public static final String INVALID_THREAD_POOL_SIZE = "Invalid thread pool size given.";
    public static final String COULD_NOT_READ_URL = "Problem during url extraction.";

    public static final String[] validRedirectDomains = {
            "http://www.bmb.colostate.edu",
            "http://www.biology.colostate.edu",
            "http://www.chem.colostate.edu",
            "http://www.cs.colostate.edu",
            "http://www.math.colostate.edu",
            "http://www.physics.colostate.edu",
            "http://www.colostate.edu/Depts/Psychology",
            "http://www.stat.colostate.edu"
    };

    public static void sleepSeconds(int seconds, boolean printHelp){
        try {
            if(printHelp){
                System.out.println("Sleeping for " + seconds + " seconds!");
            }

            for(int i=0; i< seconds; i++){
                if(printHelp){
                    System.out.println("Second " + (i + 1));
                }
                Thread.sleep(1000);
            }
        } catch (InterruptedException e) {
            System.out.println("Oh No! Somehow I have a problem sleeping for 20 seconds.");
        }
    }

    public static void writeNodesToFile(String fileName, Collection<Node> collection){
        try {
            File file = new File(fileName);
            file.getParentFile().mkdirs();
            file.createNewFile();
            //System.out.println("Created file: " + fileName);
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file, true));
            for(Node content : collection){
                bufferedWriter.write(content.getValue() +"\n");
            }
            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void writeStringsToFile(String fileName, Collection<String> collection){
        try {
            File file = new File(fileName);
            file.getParentFile().mkdirs();
            file.createNewFile();
            //System.out.println("Created file: " + fileName);
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file, true));
            for(String content : collection){
                bufferedWriter.write(content + "\n");
            }
            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String convertURLToDirectory(String dirURL){
        dirURL.replace("https://", "");
        dirURL = dirURL.replace("http://", "");

        dirURL = dirURL.replaceAll("/", "_");

        return dirURL;
    }

    public static void printSuccess(String success){
        System.out.println("[SUCCESS]: " + success);
    }

    public static void printAlert(String alert){
        System.out.println("[ALERT]: " + alert);
    }

    public static void printFail(String fail){
        System.out.println("[FAIL]: " + fail);
    }

    public static void printErrorExit(String error){
        System.out.println("[ERROR]: " + error);
        System.exit(1);
    }

}
