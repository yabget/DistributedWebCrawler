package wireformats;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ydubale on 2/13/15.
 */
public class ByteReader {

    private ByteArrayInputStream byteArrayInputStream;
    private DataInputStream dataInputStream;

    public ByteReader(byte[] data){
        byteArrayInputStream = new ByteArrayInputStream(data);
        dataInputStream = new DataInputStream(new BufferedInputStream(byteArrayInputStream));
    }

    public DataInputStream getInputStream(){
        return dataInputStream;
    }

    public void close(){
        try {
            byteArrayInputStream.close();
            dataInputStream.close();
        } catch (IOException e) {
            System.out.println("Could not close reader properly.");
        }
    }

    public byte readByte(){
        try {
            return dataInputStream.readByte();
        } catch (IOException e) {
            System.out.println("Could not read byte.");
        }
        return -1;
    }

    public byte[] readByteArray(int length){
        return (readString(length)).getBytes();
    }

    public int readInt(){
        try {
            return dataInputStream.readInt();
        } catch (IOException e) {
            System.out.println("Could not read int.");
        }
        return -1;
    }

    public long readLong(){
        try {
            return dataInputStream.readLong();
        } catch (IOException e) {
            System.out.println("Could not read long.");
        }
        return -1;
    }

    public String readString(int length){
        byte[] stringBytes = new byte[length];
        try {
            dataInputStream.readFully(stringBytes);
        } catch (IOException e) {
            System.out.println("Could not read String/Byte[].");
        }
        return new String(stringBytes);
    }

    public List<Integer> readIntList(int length){
        List<Integer> list = new ArrayList<Integer>();
        try {
            for(int i=0; i < length; i++){
                list.add(dataInputStream.readInt());
            }
        } catch (IOException e) {
            System.out.println("Could not read int list.");
        }
        return list;
    }


}
