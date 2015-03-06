package harvester;

import wireformats.Event;

/**
 * Created by ydubale on 3/3/15.
 */
public interface Harvester {

    public void onEvent(Event event);
}
