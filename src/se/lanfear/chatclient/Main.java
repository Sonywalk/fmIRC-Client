package se.lanfear.chatclient;

import se.lanfear.observers.view.GUI;
import se.lanfear.observers.ChatListener;

public class Main {

    public static void main(String[] args) {
        ChatClient chat = new ChatClient();
        ChatListener gui = new GUI(chat);
        chat.register(gui);
    }
}
