package wireformats;

/**
 * Created by ydubale on 3/8/15.
 */
public class CrawlerReportsRelayedTaskFinished implements Event {

    byte type;

    public CrawlerReportsRelayedTaskFinished(){
        type = Protocol.CRAWLER_REPORTS_RELAYED_TASK_FINISHED;
    }

    public CrawlerReportsRelayedTaskFinished(byte[] data){
        ByteReader byteReader = new ByteReader(data);

        type = byteReader.readByte();

        byteReader.close();
    }

    @Override
    public byte[] getBytes() {
        ByteWriter byteWriter = new ByteWriter();

        byteWriter.writeByte(type);

        byteWriter.close();
        return byteWriter.getBytes();
    }

    @Override
    public byte getType() {
        return type;
    }
}
