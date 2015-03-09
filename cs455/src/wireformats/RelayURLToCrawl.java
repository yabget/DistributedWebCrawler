package wireformats;

/**
 * Created by ydubale on 3/5/15.
 */
public class RelayURLToCrawl implements Event {

    private byte type;
    private byte urlLength;
    private String urlToCrawl;
    private String senderURL;
    private byte sendURLLength;

    public RelayURLToCrawl(String urlToCrawl, String senderURL){
        this.type = Protocol.RELAY_URL_TO_CRAWL;
        this.urlLength = (byte) urlToCrawl.length();
        this.urlToCrawl = urlToCrawl;
        this.sendURLLength = (byte) senderURL.length();
        this.senderURL = senderURL;
    }

    public RelayURLToCrawl(byte[] data){
        ByteReader byteReader = new ByteReader(data);

        type = byteReader.readByte();

        urlLength = byteReader.readByte();

        urlToCrawl = byteReader.readString(urlLength);

        sendURLLength = byteReader.readByte();

        senderURL = byteReader.readString(sendURLLength);

        byteReader.close();
    }

    public String getSenderURL(){
        return senderURL;
    }

    public String getUrlToCrawl(){
        return urlToCrawl;
    }

    @Override
    public byte[] getBytes() {
        ByteWriter byteWriter = new ByteWriter();

        byteWriter.writeByte(type);

        byteWriter.writeByte(urlLength);

        byteWriter.writeString(urlToCrawl);

        byteWriter.writeByte(sendURLLength);

        byteWriter.writeString(senderURL);

        byteWriter.close();

        return byteWriter.getBytes();
    }

    @Override
    public byte getType() {
        return type;
    }
}
