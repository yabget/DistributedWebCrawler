package harvester;

import Graph.Graph;
import Task.CrawlPage;
import Worker.ThreadPoolManager;
import transport.TCPConnection;
import transport.TCPConnectionsCache;
import transport.TCPServerThread;
import util.HTMLParser;
import util.PrintHelper;
import util.Storage;
import wireformats.*;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

/**
 * Created by ydubale on 3/5/15.
 */
public class PageCrawler extends Crawler implements Harvester {

    private ThreadPoolManager threadPoolManager;

    private String rootURLDir;

    private Graph graph;

    private TCPConnectionsCache tcpConnectionsCache;

    private Crawler[] allCrawlers;

    private int relayedCount = 0;

    private ArrayList<String> completedCrawlers;

    public PageCrawler(String hostName, int portNum, String rootURL) {
        super(hostName, portNum, rootURL);

    }

    public synchronized int getRelayedCount(){
        int copy = relayedCount;
        return copy;
    }

    public PageCrawler(int threadPoolSize, String rootURL, int server_port, Crawler[] allCrawlers) throws UnknownHostException {
        super(InetAddress.getLocalHost().getHostName(), server_port, rootURL);

        completedCrawlers = new ArrayList<String>();
        completedCrawlers.add(rootURL);

        tcpConnectionsCache = new TCPConnectionsCache();
        graph = new Graph(rootURL);

        this.allCrawlers = allCrawlers;

        String rootDir = HTMLParser.convertToDirectory(rootURL);
        this.rootURLDir = "/tmp/ydubale/" + rootDir;
        threadPoolManager = new ThreadPoolManager(threadPoolSize, rootURL, graph, tcpConnectionsCache, this);
    }

    public void setupConnectionWithOtherCrawlers(){
        System.out.println("I am " + this.hostName);
        for(Crawler crawler : allCrawlers){
            if(crawler.hostName.equalsIgnoreCase(this.hostName)){
                continue;
            }
            System.out.println("Connecting with Crawler: " + crawler);
            try {
                InetAddress address = InetAddress.getByName(crawler.hostName);
                Socket socket = new Socket(address.getHostAddress(), crawler.port);
                tcpConnectionsCache.addNewConn(crawler.rootURL, new TCPConnection(socket, this));
            } catch (UnknownHostException e) {
                PrintHelper.printErrorExit("Could not connect with : " + crawler.hostName);
            } catch (IOException e) {
                //e.printStackTrace();
                PrintHelper.printErrorExit("Could not create socket with: " + crawler.hostName);
            }
        }
    }

    public void startServerThread(){
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(this.port);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Started server on : " + serverSocket.getLocalSocketAddress());
        Thread serverThread = new Thread(new TCPServerThread(this, serverSocket));
        serverThread.start();
    }

    public void startThreadPoolManager(){
        //Start thread pool
        threadPoolManager.startWorkers();
    }

    public void initializeDirectories(){
        createDirectory(rootURLDir);
        createDirectory(rootURLDir + "/nodes");
        createDirectory(rootURLDir + "/disjoint-subgraphs");
        createFile(rootURLDir + "/broken-links");
    }

    private void handleRelayedURLToCrawl(Event event) {
        RelayURLToCrawl relayURLToCrawl = (RelayURLToCrawl) event;
        if(relayURLToCrawl == null){
            PrintHelper.printErrorExit("PageCrawler - RELAY EVENT RECEIVED IS NULL: " + relayURLToCrawl);
        }
        if(threadPoolManager == null){
            PrintHelper.printErrorExit("PageCrawler - ThreadPoolManager is null: ");
        }

        //PrintHelper.printSuccess("Receieved: " + relayURLToCrawl.getUrlToCrawl() + " to crawl.");
        threadPoolManager.addToTask(new CrawlPage(relayURLToCrawl.getUrlToCrawl(), 1,
                true, relayURLToCrawl.getSenderURL()));
    }

    private void handleOtherCrawlerTaskFinished(Event event) {
        CrawlerReportsSelfTasksFinished otherC = (CrawlerReportsSelfTasksFinished) event;
        String otherCrawlerURL = otherC.getCrawlerRootURL();
        synchronized (completedCrawlers){
            if(!completedCrawlers.contains(otherCrawlerURL)){
                completedCrawlers.add(otherCrawlerURL);
                threadPoolManager.notifyTaskPool();
                System.out.println(completedCrawlers.size() + " Crawlers say they are finished.");
                for(String s : completedCrawlers){
                    System.out.println("\t\t" + s);
                }
            }
        }
    }

    public synchronized boolean allOtherCrawlersFinished(){
        for(String validURL : Storage.validRedirectDomains){
            if(!completedCrawlers.contains(validURL)){
                return false;
            }
        }
        return true;
    }

    private void handleOtherCrawlerTaskNotFinished(Event event) {
        CrawlerReportsTasksNotFinished notFin = (CrawlerReportsTasksNotFinished) event;
        synchronized (completedCrawlers){
            if(completedCrawlers.contains(notFin.getCrawlerRootURL())){
                completedCrawlers.remove(completedCrawlers);
                threadPoolManager.notifyTaskPool();
            }
        }
    }

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
                    PrintHelper.printFail("PageCrawler - Unrecognized event! " + protocol);
            }
        }
    }

    private void createFile(String filePath){
        File file = new File(filePath);
        if(!file.exists()){
            try {
                file.createNewFile();
                //PrintHelper.printSuccess("Created " + filePath);
            } catch (IOException e) {
                //PrintHelper.printFail("Could not create " + filePath);
            }
        }
    }

    private void createDirectory(String dirPath){
        File dirFile = new File(dirPath);
        if(!dirFile.exists()){
            if(dirFile.mkdirs()){
                //PrintHelper.printSuccess("Created " + dirPath);
            }
        }
        else {
            //PrintHelper.printFail("Could not create " + dirPath);
        }
    }

    public synchronized void incrementRelayedCount() {
        relayedCount++;
    }

    public void sendRelayedTaskFinished(String toSendTo, String page) {
        tcpConnectionsCache.sendEvent(toSendTo, new CrawlerReportsRelayedTaskFinished());
    }
}
