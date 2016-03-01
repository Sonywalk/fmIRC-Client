package se.lanfear.entities;

import se.lanfear.chatclient.util.ChatUtils;

/**
 * Created by LogiX on 2016-02-23.
 */
public class MessageEntity {

    private String to;
    private String from;
    private String message;
    private String time;

    public MessageEntity(String to, String from, String message) {
        this.to = to;
        this.from = from;
        this.message = message;
        setTime();
    }

    private void setTime() {
        this.time = ChatUtils.getTime();
    }

    public String getTime() {
        return time;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
