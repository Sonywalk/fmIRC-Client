package com.company;

import com.company.Util.ChatUtils;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.io.File;
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
        String time = "[" + ChatUtils.getTime() + "] ";
        if (input.equals("NICK?")) {
            client.write("NICK " + client.nick.getText());
        }
        else if (input.equals("NICK OK")) {
            nickOk(time);
        }
        else if (input.equals("NICK TAKEN")) {
            nickTaken(time);
        }
        else if (input.startsWith("JOINED")) {
            joined(input, time);
        }
        else if (input.startsWith("MESSAGE")) {
            message(input, time);
        }
        else if (input.startsWith("PRIVMSG")) {
            privateMessage(input, time);
        }
        else if (input.startsWith("QUIT")) {
            quit(input, time);
        }
        else if (input.startsWith("GET")) {
            sendFile(input);
        }
        else if (input.startsWith("SENDING")) {
            receiveFile(input);
        }
    }

    private void sendFile(String input) throws IOException, BadLocationException {
        int index = input.indexOf(":");
        String filename = input.substring(index + 1, input.length());
        String to = input.substring(0, index).replace("GET", "").trim();
        File f = new File(filename);
        String size = Long.toString(f.length());
        new FileSender(filename, size, client).execute();
        client.write("SENDING " + to + " :" + filename + " -" + size);
    }

    private void receiveFile(String input) throws IOException, BadLocationException {
        String filename = input.substring(input.indexOf(":") + 1, input.indexOf("-")).trim();
        String size = input.substring(input.indexOf("-")+1, input.length());
        new FileReceiver(filename, size, client).execute();
    }

    //"PRIVMSG from@to :message
    private void privateMessage(String input, String time) throws BadLocationException, IOException {
        int index = input.indexOf(":");
        String message = input.substring(index + 1, input.length());
        String[] fromAndTo = input.substring(0, index-1).replace("PRIVMSG", "").split("@");
        String from = fromAndTo[0].trim();
        String to = fromAndTo[1].trim();
        String output = time + "<" + from + ">: " + message;

        if (from.equals(client.getNickname())) {
            if (client.messageTabExists(to)) {
                client.getPrivateChatWindow(to).appendToPane(output, from);
            }
            else {
                PrivateChatWindow pcw = new PrivateChatWindow(client, from);
                client.putPrivateChatWindow(from, pcw);
                pcw.appendToPane(output, from);
            }
        }
        else {
            if (client.messageTabExists(from)) {
                client.getPrivateChatWindow(from).appendToPane(output, from);
            }
            else {
                PrivateChatWindow pcw = new PrivateChatWindow(client, from);
                client.putPrivateChatWindow(from, pcw);
                pcw.appendToPane(output, from);
            }
        }
    }

    private void nickOk(String time) throws BadLocationException {
        ChatUtils.appendToPane(client.getMainTextPane(), time + "Welcome to fmIRC+!", Color.RED, Color.DARK_GRAY);
        client.setNickname(client.nick.getText());
        client.setWindowTitle("fmIRC+ - " + client.getNickname());
    }

    private void nickTaken(String time) throws BadLocationException, IOException {
        client.disconnect();
        ChatUtils.appendToPane(client.getMainTextPane(), time + "Nickname already in use, please pick another one!", Color.RED, Color.BLUE);
    }
    private void quit(String input, String time) throws BadLocationException {
        String name = input.replace("QUIT", "").trim();
        client.removeOnlineNameAndColor(name);
        client.updateOnlineList();
        ChatUtils.appendToPane(client.getMainTextPane(), time + name + " has left the chat!", Color.GREEN, Color.DARK_GRAY);
    }

    private void joined(String input, String time) throws BadLocationException {
        String name = input.replace("JOINED", "").trim();
        client.putOnlineNameAndColor(name);
        ChatUtils.appendToPane(client.getMainTextPane(), time + name + " has just joined the chat!", Color.MAGENTA, Color.CYAN);
        client.createPopupAsync(name + " just logged in!", "");
        client.updateOnlineList();
    }

    private void message(String input, String time) throws BadLocationException {
        int index = input.indexOf(":");
        String message = input.substring(index + 1, input.length());
        String name = input.substring(0, index).replace("MESSAGE", "").trim();
        ChatUtils.appendToPane(client.getMainTextPane(), time + "<" + name + ">: " + message, client.getColorByName(name), null);
        client.createPopupAsync("New Message", message);
    }
}
