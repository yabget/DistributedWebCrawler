package Worker;

import Graph.Graph;
import Task.Task;
import harvester.PageCrawler;
import transport.TCPConnectionsCache;
import util.PrintHelper;
import util.Util;
import wireformats.CrawlerReportsSelfTasksFinished;
import wireformats.CrawlerReportsTasksNotFinished;
import wireformats.RelayURLToCrawl;

import java.util.Queue;

/**
 * Created by ydubale on 2/17/15.
 */
public class Worker implements Runnable {

    private Queue<Task> tasks;
    private Graph graph;
    private TCPConnectionsCache tcpConnectionsCache;
    private PageCrawler pageCrawler;

    public Worker(Queue<Task> tasks, Graph graph, TCPConnectionsCache tcpConnectionsCache, PageCrawler pageCrawler){
        this.tasks = tasks;
        this.graph = graph;
        this.tcpConnectionsCache = tcpConnectionsCache;
        this.pageCrawler = pageCrawler;
    }

    public String getRootURL(){
        return graph.getRootURL();
    }

    public void addToTasks(Task task){
        synchronized (tasks){
            tasks.offer(task);
            tasks.notify();
        }
    }

    public void incrementRelayedCount(){
        pageCrawler.incrementRelayedCount();
    }

    public void addPageToGraph(String page){
        graph.addNode(page);
    }

    public void addFromToGraph(String source, String dest){
        graph.addFromTo(source, dest);
    }

    public void relayToOtherCrawler(String crawlerURL, String urlToCrawl){
        RelayURLToCrawl relayURLToCrawl = new RelayURLToCrawl(urlToCrawl, this.getRootURL());
        tcpConnectionsCache.sendEvent(crawlerURL, relayURLToCrawl);
    }

    @Override
    public void run() {
        Task toDo;
        while(true){
            //System.out.print("I AM: " + Thread.currentThread().getId() + " ");
            Util.sleepSeconds(1, false);

            try {
                synchronized (tasks) {
                    toDo = tasks.poll();

                    if(toDo != null){
                        toDo.execute(this);
                        CrawlerReportsTasksNotFinished notFin = new CrawlerReportsTasksNotFinished(graph.getRootURL());
                        tcpConnectionsCache.sendToAll(notFin);
                        //System.out.println("WORKER - " + Thread.currentThread().getId() + " FINISHED! TASKS LEFT " + tasks.size());
                        continue;
                    }
                    if(tasks.isEmpty()){
                        tcpConnectionsCache.sendToAll(new CrawlerReportsSelfTasksFinished(graph.getRootURL()));
                    }
                    //System.out.println("SENT TO ALL! RelayedCount == " + pageCrawler.getRelayedCount() +
                            //"\tALL CRAWLERS FIN: " + pageCrawler.allOtherCrawlersFinished());
                    if(pageCrawler.getRelayedCount() == 0 && pageCrawler.allOtherCrawlersFinished()){
                        PrintHelper.printAlert("NICE! All other crawlers finished and I am finished with my tasks." +
                                " Goodbye.");
                        break;
                    }
                    //System.out.println("WORKER - " + Thread.currentThread().getId() +" WAITING FOR TASK " +tasks.size());

                    tasks.wait();
                }
                //System.out.println("WORKER - " + Thread.currentThread().getId() + " GOT NOTIFIED OF TASK " + tasks.size());
            } catch (InterruptedException e) {
                System.out.println("WORKER -  Interrupted while waiting on Task.");
            }

        }
    }

    public void sendRelayedTaskFinished(String toSendTo) {
        pageCrawler.sendRelayedTaskFinished(toSendTo);
    }
}
