package se.lanfear.chatclient;

import se.lanfear.entities.AppendEntity;
import se.lanfear.entities.JoinedEntity;
import se.lanfear.entities.MessageEntity;
import se.lanfear.entities.QuitEntity;
import se.lanfear.chatclient.util.ChatUtils;
import se.lanfear.chatclient.util.Constants;

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

    public void processInput(String input) throws IOException {
        System.out.println(input);
        String time = "[" + ChatUtils.getTime() + "] ";
        if (input.equals("NICK?")) {
            client.write("NICK " + client.getNickname());
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
        else if (input.startsWith("MSG")) {
            message(input);
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
        else if (input.startsWith("LIST")) {
            list(input);
        }
        else if (input.startsWith("<") || input.startsWith(">")) {
            serverMessage(input);
        }
    }

    private void serverMessage(String input) {
        MessageEntity entity = new MessageEntity("", "<", input);
        client.incomingMessage(entity);
    }

    private void sendFile(String input) throws IOException {
        int index = input.indexOf(":");
        String filename = input.substring(index + 1, input.length());
        String to = input.substring(0, index).replace("GET", "").trim();
        File f = new File(Constants.SHARED_PATH + "/" + filename);
        if (!f.exists()) {
            return;
        }
        String size = Long.toString(f.length());
        new FileSender(filename, size, client).execute();
        client.write("SENDING " + to + " :" + filename + " /" + size);
    }

    private void receiveFile(String input) throws IOException {
        String filename = input.substring(input.indexOf(":") + 1, input.indexOf("/")).trim();
        String size = input.substring(input.indexOf("/") + 1, input.length());
        new FileReceiver(filename, size, client).execute();
    }

    //"MSG from@to :message
    private void message(String input) throws IOException {
        int index = input.indexOf(":");
        String message = input.substring(index + 1, input.length());
        String[] fromAndTo = input.substring(0, index-1).replace("MSG", "").split("@");
        String from = fromAndTo[0].trim();
        String to = fromAndTo[1].trim();

        MessageEntity entity = new MessageEntity(to, from, message);
        client.incomingMessage(entity);
    }

    private void nickOk(String time) {
        client.appendToPane(new AppendEntity(Color.RED, Color.DARK_GRAY, time + "Welcome to fmIRC+!"));
        client.setWindowTitle("fmIRC+ - " + client.getNickname());
    }

    private void nickTaken(String time) throws IOException {
        client.disconnect();
        client.appendToPane(new AppendEntity(Color.DARK_GRAY, Color.RED, time + "Nickname already in use, please pick another one!"));
    }
    private void quit(String input, String time) {
        String[] parts = input.split(" ");
        client.quit(new QuitEntity(parts[1], parts[2]));
    }

    private void joined(String input, String time) {
        String[] parts = input.split(" "); // input will be JOINED [channel] [nickname]
        client.joined(new JoinedEntity(parts[1], parts[2]));
    }

    private void list(String input) {
        try {
            String to = input.replace("LIST", "").trim();
            File folder = new File(Constants.SHARED_PATH);
            File[] listOfFiles = folder.listFiles();
            if (listOfFiles == null) {
                client.write("LISTBACK " + to + " :***** NO LIST *****");
                return;
            }
            client.write("LISTBACK " + to + " :*********************************************");
            for (int i = 0; i < listOfFiles.length; i++) {
                if (listOfFiles[i].isFile()) {
                    client.write("LISTBACK " + to + " :" + listOfFiles[i].length() + "\t" + listOfFiles[i].getName());
                }
            }
            client.write("LISTBACK " + to + " :*********************************************");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
