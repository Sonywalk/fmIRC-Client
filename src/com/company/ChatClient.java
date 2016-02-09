package com.company;

import com.company.UI.Main;
import com.company.Util.ChatUtils;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.*;
import java.util.List;

/**
 * Created by LogiX on 2016-01-28.
 */

public class ChatClient extends Main implements ActionListener, KeyListener, MouseListener {

    private BufferedReader in;
    private BufferedWriter out;
    private HashMap<String, Color> onlineNameAndColor;
    private static HashMap<String, PrivateChatWindow> privateChatWindows;
    private Socket socket;
    private Stack<String> latestMessages;
    private int counter;
    private String myNickname;

    public ChatClient() throws IOException {
        counter = 0;
        latestMessages = new Stack<String>();
        privateChatWindows = new HashMap<>();
        latestMessages.push("");
        onlineNameAndColor = new HashMap<>();
        connectBtn.addActionListener(this);
        chat.addKeyListener(this);
        tfIp.addKeyListener(this);
        tfPort.addKeyListener(this);
        nick.addKeyListener(this);
        onlineList.addMouseListener(this);
    }

    private void disconnect() throws IOException {
        socket.close();
        chat.setText("");
        mainTextArea.setText("");
        model.clear();
        onlineNameAndColor.clear();
        socket = null;
        connectBtn.setIcon(new ImageIcon(connectImg));
        connectBtn.setToolTipText("Connect");
        window.setTitle("fmIRC+");
    }

    private void connect() throws BadLocationException, IOException {
        try {
            socket = new Socket(tfIp.getText(), Integer.parseInt(tfPort.getText()));
            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
            in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            createReaderThread();
            chat.requestFocus();
            connectBtn.setIcon(new ImageIcon(disconnectImg));
            connectBtn.setToolTipText("Disconnect");
        }
        catch (NumberFormatException e2) {
            ChatUtils.appendToPane(mainTextArea, "[" + ChatUtils.getTime() + "] *** Wrong port or address! ***", Color.RED, Color.YELLOW);
        }
        catch (UnknownHostException e) {
            ChatUtils.appendToPane(mainTextArea, "[" + ChatUtils.getTime() + "] *** Host not found! ***", Color.RED, Color.YELLOW);
        }
        catch (ConnectException e3) {
            ChatUtils.appendToPane(mainTextArea, "[" + ChatUtils.getTime() + "] *** Could not connect to host! ***", Color.RED, Color.YELLOW);
        }
        catch (IllegalArgumentException e4) {
            ChatUtils.appendToPane(mainTextArea, "[" + ChatUtils.getTime() + "] *** Wrong port or address! ***", Color.RED, Color.YELLOW);
        }
        catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    private void createReaderThread () {
        Thread read = new Thread(new Runnable() {
            public void run() {
                String line;
                try {
                    while((line = in.readLine()) != null) {
                        handleInput(line);
                    }
                    disconnect();
                } catch (IOException e) {
                    try {
                        disconnect();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                    e.printStackTrace();
                } catch (BadLocationException e) {
                    e.printStackTrace();
                }
            }
        });
        read.start();
    }

    private void createPopupAsync(final String header, final String paragraph) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if (!window.isActive()) {
                    Notification.getInstance(connectImg, header, paragraph).execute();
                }
            }
        });
    }

    private void handleInput(String str) throws BadLocationException, IOException {
        String time = "[" + ChatUtils.getTime() + "]";
        if (str.equals("NICK?")) {
            write("NICK " + nick.getText());
        }
        else if (str.equals("NICK OK")) {
            ChatUtils.appendToPane(mainTextArea, "Welcome to fmIRC+!", Color.RED, Color.DARK_GRAY);
            window.setTitle("fmIRC+ - " + nick.getText());
            myNickname = nick.getText();
        }
        else if (str.equals("NICK TAKEN")) {
            ChatUtils.appendToPane(mainTextArea, "Nickname already in use, please pick another one!", Color.RED, Color.BLUE);
        }
        else if (str.startsWith("JOINED")) {
            String name = str.replace("JOINED", "").trim();
            onlineNameAndColor.put(name, ChatUtils.getRandomColor());
            ChatUtils.appendToPane(mainTextArea, time + " " + name + " has just joined the chat!", Color.MAGENTA, Color.CYAN);
            createPopupAsync(name + " just logged in!", "");
            updateOnlineList();
        }
        else if (str.startsWith("MESSAGE")) {
            int index = str.indexOf(":");
            String message = str.substring(index + 1, str.length());
            String name = str.substring(0, index).replace("MESSAGE", "").trim();
            ChatUtils.appendToPane(mainTextArea, time + " <" + name + ">: " + message, onlineNameAndColor.get(name), null);
            createPopupAsync("New Message", message);
        }
        else if (str.startsWith("PRIVMSG")) {
            handlePrivateIncomingMessage(str);
        }
        else if (str.startsWith("RESPONSEPRIVMSG")) {
            handlePrivateIncomingMessageResponse(str);
        }
        else if (str.startsWith("QUIT")) {
            String name = str.replace("QUIT", "").trim();
            onlineNameAndColor.remove(name);
            updateOnlineList();
            ChatUtils.appendToPane(mainTextArea, time + " " + name + " has left the chat!", Color.GREEN, Color.DARK_GRAY);
        }
    }

    //"PRIVMSG from@to :message
    private void handlePrivateIncomingMessage(String input) throws BadLocationException, IOException {
        boolean found = false;
        int index = input.indexOf(":");
        String message = input.substring(index + 1, input.length());
        String[] fromAndTo = input.substring(0, index-1).replace("PRIVMSG", "").split("@");
        String from = fromAndTo[0].trim();
        String to = fromAndTo[1].trim();

        for (String key : privateChatWindows.keySet()) {
            if (key.equals(from)) {
                found = true;
            }
        }
        if (found) {
            privateChatWindows.get(from).appendToPane("[" + ChatUtils.getTime() + "]" + " <" + from + ">: " + message, from);
        }
        else {
            PrivateChatWindow pcw = new PrivateChatWindow(this, from);
            privateChatWindows.put(from, pcw);
            pcw.appendToPane("[" + ChatUtils.getTime() + "]" + " <" + from + ">: " + message, from);
        }
    }

    private void handlePrivateIncomingMessageResponse(String input) throws BadLocationException, IOException {
        boolean found = false;
        int index = input.indexOf(":");
        String message = input.substring(index + 1, input.length());
        String to = input.substring(0, index).replace("RESPONSEPRIVMSG", "").trim();

        for (String key : privateChatWindows.keySet()) {
            if (key.equals(to)) {
                found = true;
            }
        }
        if (found) {
            privateChatWindows.get(to).appendToPane("[" + ChatUtils.getTime() + "]" + " <" + myNickname + ">: " + message, myNickname);
        }
        else {
            PrivateChatWindow pcw = new PrivateChatWindow(this, to);
            privateChatWindows.put(to, pcw);
            pcw.appendToPane("[" + ChatUtils.getTime() + "]" + " <" + myNickname + ">: " + message, myNickname);
        }
    }

    private void updateOnlineList() {
        model.clear();
        List<String> sortedKeys = new ArrayList(onlineNameAndColor.keySet());
        Collections.sort(sortedKeys);
        for(String key : sortedKeys) {
            model.addElement(key); //This will be sorted alphabetical
        }
    }

    public void write(String msg) throws BadLocationException, IOException {
        if (out != null) {
            socket.setSoTimeout(5000);
            out.write(msg + "\r\n");
            out.flush();
            chat.setText("");
            socket.setSoTimeout(0);
        }
        else {
            System.out.println(System.nanoTime() + " FUCKED UP CLIENT");
            ChatUtils.appendToPane(mainTextArea, "[" + ChatUtils.getTime() + "] *** You are disconnected! ***", Color.RED, Color.YELLOW);
            chat.setText("");
        }
    }

    private void handleMessageHistory(int num) {
        counter += num;

        if (counter >= latestMessages.size()) {
            counter = 0;
        }
        else if (counter < 0) {
            counter = latestMessages.size()-1;
        }
        chat.setText(latestMessages.get(counter));
    }

    private void handlePrivateMessageTab() throws IOException {
        boolean found = false;
        String nickname = (String) onlineList.getSelectedValue();
        for (String key : privateChatWindows.keySet()) {
            if (key.equals(nickname)) {
                found = true;
                break;
            }
        }
        if (!found) {
            PrivateChatWindow pcw = new PrivateChatWindow(this, nickname);
            privateChatWindows.put(nickname, pcw);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == connectBtn) {
            if (socket != null) {
                try {
                    disconnect();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            else {
                try {
                    connect();
                } catch (BadLocationException e1) {
                    e1.printStackTrace();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {

    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getSource() == chat && e.getKeyCode() == KeyEvent.VK_ENTER) {
            try {
                latestMessages.push(chat.getText());
                counter = 0;
                write(chat.getText());
            } catch (BadLocationException e1) {
                e1.printStackTrace();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
        //If enter is pressed when filling the port field it will connect
        else if (e.getSource() == tfPort && socket == null && e.getKeyCode() == KeyEvent.VK_ENTER) {
            try {
                connect();
            } catch (BadLocationException e1) {
                e1.printStackTrace();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
        else if (e.getKeyCode() == KeyEvent.VK_UP && e.getSource() == chat) {
            handleMessageHistory(-1);
        }
        else if (e.getKeyCode() == KeyEvent.VK_DOWN && e.getSource() == chat) {
            handleMessageHistory(1);
        }
    }


    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getSource() == onlineList && e.getClickCount() == 2) {
            try {
                handlePrivateMessageTab();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    public static void removeTab(String tabValue) {
        privateChatWindows.remove(tabValue.replace("#", ""));
    }
}
