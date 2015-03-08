package wireformats;

/**
 * Created by ydubale on 3/8/15.
 */
public class CrawlerReportsSelfTasksFinished implements Event {

    byte type;

    public CrawlerReportsSelfTasksFinished(){
        type = Protocol.CRAWLER_REPORTS_SELF_TASKS_FINISHED;
    }

    public CrawlerReportsSelfTasksFinished(byte[] data){
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
