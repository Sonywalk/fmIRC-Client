package se.lanfear.entities;

import java.net.InetSocketAddress;

/**
 * Created by LogiX on 2016-02-23.
 */
public class ConnectEntity {
    private InetSocketAddress address;
    private String nickname;

    public InetSocketAddress getAddress() {
        return address;
    }

    public void setAddress(InetSocketAddress address) {
        this.address = address;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
}
