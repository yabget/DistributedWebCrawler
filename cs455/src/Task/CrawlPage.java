package Task;

import Worker.Worker;
import util.HTMLParser;
import util.Util;

/**
 * Created by ydubale on 2/28/15.
 */

public class CrawlPage implements Task {

    private boolean isRelayedPacket;
    private int recursionDepth;

    private String relayerURL;
    private String page;

    public CrawlPage(String page, int recursionDepth, boolean isRelayedPacket, String relayerURL){
        this.page = page;
        this.recursionDepth = recursionDepth;
        this.isRelayedPacket = isRelayedPacket;
        this.relayerURL = relayerURL;
    }

    /**
     * This method executes the task
     * - Adds the page to the graph and populates the in/out edges
     * - Spawns child tasks if needed
     * - Relays page to other crawler if the page is not in the domain
     * @param worker - The current worker of this task
     */
    @Override
    public void execute(Worker worker) {
        worker.addPageToGraph(page);

        if(recursionDepth >= 5 ){
            // Don't add the links at the last depth to tasks, but add them to the graph
            for(String outNode : HTMLParser.getInstance().getUnCrawledURLs(page)){
                worker.addFromToGraph(this.page, outNode);
            }
            return;
        }

        if(!page.contains(worker.getRootURL())){ // Not my domain

            for(String otherCrawlerURL : Util.validRedirectDomains){
                if(page.contains(otherCrawlerURL)){
                    if(HTMLParser.getInstance().addToRelayedURLs(page)){ //True if page has not already been relayed
                        Util.printAlert("Sent " + page + " to " + otherCrawlerURL);
                        worker.relayToOtherCrawler(otherCrawlerURL, page);
                        worker.incrementRelayedCount();
                    }
                    break;
                }
            }
            return;
        }

        //System.out.println("[CRAWLED " + recursionDepth + "]\t" + page);

        if(isRelayedPacket){
            worker.sendRelayedTaskFinished(relayerURL);
        }

        //Add to graph all the out-in node relationships
        recursionDepth++;
        for(String outNode : HTMLParser.getInstance().getUnCrawledURLs(page)){
            worker.addToTasks(new CrawlPage(outNode, recursionDepth, false, null));
            worker.addFromToGraph(this.page, outNode);
        }
    }
}
