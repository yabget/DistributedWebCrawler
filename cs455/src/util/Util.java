package util;

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

}
