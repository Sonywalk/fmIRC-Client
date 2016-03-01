package se.lanfear.observers;

import se.lanfear.chatclient.util.Event;

/**
 * Created by LogiX on 2016-02-23.
 */
public interface ChatListener {

    void update(Event e, Object obj);
}
