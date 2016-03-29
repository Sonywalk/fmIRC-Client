package se.lanfear.chatclient;

import se.lanfear.chatclient.util.Constants;
import se.lanfear.observers.ChatListener;
import se.lanfear.entities.*;
import se.lanfear.chatclient.util.ChatUtils;
import se.lanfear.chatclient.util.Event;

import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.ArrayList;

public class ChatClient {
    private ArrayList<ChatListener> listeners;
    private BufferedReader in;
    public BufferedWriter out;
    private Socket socket;
    private String nickname;
    private String address;

    public ChatClient() {
        File sharedDir = new File(Constants.SHARED_PATH);

        if(!sharedDir.exists()){
            sharedDir.mkdir();
        }
        listeners = new ArrayList<>();
    }

    public void disconnect() throws IOException {
        if (socket != null) {
            socket.close();
            socket = null;
        }
    }

    public void connect(ConnectEntity entity) {
        try {
            setNickname(entity.getNickname());
            this.address = entity.getAddress().getHostName();
            socket = new Socket();
            socket.connect(entity.getAddress(), 2000);
            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
            in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            createReaderThread();
        }
        catch (SocketTimeoutException e) {
            notifyObservers(Event.DISCONNECT, null);
            AppendEntity appendEntity = new AppendEntity(Color.YELLOW, Color.RED, "[" + ChatUtils.getTime() + "] *** Socket Timeout! ***");
            appendToPane(appendEntity);
        }
        catch (SocketException e2) {
            notifyObservers(Event.DISCONNECT, null);
            AppendEntity appendEntity = new AppendEntity(Color.YELLOW, Color.RED, "[" + ChatUtils.getTime() + "] *** Could not connect! ***");
            appendToPane(appendEntity);
        }
        catch (IOException e1) {
            notifyObservers(Event.DISCONNECT, null);
            e1.printStackTrace();
        }
    }

    private void createReaderThread () {
        InputHelper helper = new InputHelper(this);
        Thread read = new Thread(() -> {
            String line;
            try {
                while((line = in.readLine()) != null) {
                    helper.processInput(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        read.start();
    }

    public void write(String msg) {
        if (socket != null) {
            try {
                socket.setSoTimeout(5000);
                out.write(msg + "\r\n");
                out.flush();
                socket.setSoTimeout(0);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void setWindowTitle(String title) {
        notifyObservers(Event.SET_TITLE, title);
    }
    public String getNickname() {
        return this.nickname;
    }
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
    public String getAddress() {
        return this.address;
    }

    public void quit(QuitEntity entity) {
        notifyObservers(Event.QUIT, entity);
    }
    public void joined(JoinedEntity entity) {
        notifyObservers(Event.JOINED, entity);
    }
    public void createPopup(String header, String paragraph) {
        notifyObservers(Event.CREATE_POPUP, new PopupEntity(header, paragraph));
    }
    public void appendToPane(AppendEntity appendEntity) {
        notifyObservers(Event.APPEND_TEXT, appendEntity);
    }
    public void incomingMessage(MessageEntity entity) {
        notifyObservers(Event.INCOMING_MESSAGE, entity);
    }


    public void register(ChatListener l) {
        listeners.add(l);
    }

    public void unregister(ChatListener l) {
        listeners.remove(l);
    }
    public void notifyObservers(Event e, Object obj) {
        for (ChatListener l : listeners) {
            l.update(e, obj);
        }
    }
}
