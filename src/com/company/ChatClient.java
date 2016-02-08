package com.company;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.*;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

/**
 * Created by LogiX on 2016-01-28.
 */

public class ChatClient extends Main implements ActionListener, KeyListener {

    private BufferedReader in;
    private BufferedWriter out;
    private HashMap<String, Color> onlineNameAndColor;
    private Socket socket;
    private volatile boolean running = true;
    private Stack<String> latestMessages;
    private int counter;

    public ChatClient() throws IOException {
        counter = 0;
        latestMessages = new Stack<String>();
        latestMessages.push("");
        onlineNameAndColor = new HashMap<>();
        connectBtn.addActionListener(this);
        chat.addKeyListener(this);
        tfIp.addKeyListener(this);
        tfPort.addKeyListener(this);
        nick.addKeyListener(this);
    }

    private void disconnect() throws IOException {
        running = false;
        socket.close();
        chat.setText("");
        mainTextArea.setText("");
        model.clear();
        onlineNameAndColor.clear();
        socket = null;
        connectBtn.setIcon(new ImageIcon(connectImg));
        connectBtn.setToolTipText("Connect");
        window.setTitle("Linkura Chat Client");
    }

    private void connect() throws BadLocationException, IOException {
        try {
            socket = new Socket(tfIp.getText(), Integer.parseInt(tfPort.getText()));
            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
            in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            running = true;
            createReaderThread();
            chat.requestFocus();
            connectBtn.setIcon(new ImageIcon(disconnectImg));
            connectBtn.setToolTipText("Disconnect");
        }
        catch (NumberFormatException e2) {
            appendToPane(mainTextArea, "[" + getTime() + "] *** Wrong port or address! ***", Color.RED, Color.YELLOW);
        }
        catch (UnknownHostException e) {
            appendToPane(mainTextArea, "[" + getTime() + "] *** Host not found! ***", Color.RED, Color.YELLOW);
        }
        catch (ConnectException e3) {
            appendToPane(mainTextArea, "[" + getTime() + "] *** Could not connect to host! ***", Color.RED, Color.YELLOW);
        }
        catch (IllegalArgumentException e4) {
            appendToPane(mainTextArea, "[" + getTime() + "] *** Wrong port or address! ***", Color.RED, Color.YELLOW);
        }
        catch (IOException e1) {
            disconnect();
        }
    }

    private void createReaderThread () {
        Thread read = new Thread(new Runnable() {
            public void run() {
                String line;
                while(running) {
                    try {
                        line = in.readLine();
                        handleInput(line);
                    }
                    catch (SocketException e1) {
                        //When disconnecting read might be called, this exception will occur then, ignore it
                    }
                    catch (Exception e) { //Remote server probably closed socket
                        try {
                            running = false;
                            socket.close();
                            appendToPane(mainTextArea, "[" + getTime() + "] *** You have been disconnected! ***", Color.RED, Color.YELLOW);
                        } catch (BadLocationException e1) {
                            e1.printStackTrace();
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    }
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
        String time = "[" + getTime() + "]";
        if (str.equals("NICK?")) {
            write("NICK " + nick.getText());
        }
        else if (str.equals("NICK OK")) {
            appendToPane(mainTextArea, "Welcome to Linkura Chat!", Color.RED, Color.DARK_GRAY);
            window.setTitle("Linkura Chat Client - " + nick.getText());
        }
        else if (str.equals("NICK TAKEN")) {
            appendToPane(mainTextArea, "Nickname already in use, please pick another one!", Color.RED, Color.BLUE);
        }
        else if (str.startsWith("JOINED")) {
            String name = str.replace("JOINED", "").trim();
            onlineNameAndColor.put(name, getRandomColor());
            appendToPane(mainTextArea, time + " " + name + " has just joined the chat!", Color.MAGENTA, Color.CYAN);
            createPopupAsync(name + " just logged in!", "");
            updateOnlineList();
        }
        else if (str.startsWith("MESSAGE")) {
            int index = str.indexOf(":");
            String message = str.substring(index + 1, str.length());
            String name = str.substring(0, index).replace("MESSAGE", "").trim();
            appendToPane(mainTextArea, time + " <" + name + ">: " + message, onlineNameAndColor.get(name), null);
            createPopupAsync("New Message", message);
        }
        else if (str.startsWith("QUIT")) {
            String name = str.replace("QUIT", "").trim();
            onlineNameAndColor.remove(name);
            updateOnlineList();
            appendToPane(mainTextArea, time + " " + name + " has left the chat!", Color.GREEN, Color.DARK_GRAY);
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

    private String getTime() {
        Date date = Calendar.getInstance().getTime();
        SimpleDateFormat sdt = new SimpleDateFormat("HH:mm:ss");
        return sdt.format(date);
    }

    private Color getRandomColor() {
        Random random = new Random();
        final float hue = random.nextFloat();
        final float saturation = 0.9f; //1.0 for brilliant, 0.0 for dull
        final float luminance = 1.0f; //1.0 for brighter, 0.0 for black
        return Color.getHSBColor(hue, saturation, luminance);
    }

    //Pass null as backgroundColor or foregroundColor and it will return a default value
    private void appendToPane(JTextPane tp, String msg, Color foregroundColor, Color backgroundColor) throws BadLocationException {
        if (foregroundColor == null) {
            foregroundColor = Color.RED;
        }
        if (backgroundColor == null) {
            backgroundColor = new Color(0, 0, 0, 1); //Transparent
        }

        StyledDocument doc = tp.getStyledDocument();
        SimpleAttributeSet sas = new SimpleAttributeSet();
        StyleConstants.setForeground(sas, foregroundColor);
        StyleConstants.setBackground(sas, backgroundColor);
        doc.insertString(doc.getLength(), msg + "\n", sas);
    }

    private void write(String msg) throws BadLocationException, IOException {
        if (out != null) {
            socket.setSoTimeout(5000);
            out.write(msg + "\r\n");
            out.flush();
            chat.setText("");
            socket.setSoTimeout(0);
        }
        else {
            appendToPane(mainTextArea, "[" + getTime() + "] *** You are disconnected! ***", Color.RED, Color.YELLOW);
            chat.setText("");
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
}
