package harvester;

import Graph.Graph;
import Graph.Node;
import Task.CrawlPage;
import Worker.ThreadPoolManager;
import transport.TCPConnection;
import transport.TCPConnectionsCache;
import transport.TCPServerThread;
import util.HTMLParser;
import util.Util;
import wireformats.*;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by ydubale on 3/5/15.
 */
public class PageCrawler extends Crawler implements Harvester {

    private TCPConnectionsCache tcpConnectionsCache;
    private ArrayList<String> completedCrawlers; //Default includes this crawler
    private ThreadPoolManager threadPoolManager;
    private Crawler[] allCrawlers;
    private Graph graph;

    private int relayedCount = 0;   //Packets sent to other crawlers

    public PageCrawler(int threadPoolSize, String rootURL, int server_port, Crawler[] allCrawlers)
            throws UnknownHostException {

        super(InetAddress.getLocalHost().getHostName(), server_port, rootURL);

        completedCrawlers = new ArrayList<String>();
        completedCrawlers.add(rootURL); //Add myself to the list of completed (to make count 8)

        graph = new Graph(rootURL);

        this.allCrawlers = allCrawlers;

        tcpConnectionsCache = new TCPConnectionsCache();

        threadPoolManager = new ThreadPoolManager(threadPoolSize, rootURL, graph, tcpConnectionsCache, this);
    }

    public synchronized int getRelayedCount(){
        return relayedCount;
    }

    public synchronized void incrementRelayedCount() {
        relayedCount++;
    }

    /**
     * Establishes a TCPConnection with every crawler
     */
    public void setupConnectionWithOtherCrawlers(){
        try {
            for(Crawler crawler : allCrawlers){
                if(crawler.hostName.equalsIgnoreCase(this.hostName)){
                    //If I am the crawler, then ignore self
                    continue;
                }
                System.out.println("Connecting with Crawler: " + crawler); //Calls parent to string

                InetAddress address = InetAddress.getByName(crawler.hostName); // Get IP (xxx.xxx.xxx.xxx)

                Socket socket = new Socket(address.getHostAddress(), crawler.port);

                //Adds the crawler to TCPConnection
                tcpConnectionsCache.addNewConn(crawler.rootURL, new TCPConnection(socket, this));
            }
        } catch (UnknownHostException e) {
            Util.printErrorExit("PageCrawler - Could not connect with : " + this.hostName);
        } catch (IOException e) {
            Util.printErrorExit("PageCrawler - Could not create socket with: " + this.hostName);
        }
    }

    /**
     * Starts the server socket and server thread
     */
    public void startServerThread(){
        try {
            ServerSocket serverSocket= new ServerSocket(this.port);
            Thread serverThread = new Thread(new TCPServerThread(this, serverSocket));
            serverThread.start();
        } catch (IOException e) {
            Util.printErrorExit("PageCrawler - Could not start server socket for listening.");
        }
    }

    /**
     * Starts the thread pool manager
     */
    public void startThreadPoolManager(){
        threadPoolManager.startWorkers();
    }

    /**
     * When a URL is received from another crawler, add it to the task queue
     * @param event
     */
    private void handleRelayedURLToCrawl(Event event) {
        RelayURLToCrawl relayURLToCrawl = (RelayURLToCrawl) event;
        Util.printSuccess("Receieved: " + relayURLToCrawl.getUrlToCrawl() + " to crawl.");

        String toCrawl = relayURLToCrawl.getUrlToCrawl();
        String sender = relayURLToCrawl.getSenderURL();

        threadPoolManager.addToTask(new CrawlPage(toCrawl, 1, true, sender));
    }

    /**
     * When another crawler sends their task finished
     *  - If crawler is not in the completed crawlers list, add it
     *  - Notify threads waiting on task pool
     *      - If the task pool is empty - triggers a check for allOtherCrawlersFinished()
     * @param event - CrawlerReportsSelfTaskFinished
     */
    private void handleOtherCrawlerTaskFinished(Event event) {
        CrawlerReportsSelfTasksFinished otherC = (CrawlerReportsSelfTasksFinished) event;
        String otherCrawlerURL = otherC.getCrawlerRootURL();

        synchronized (completedCrawlers){
            if(!completedCrawlers.contains(otherCrawlerURL)){
                completedCrawlers.add(otherCrawlerURL);
                //Util.printAlert(completedCrawlers.size()-1 + " crawlers say they are finished.");
                threadPoolManager.notifyTaskPool(); //Notify everyone waiting on TaskPool

            }
        }
    }

    /**
     * If another crawler reports that they have not finished their task
     * Remove them from the list of completed crawlers.
     * This can happen because crawlers receive a hand off task after their queue was empty
     * @param event
     */
    private void handleOtherCrawlerTaskNotFinished(Event event) {
        CrawlerReportsTasksNotFinished notFin = (CrawlerReportsTasksNotFinished) event;

        synchronized (completedCrawlers){
            if(completedCrawlers.contains(notFin.getCrawlerRootURL())){
                completedCrawlers.remove(notFin.getCrawlerRootURL());
                //Util.printAlert(notFin.getCrawlerRootURL() + " says not finished with tasks.");
                threadPoolManager.notifyTaskPool(); // Triggers check for all crawlers completed
            }
        }
    }

    /**
     * Checks if all of the crawlers have sent a message saying they are finished
     * List includes self
     * @return
     */
    public synchronized boolean allOtherCrawlersFinished(){
        for(String validURL : Util.validRedirectDomains){
            if(!completedCrawlers.contains(validURL)){
                return false;
            }
        }
        return true;
    }

    /**
     * Writes the content of the graph to directories
     */
    public void writeGraphToDirectories(){
        String currentCrawler = graph.getRootURL();
        Util.printAlert("I am " + currentCrawler +"Writing my graph to directories.");
        String rootURL = graph.getRootURL();
        String rootDir = Util.convertURLToDirectory(rootURL);

        String dirPrefix = "/tmp/cs455-ydubale/" + rootDir + "/nodes/";

        int disjointG = 1;
        for(Node node : graph.getNodes()){
            if(node.getValue().contains(graph.getRootURL())){
                String nodeDir = dirPrefix + Util.convertURLToDirectory(node.getValue());
                String inFileDir = nodeDir + "/in";
                String outFileDir = nodeDir + "/out";

                Util.writeNodesToFile(inFileDir, node.getInNodes());
                Util.writeNodesToFile(outFileDir, node.getOutNodes());
            }
            else {
                String disGPrefix = "/tmp/cs455-ydubale/" + rootDir + "/disjoint-subgraphs/";

                String file = disGPrefix + "graph" + disjointG;
                disjointG++;

                Collection<Node> toWrite = new ArrayList<Node>();
                toWrite.add(node);

                Util.writeNodesToFile(file, toWrite);
            }
        }

        String brokenLinksFile = "/tmp/cs455-ydubale/" + rootDir + "/broken-links";
        Util.writeStringsToFile(brokenLinksFile, HTMLParser.getInstance().getBrokenLinks());

        Util.printAlert("Finished writing graph to directories.");
    }

    /**
     * Notifies the crawler that the relayed task has finished
     * @param toSendTo
     */
    public void sendRelayedTaskFinished(String toSendTo) {
        tcpConnectionsCache.sendEvent(toSendTo, new CrawlerReportsRelayedTaskFinished());
    }

    /**
     * onEvent is called when a message is received, depending on the protocol,
     * Do a corresponding task
     * @param event
     */
    @Override
    public void onEvent(Event event) {
        synchronized (event){
            byte protocol = event.getType();

            switch (protocol){
                case Protocol.RELAY_URL_TO_CRAWL:
                    handleRelayedURLToCrawl(event);
                    return;
                case Protocol.CRAWLER_REPORTS_RELAYED_TASK_FINISHED:
                    relayedCount--;
                    return;
                case Protocol.CRAWLER_REPORTS_SELF_TASKS_FINISHED:
                    handleOtherCrawlerTaskFinished(event);
                    return;
                case Protocol.CRAWLER_REPORTS_TASKS_NOT_FINISHED:
                    handleOtherCrawlerTaskNotFinished(event);
                    return;
                default:
                    Util.printFail("PageCrawler - Unrecognized event! " + protocol);
            }
        }
    }
}
