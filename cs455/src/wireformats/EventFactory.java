package wireformats;

import util.PrintHelper;

/**
 * Created by ydubale on 1/22/15.
 */
public class EventFactory {
    private static final EventFactory ourInstance = new EventFactory();

    public static EventFactory getInstance() {
        return ourInstance;
    }

    private EventFactory() {

    }

    public Event getEvent(byte[] data) {
        byte protocol = data[0];

        switch (protocol) {

            case Protocol.RELAY_URL_TO_CRAWL:
                return new RelayURLToCrawl(data);
            case Protocol.CRAWLER_REPORTS_RELAYED_TASK_FINISHED:
                return new CrawlerReportsRelayedTaskFinished(data);
            case Protocol.CRAWLER_REPORTS_SELF_TASKS_FINISHED:
                return new CrawlerReportsSelfTasksFinished(data);
            case Protocol.CRAWLER_REPORTS_TASKS_NOT_FINISHED:
                return new CrawlerReportsTasksNotFinished(data);
            default:
                PrintHelper.printFail("EventFactory - Unrecognized event");
        }

        return null;
    }
}