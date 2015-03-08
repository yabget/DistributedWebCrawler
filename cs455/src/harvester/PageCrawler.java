package harvester;

import Graph.Graph;
import Task.CrawlPage;
import Worker.ThreadPoolManager;
import transport.TCPConnection;
import transport.TCPConnectionsCache;
import transport.TCPServerThread;
import util.HTMLParser;
import util.PrintHelper;
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
 * Created by ydubale on 3/5/15.
 */
public class PageCrawler extends Crawler implements Harvester {

    private ThreadPoolManager threadPoolManager;

    private String rootURLDir;

    private Graph graph;

    private TCPConnectionsCache tcpConnectionsCache;

    private Crawler[] allCrawlers;

    public PageCrawler(String hostName, int portNum, String rootURL) {
        super(hostName, portNum, rootURL);

    }

    public PageCrawler(int threadPoolSize, String rootURL, int server_port, Crawler[] allCrawlers) throws UnknownHostException {
        super(InetAddress.getLocalHost().getHostName(), server_port, rootURL);

        tcpConnectionsCache = new TCPConnectionsCache();
        graph = new Graph(rootURL);

        this.allCrawlers = allCrawlers;

        String rootDir = HTMLParser.convertToDirectory(rootURL);
        this.rootURLDir = "/tmp/ydubale/" + rootDir;
        threadPoolManager = new ThreadPoolManager(threadPoolSize, rootURL, graph, tcpConnectionsCache);
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
        Thread tpmThread = new Thread(threadPoolManager);
        tpmThread.start();
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
            PrintHelper.printErrorExit("PageCralwer - ThreadPoolManaager is null: ");
        }

        //PrintHelper.printSuccess("Receieved: " + relayURLToCrawl.getUrlToCrawl() + " to crawl.");
        threadPoolManager.addToTask(new CrawlPage(relayURLToCrawl.getUrlToCrawl(), 1));
    }

    @Override
    public void onEvent(Event event) {
        synchronized (event){
            byte protocol = event.getType();

            switch (protocol){
                case Protocol.RELAY_URL_TO_CRAWL:
                    handleRelayedURLToCrawl(event);
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

}
