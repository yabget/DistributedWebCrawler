package Task;

import Worker.Worker;
import util.PrintHelper;
import util.Storage;
import util.URLExtractor;

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
        if(recursionDepth >= 5){
            return;
        }

        String rootURL = worker.getRootURL();
        String currentPage = this.page;

        if(currentPage.startsWith("/")){
            currentPage = rootURL + currentPage;
        }

        if(worker.getGraph().isVisited(currentPage)){
            //todo: Do nothing??
            //System.out.println("[IGNORING] " + this.page);
            return;
        }

        worker.addPageToGraph(this.page); // Adds the page without prefix, for directory creation purposes

        if(!currentPage.contains(worker.getRootURL())){
            // Not the same domain
            //todo: Send to another node to process
            //System.out.println("NOT MY DOMAIN!!!!!!!" + currentPage);
            for(String otherCrawlerURL : Storage.validRedirectDomains){
                if(currentPage.contains(otherCrawlerURL)){
                    PrintHelper.printAlert("Sending: " + currentPage + " to crawler " + otherCrawlerURL);
                    worker.relayToOtherCrawler(otherCrawlerURL, currentPage);
                    break;
                }
            }
            return;
        }

        //Add to graph all the out-in node relationships
        recursionDepth++;
        for(String outNode : URLExtractor.parseURL(currentPage)){
            worker.addToTasks(new CrawlPage(outNode, recursionDepth));
            worker.addFromToGraph(this.page, outNode);
        }

        //System.out.println("[CRAWLED " + recursionDepth + "]\t" + currentPage);
        worker.addVisitedLink(currentPage);


    }
}
