package se.lanfear.observers.view.tab;

import se.lanfear.chatclient.ChatClient;
import se.lanfear.entities.AppendEntity;
import se.lanfear.entities.MessageEntity;
import se.lanfear.chatclient.util.ChatUtils;
import se.lanfear.observers.view.GUI;

import java.awt.*;

public class PrivateChatTab extends Tab {
    private Color buddyColor;

    public PrivateChatTab(String id, ChatClient chatClient, GUI gui) {
        super(id, chatClient, gui);
        buddyColor = ChatUtils.getRandomColor();
    }

    @Override
    public void message(MessageEntity entity) {
        Color foreground;
        if (entity.getFrom().equals(id)) {
            foreground = buddyColor;
        }
        else {
            foreground = myColor;
        }
        String msg = "[" + entity.getTime() + "] <" + entity.getFrom() + "> " + entity.getMessage();
        appendToPane(new AppendEntity(foreground, null, msg));
    }

    @Override
    protected void write() {
        chatClient.write("MSG " + id + " :" + chat.getText());
        chat.setText("");
    }
}
