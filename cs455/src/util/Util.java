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
                bufferedWriter.write(content.getValue());
            }
            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
