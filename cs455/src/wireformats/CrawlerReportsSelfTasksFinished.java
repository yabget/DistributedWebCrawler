package wireformats;

/**
 * Created by ydubale on 3/8/15.
 */
public class CrawlerReportsSelfTasksFinished implements Event {

    private byte type;
    private byte urlLength;
    private String crawlerRootURL;

    public CrawlerReportsSelfTasksFinished(String crawlerRootURL){
        type = Protocol.CRAWLER_REPORTS_SELF_TASKS_FINISHED;
        this.crawlerRootURL = crawlerRootURL;
        this.urlLength = (byte) crawlerRootURL.length();
    }

    public CrawlerReportsSelfTasksFinished(byte[] data){
        ByteReader byteReader = new ByteReader(data);

        type = byteReader.readByte();

        urlLength = byteReader.readByte();

        crawlerRootURL = byteReader.readString(urlLength);

        byteReader.close();
    }

    public String getCrawlerRootURL(){
        return crawlerRootURL;
    }

    @Override
    public byte[] getBytes() {
        ByteWriter byteWriter = new ByteWriter();

        byteWriter.writeByte(type);

        byteWriter.writeByte(urlLength);

        byteWriter.writeString(crawlerRootURL);

        byteWriter.close();

        return byteWriter.getBytes();
    }

    @Override
    public byte getType() {
        return type;
    }
}
