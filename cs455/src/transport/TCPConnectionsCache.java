package transport;

import wireformats.Event;

import java.util.Hashtable;

/**
 * Created by ydubale on 1/22/15.
 */
public class TCPConnectionsCache {

    private Hashtable<String, TCPConnection> tcpConns; //String is url, Connection with that domain

    public TCPConnectionsCache(){
        tcpConns = new Hashtable<String, TCPConnection>();
    }

    public synchronized void addNewConn(String url, TCPConnection  tcpC){
        tcpConns.put(url, tcpC);
    }

    /**
     * Sends the corresponding event to the appropriate crawler
     * @param url - crawler to send to
     * @param event - event to send
     */
    public synchronized void sendEvent(String url, Event event){
        tcpConns.get(url).sendData(event.getBytes());
    }

    /**
     * Sends the event to all the crawlers
     * @param event - event to send
     */
    public synchronized void sendToAll(Event event){
        for(TCPConnection tcpConnection : tcpConns.values()){
            tcpConnection.sendData(event.getBytes());
        }
    }

    public String toString(){
        String toReturn = "\nExisting connections\n";
        for(String i : tcpConns.keySet()){
            toReturn += "Crawler " + i + " " + tcpConns.get(i) + "\n";
        }
        return toReturn;
    }
}