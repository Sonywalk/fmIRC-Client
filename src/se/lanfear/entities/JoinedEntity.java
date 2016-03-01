package se.lanfear.entities;

import se.lanfear.chatclient.util.ChatUtils;

/**
 * Created by LogiX on 2016-02-25.
 */
public class JoinedEntity {
    private String channel;
    private String nickname;
    private String time;

    public JoinedEntity(String channel, String nickname) {
        this.channel = channel;
        this.nickname = nickname;
        setTime();
    }

    private void setTime() {
        this.time = ChatUtils.getTime();
    }

    public String getTime() {
        return time;
    }

    public String getNickname() {
        return nickname;
    }

    public String getChannel() {
        return channel;
    }
}
