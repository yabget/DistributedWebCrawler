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

    public CrawlPage(String page, int recursionDepth){
        this.page = page;
        this.recursionDepth = recursionDepth;
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
                    }
                    break;
                }
            }
            return;
        }
        System.out.println("[CRAWLED " + recursionDepth + "]\t" + page);
        //Add to graph all the out-in node relationships
        recursionDepth++;
        for(String outNode : HTMLParser.getInstance().getUnCrawledURLs(page)){
            worker.addToTasks(new CrawlPage(outNode, recursionDepth));
            worker.addFromToGraph(this.page, outNode);
        }
    }
}
