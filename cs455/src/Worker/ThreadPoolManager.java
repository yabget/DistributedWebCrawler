package Worker;

import Graph.Graph;
import Task.CrawlPage;
import Task.Task;
import harvester.PageCrawler;
import transport.TCPConnectionsCache;
import util.HTMLParser;
import util.PrintHelper;

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
            //System.out.println("THEADPOOLMANAGER - NEW TASK ADDED TO QUEUE: " + tasks.size());
            tasks.notify();
            //System.out.println("THEADPOOLMANAGER - SENT NOTIFIED: " + tasks.size());
        }
    }

    public void startWorkers(){
        for(Thread thread : threadpool){
            thread.start();
        }
        PrintHelper.printAlert("THEADPOOLMANAGER - Started all threads");
        try {
            for(Thread thread : threadpool){
                //System.out.println("Thread waiting for join " + thread.getId());
                thread.join();
                //System.out.println("Thread Joined " + thread.getId());
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /*
    @Override
    public void run() {



        while(true){
            if(!tasks.isEmpty()){
                tasks.notify();
            }
        }

        while (true){
            Util.sleepSeconds(5);
            synchronized (tasks){
                if(tasks.isEmpty()){
                    try {
                        System.out.println("THEADPOOLMANAGER - Task queue is empty. " + tasks.size());
                        tcpConnectionsCache.sendToAll(new CrawlerReportsSelfTasksFinished(graph.getRootURL()));
                        System.out.println("THEADPOOLMANAGER - TOLD ALL NODES I'M DONE WITH TASKS");
                        if(pageCrawler.getRelayedCount() == 0 && pageCrawler.allOtherCrawlersFinished()){
                            PrintHelper.printAlert("NICE! All other crawlers finished and I am finished with my tasks." +
                                    " Goodbye.");
                            break;
                        }
                        System.out.println("THEADPOOLMANAGER - SITTING AND WAITING FOR TASK NOTIFY");
                        tasks.wait();
                        System.out.println("THEADPOOLMANAGER - NOTIFY OCCURED! NOTIFY OCCURED!");
                    } catch (InterruptedException e) {
                        System.out.println("THEADPOOLMANAGER - Interrupted while waiting on Task.");
                    }
                }else{
                    System.out.println("THEADPOOLMANAGER - TASK IS NOT EMPTY! " + tasks.size());
                    tasks.notify();
                    System.out.println("THEADPOOLMANAGER - NOTIFIED SOMEONE TO WORK ON TASK " + tasks.size());
                    Util.sleepSeconds(5);
                    System.out.println("THEADPOOLMANAGER - TELLING OTHER NODES I'M NOT FIN " + tasks.size());
                    CrawlerReportsTasksNotFinished notFin = new CrawlerReportsTasksNotFinished(graph.getRootURL());
                    tcpConnectionsCache.sendToAll(notFin);
                    System.out.println("THEADPOOLMANAGER - TOLD OTHER NODES I'M NOT FIN " + tasks.size());
                }
            }
        }
    }
    */

    public void notifyTaskPool() {
        synchronized (tasks){
            //System.out.println("THEADPOOLMANAGER - NOTIFYING TASK POOL METHOD: " + tasks.size());
            tasks.notifyAll();
        }
    }
}
