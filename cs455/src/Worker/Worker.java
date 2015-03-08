package Worker;

import Graph.Graph;
import Task.Task;
import transport.TCPConnectionsCache;
import wireformats.RelayURLToCrawl;

import java.util.Queue;

/**
 * Created by ydubale on 2/17/15.
 */
public class Worker implements Runnable {

    private Queue<Task> tasks;
    private Graph graph;
    private TCPConnectionsCache tcpConnectionsCache;

    public Worker(Queue<Task> tasks, Graph graph, TCPConnectionsCache tcpConnectionsCache){
        this.tasks = tasks;
        this.graph = graph;
        this.tcpConnectionsCache = tcpConnectionsCache;
    }

    public Graph getGraph(){
        return graph;
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

    public void incrementDepth(){
        graph.incrementDepth();
    }

    public void addPageToGraph(String page){
        graph.addNode(page);
    }

    public void addFromToGraph(String source, String dest){
        graph.addFromTo(source, dest);
    }

    public void relayToOtherCrawler(String crawlerURL, String urlToCrawl){
        RelayURLToCrawl relayURLToCrawl = new RelayURLToCrawl(urlToCrawl);
        tcpConnectionsCache.sendEvent(crawlerURL, relayURLToCrawl);
    }

    @Override
    public void run() {
        Task toDo;
        while(true){
            synchronized (tasks){
                try {
                    tasks.wait();
                    toDo = tasks.poll();
                    if(toDo != null){
                        toDo.execute(this);
                    }
                } catch (InterruptedException e) {
                    System.out.println("Worker - Interrupted while waiting on Task.");
                }
            }
        }
    }
}
