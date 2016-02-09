package com.company;

import com.company.Util.ChatUtils;

import javax.swing.text.BadLocationException;
import java.awt.*;
import java.io.IOException;

/**
 * Created by LogiX on 2016-02-09.
 */
public class InputHelper {
    private ChatClient client;

    public InputHelper(ChatClient client) {
        this.client = client;
    }

    public void processInput(String input) throws IOException, BadLocationException {
        String time = "[" + ChatUtils.getTime() + "]";
        if (input.equals("NICK?")) {
            client.write("NICK " + client.nick.getText());
        }
        else if (input.equals("NICK OK")) {
            ChatUtils.appendToPane(client.getMainTextPane(), "Welcome to fmIRC+!", Color.RED, Color.DARK_GRAY);
            client.setNickname(client.nick.getText());
            client.setWindowTitle("fmIRC+ - " + client.getNickname());
        }
        else if (input.equals("NICK TAKEN")) {
            ChatUtils.appendToPane(client.getMainTextPane(), "Nickname already in use, please pick another one!", Color.RED, Color.BLUE);
        }
        else if (input.startsWith("JOINED")) {
            String name = input.replace("JOINED", "").trim();
            client.putOnlineNameAndColor(name);
            ChatUtils.appendToPane(client.getMainTextPane(), time + " " + name + " has just joined the chat!", Color.MAGENTA, Color.CYAN);
            client.createPopupAsync(name + " just logged in!", "");
            client.updateOnlineList();
        }
        else if (input.startsWith("MESSAGE")) {
            int index = input.indexOf(":");
            String message = input.substring(index + 1, input.length());
            String name = input.substring(0, index).replace("MESSAGE", "").trim();
            ChatUtils.appendToPane(client.getMainTextPane(), time + " <" + name + ">: " + message, client.getColorByName(name), null);
            client.createPopupAsync("New Message", message);
        }
        else if (input.startsWith("PRIVMSG")) {
            handlePrivateIncomingMessage(input);
        }
        else if (input.startsWith("RESPONSEPRIVMSG")) {
            handlePrivateIncomingMessageResponse(input);
        }
        else if (input.startsWith("QUIT")) {
            String name = input.replace("QUIT", "").trim();
            client.removeOnlineNameAndColor(name);
            client.updateOnlineList();
            ChatUtils.appendToPane(client.getMainTextPane(), time + " " + name + " has left the chat!", Color.GREEN, Color.DARK_GRAY);
        }
    }

    //"PRIVMSG from@to :message
    private void handlePrivateIncomingMessage(String input) throws BadLocationException, IOException {
        int index = input.indexOf(":");
        String message = input.substring(index + 1, input.length());
        String[] fromAndTo = input.substring(0, index-1).replace("PRIVMSG", "").split("@");
        String from = fromAndTo[0].trim();
        String to = fromAndTo[1].trim();

        if (client.messageTabExists(from)) {
            client.getPrivateChatWindow(from).appendToPane("[" + ChatUtils.getTime() + "]" + " <" + from + ">: " + message, from);
        }
        else {
            PrivateChatWindow pcw = new PrivateChatWindow(client, from);
            client.putPrivateChatWindow(from, pcw);
            pcw.appendToPane("[" + ChatUtils.getTime() + "]" + " <" + from + ">: " + message, from);
        }
    }

    private void handlePrivateIncomingMessageResponse(String input) throws BadLocationException, IOException {
        int index = input.indexOf(":");
        String message = input.substring(index + 1, input.length());
        String to = input.substring(0, index).replace("RESPONSEPRIVMSG", "").trim();

        if (client.messageTabExists(to)) {
            client.getPrivateChatWindow(to).appendToPane("[" + ChatUtils.getTime() + "]" +
                    " <" + client.getNickname() + ">: " + message, client.getNickname());
        }
        else {
            PrivateChatWindow pcw = new PrivateChatWindow(client, to);
            client.putPrivateChatWindow(to, pcw);
            pcw.appendToPane("[" + ChatUtils.getTime() + "]" + " <" + client.getNickname() + ">: " + message, client.getNickname());
        }
    }
}
