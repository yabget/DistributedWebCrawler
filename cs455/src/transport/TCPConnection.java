package transport;

import harvester.Harvester;
import wireformats.Event;
import wireformats.EventFactory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Created by ydubale on 1/22/15.
 */
public class TCPConnection {

    private TCPSender tcpSender;
    private TCPReceiver tcpReceiver;

    public TCPConnection(Socket socket, Harvester harvester){
        tcpSender = new TCPSender(socket);
        tcpReceiver = new TCPReceiver(socket, harvester);
    }

    public void startReceiveThread(){
        Thread receiveThread = new Thread(tcpReceiver);
        receiveThread.start();
    }

    public String getIP(){
        return tcpSender.socket.getInetAddress().getHostAddress();
    }

    public int getPort(){
        return tcpSender.socket.getPort();
    }

    public String toString(){
        return getIP() + " " + getPort();
    }

    public void sendData(byte[] dataToSend){
        tcpSender.sendData(dataToSend);
    }

    private class TCPSender{

        private Socket socket;

        public TCPSender(Socket socket){
            this.socket = socket;
        }

        public synchronized void sendData(byte[] dataToSend){
            try{
                DataOutputStream dos = new DataOutputStream(socket.getOutputStream());

                int dataLen = dataToSend.length;

                dos.writeInt(dataLen);
                dos.write(dataToSend, 0, dataLen);
                dos.flush();
            }
            catch (IOException ioe){
                ioe.printStackTrace();
            }
        }
    }

    private class TCPReceiver implements Runnable {

        private Socket socket;
        private Harvester harvester;

        public TCPReceiver(Socket socket, Harvester harvester) {
            this.socket = socket;
            this.harvester = harvester;
        }

        @Override
        public void run() {
            try {
                int dataLen;

                DataInputStream dis = new DataInputStream(socket.getInputStream());

                EventFactory eventFac = EventFactory.getInstance();

                while(socket != null){
                    dataLen = dis.readInt();
                    byte[] data = new byte[dataLen];
                    dis.readFully(data, 0, dataLen);

                    Event receivedEvent = eventFac.getEvent(data);
                    harvester.onEvent(receivedEvent);
                }
            }
            catch(IOException ioe ){
                System.out.println("Connection with node " + socket.getInetAddress().getHostAddress() + " is lost! ");
            }
        }
    }
}