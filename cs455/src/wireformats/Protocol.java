package wireformats;

/**
 * Created by ydubale on 1/20/15.
 */
public interface Protocol {

    public static final byte RELAY_URL_TO_CRAWL = 1;
    public static final byte CRAWLER_REPORTS_RELAYED_TASK_FINISHED = 2;

    public static final byte CRAWLER_REPORTS_SELF_TASKS_FINISHED = 3;
    public static final byte CRAWLER_REPORTS_TASKS_NOT_FINISHED = 4;


}