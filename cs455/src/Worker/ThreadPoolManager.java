package Worker;

import Graph.Graph;
import Task.CrawlPage;
import Task.Task;
import transport.TCPConnectionsCache;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by ydubale on 3/1/15.
 */
public class ThreadPoolManager implements Runnable {

    private Queue<Task> tasks;
    private Queue<Thread> threadpool;
    private TCPConnectionsCache tcpConnectionsCache;
    private Graph graph;

    public ThreadPoolManager(int threadPoolSize, String rootURL, Graph graph, TCPConnectionsCache tcpConnectionsCache) {
        threadpool = new LinkedList<Thread>();
        tasks = new LinkedList<Task>();

        this.tcpConnectionsCache = tcpConnectionsCache;
        this.graph = graph;

        for(int i = 0; i < threadPoolSize; i++){
            threadpool.add(new Thread(new Worker(tasks, this.graph, this.tcpConnectionsCache)));
        }

        tasks.offer(new CrawlPage(rootURL, -1));
        for(Thread thread : threadpool){
            thread.start();
        }
        System.out.println("Started all threads");
    }

    public void addToTask(Task task){
        synchronized (tasks){
            tasks.offer(task);
            tasks.notifyAll();
        }
    }

    @Override
    public void run() {
        while (true){
            synchronized (tasks){
                if(tasks.isEmpty()){
                    System.out.println("TaskQueue is empty");
                    try {
                        //todo: check if it is the end of the program?
                        System.out.println("Waiting for notification.");
                        tasks.wait();
                        break;
                    } catch (InterruptedException e) {
                        System.out.println("ThreadPoolManager: Interrupted while waiting on Task.");
                    }
                }else{
                    tasks.notifyAll();
                }
            }
        }
        graph.printTree();
    }

}
