package Worker;

import Graph.Graph;
import Task.CrawlPage;
import Task.Task;
import harvester.PageCrawler;
import transport.TCPConnectionsCache;
import util.HTMLParser;
import util.Util;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by ydubale on 3/1/15.
 */
public class ThreadPoolManager {

    private Queue<Task> tasks;
    private Queue<Thread> threadpool;
    private TCPConnectionsCache tcpConnectionsCache;
    private Graph graph;

    public ThreadPoolManager(int threadPoolSize, String rootURL, Graph graph,
                             TCPConnectionsCache tcpConnectionsCache, PageCrawler pageCrawler) {
        threadpool = new LinkedList<Thread>();
        tasks = new LinkedList<Task>();

        this.tcpConnectionsCache = tcpConnectionsCache;
        this.graph = graph;

        for(int i = 0; i < threadPoolSize; i++){
            threadpool.add(new Thread(new Worker(tasks, this.graph, this.tcpConnectionsCache, pageCrawler)));
        }

        tasks.offer(new CrawlPage(rootURL, 1, false, null)); //Add the root url to be crawled
        HTMLParser.getInstance().addToCrawledURLs(rootURL);

    }

    public void addToTask(Task task){
        synchronized (tasks){
            tasks.offer(task);
            tasks.notify();
        }
    }

    public void startWorkers(){
        for(Thread thread : threadpool){
            thread.start();
        }
        Util.printAlert("THEADPOOLMANAGER - Started all threads");
        try {
            for(Thread thread : threadpool){
                thread.join();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void notifyTaskPool() {
        synchronized (tasks){
            tasks.notifyAll();
        }
    }
}
