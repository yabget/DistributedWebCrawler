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
            default:
                PrintHelper.printFail("EventFactory - Unrecognized event");
        }

        return null;
    }
}