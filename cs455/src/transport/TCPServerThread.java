package transport;

import harvester.Harvester;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by ydubale on 1/22/15.
 */
public class TCPServerThread implements Runnable {

    private ServerSocket serverSocket;
    private Harvester harvester;

    public TCPServerThread(Harvester harvester, ServerSocket serverSocket){
        this.serverSocket = serverSocket;
        this.harvester = harvester;
    }

    @Override
    public void run() {
        try {
            Socket socket;
            //System.out.println("Server thread is running: " + serverSocket.getLocalSocketAddress().toString());
            while((socket = serverSocket.accept()) != null){
                //Starts a new receiver thread to listen on the socket
                System.out.println("Accepted new connection! " + socket.getInetAddress().getHostAddress().toString());
                TCPConnection newConnection = new TCPConnection(socket, harvester);
                newConnection.startReceiveThread();
            }
        } catch (IOException e) {
            System.out.println("Problem configuring new TCPConnection (TCPServerThread).");
        }
    }

}