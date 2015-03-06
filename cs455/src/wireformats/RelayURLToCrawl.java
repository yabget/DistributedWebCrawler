package wireformats;

/**
 * Created by ydubale on 3/5/15.
 */
public class RelayURLToCrawl implements Event {

    private byte type;
    private byte urlLength;
    private String urlToCrawl;

    public RelayURLToCrawl(String urlToCrawl){
        this.type = Protocol.RELAY_URL_TO_CRAWL;
        this.urlLength = (byte) urlToCrawl.length();
        this.urlToCrawl = urlToCrawl;
    }

    public RelayURLToCrawl(byte[] data){
        ByteReader byteReader = new ByteReader(data);

        type = byteReader.readByte();

        urlLength = byteReader.readByte();

        urlToCrawl = byteReader.readString(urlLength);

        byteReader.close();
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

        byteWriter.close();

        return byteWriter.getBytes();
    }

    @Override
    public byte getType() {
        return type;
    }
}
