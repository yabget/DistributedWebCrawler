package Task;

import Worker.Worker;
import util.HTMLParser;
import util.Storage;

/**
 * Created by ydubale on 2/28/15.
 */

public class CrawlPage implements Task {

    private String page;
    private int recursionDepth;
    private boolean isRelayedPacket;
    private String relayerURL;

    public CrawlPage(String page, int recursionDepth, boolean isRelayedPacket, String relayerURL){
        this.page = page;
        this.recursionDepth = recursionDepth;
        this.isRelayedPacket = isRelayedPacket;
        this.relayerURL = relayerURL;
    }

    @Override
    public void execute(Worker worker) {
        if(recursionDepth > 5){
            return;
        }

        worker.addPageToGraph(page); // Adds the page without prefix, for directory creation purposes

        if(!page.contains(worker.getRootURL())){
            // Not the same domain
            for(String otherCrawlerURL : Storage.validRedirectDomains){
                if(page.contains(otherCrawlerURL)){
                    if(HTMLParser.getInstance().addToRelayedURLs(page)){
                        //PrintHelper.printAlert("CrawlPage - Sending: [" + page + "] to crawler " + otherCrawlerURL);
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
            worker.sendRelayedTaskFinished(relayerURL, page);
        }

        //Add to graph all the out-in node relationships
        recursionDepth++;
        for(String outNode : HTMLParser.getInstance().getUnCrawledURLs(page)){
            worker.addToTasks(new CrawlPage(outNode, recursionDepth, false, null));
            worker.addFromToGraph(this.page, outNode);
        }
    }
}
