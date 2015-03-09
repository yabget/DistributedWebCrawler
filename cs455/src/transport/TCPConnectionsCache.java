package transport;

import util.PrintHelper;
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

    public synchronized void addNewConn(String url, TCPConnection  tcpC){
        tcpConns.put(url, tcpC);
    }

    public synchronized void sendEvent(String url, Event event){
        if(event == null){
            PrintHelper.printErrorExit("TCPConnectionsCache - event is null sending.");
        }
        if(tcpConns == null){
            PrintHelper.printErrorExit("TCPConnectionsCache - is null.");
        }
        if(tcpConns.get(url) == null){
            PrintHelper.printErrorExit("TCPConnectionsCache - get " + url + " is null.");
        }

        tcpConns.get(url).sendData(event.getBytes());
    }

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