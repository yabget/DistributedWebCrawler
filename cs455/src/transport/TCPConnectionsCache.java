package transport;

import wireformats.Event;

import java.util.Hashtable;

/**
 * Created by ydubale on 1/22/15.
 */
public class TCPConnectionsCache {

    private Hashtable<String, TCPConnection> tcpConns;

    public TCPConnectionsCache(){
        tcpConns = new Hashtable<String, TCPConnection>();
    }

    public void addNewConn(String url, TCPConnection  tcpC){
        tcpConns.put(url, tcpC);
    }

    public void removeConn(String url){
        //todo: check if remove needs a check if exists
        tcpConns.remove(url);
    }

    public TCPConnection getTCPConnection(String url){
        return tcpConns.get(url);
    }

    public void sendEvent(String url, Event event){
        tcpConns.get(url).sendData(event.getBytes());
    }

    public String toString(){
        String toReturn = "\nExisting connections\n";
        for(String i : tcpConns.keySet()){
            toReturn += "Crawler " + i + " " + tcpConns.get(i) + "\n";
        }
        return toReturn;
    }
}