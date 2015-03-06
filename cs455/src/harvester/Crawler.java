package harvester;

import Graph.Graph;
import Task.CrawlPage;
import Worker.ThreadPoolManager;
import transport.TCPConnection;
import transport.TCPConnectionsCache;
import transport.TCPServerThread;
import util.CommandLineParser;
import util.PrintHelper;
import util.URLExtractor;
import wireformats.Event;
import wireformats.Protocol;
import wireformats.RelayURLToCrawl;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Created by ydubale on 2/17/15.
 */
public class Crawler implements Harvester {

    private String rootURLDir;
    private String rootURL;
    private ThreadPoolManager threadPoolManager;
    private Graph graph;
    private final int threadPoolSize;

    private String hostName;
    private int port;

    private TCPConnection tcpConnection;
    private TCPConnectionsCache tcpConnectionsCache;

    private Crawler[] otherCrawlers;

    public Crawler(int threadPoolSize, String rootURL, int server_port, Crawler[] otherCrawlers) throws IOException{
        this.threadPoolSize = threadPoolSize;
        tcpConnectionsCache = new TCPConnectionsCache();
        graph = new Graph(rootURL);

        this.port = server_port;
        this.hostName = InetAddress.getLocalHost().getHostName();
        this.otherCrawlers = otherCrawlers;

        String rootDir = URLExtractor.convertToDirectory(rootURL);
        this.rootURLDir = "/tmp/ydubale/" + rootDir;
        this.rootURL = rootURL;
    }

    public void setupConnectionWithOtherCrawlers(){
        System.out.println("I am " + this.hostName);
        for(Crawler crawler : otherCrawlers){
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

    public Crawler(String hostName, int portNum, String rootURL){
        this.rootURL = rootURL;
        this.port = portNum;
        this.hostName = hostName;
        this.threadPoolSize = 0;
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
        threadPoolManager = new ThreadPoolManager(threadPoolSize, rootURL, graph, tcpConnectionsCache);
        Thread tpmThread = new Thread(threadPoolManager);
        tpmThread.start();
    }

    public void initializeDirectories(){
        createDirectory(rootURLDir);
        createDirectory(rootURLDir + "/nodes");
        createDirectory(rootURLDir + "/disjoint-subgraphs");
        createFile(rootURLDir + "/broken-links");
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

    private void sleepSeconds(int seconds){
        try {
            System.out.println("Counting to " + seconds + " seconds!");
            for(int i=0; i< seconds; i++){
                System.out.println("Second " + (i + 1));
                Thread.sleep(1000);
            }
        } catch (InterruptedException e) {
            System.out.println("Oh No! Somehow I have a problem sleeping for 20 seconds.");
        }
    }

    public String toString(){
        return this.hostName + "\t\t" + this.port + "\t\t" + this.rootURL;
    }

    private void handleRelayedURLToCrawl(Event event) {
        RelayURLToCrawl relayURLToCrawl = (RelayURLToCrawl) event;
        if(relayURLToCrawl == null){
            PrintHelper.printErrorExit("RELAY EVENT RECEIVED IS NULL: " + relayURLToCrawl);
        }

        PrintHelper.printSuccess("Receieved: " + relayURLToCrawl.getUrlToCrawl() + " to crawl.");
        threadPoolManager.addToTask(new CrawlPage(relayURLToCrawl.getUrlToCrawl(), 0));
    }

    @Override
    public void onEvent(Event event) {

        byte protocol = event.getType();

        switch (protocol){
            case Protocol.RELAY_URL_TO_CRAWL:
                handleRelayedURLToCrawl(event);
            default:
                PrintHelper.printFail("Unrecognized event!");
        }

    }

    public static void main(String[] args){
        CommandLineParser clp = new CommandLineParser(args);

        System.out.println("Port: " + clp.portNum);
        System.out.println("TP-s: " + clp.threadPoolSize);
        System.out.println("Root: " + clp.rootUrl);
        System.out.println("Path: " + clp.pathToConfigFile);
        System.out.println();

        try {
            Crawler crawler = new Crawler(clp.threadPoolSize, clp.rootUrl, clp.portNum, clp.getCrawlers());
            crawler.startServerThread();
            //Wait for all servers to start on other machines
            crawler.sleepSeconds(15);
            crawler.setupConnectionWithOtherCrawlers();
            //Wait to make sure all connections have been made
            crawler.sleepSeconds(15);
            crawler.startThreadPoolManager();

        } catch (IOException e) {
            //e.printStackTrace();
            PrintHelper.printErrorExit("Could not start server on " + clp.portNum);
        }
        //crawler.initializeDirectories();
    }


}
