package com.company;

import com.company.UI.Main;
import com.company.Util.ChatUtils;
import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.List;

/**
 * Created by LogiX on 2016-01-28.
 */

public class ChatClient extends Main implements ActionListener, KeyListener, MouseListener {

    private BufferedReader in;
    public BufferedWriter out;
    private HashMap<String, Color> onlineNameAndColor;
    private static HashMap<String, PrivateChatWindow> privateChatWindows;
    private Socket socket;
    private Stack<String> latestMessages;
    private int counter;
    private String myNickname;
    private String address;

    public ChatClient() throws IOException {
        counter = 0;
        latestMessages = new Stack<>();
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

    public void disconnect() throws IOException {
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
            this.address = tfIp.getText();
            socket = new Socket();
            socket.connect(new InetSocketAddress(address, Integer.parseInt(tfPort.getText())), 2000);
            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
            in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            createReaderThread();
            chat.requestFocus();
            connectBtn.setIcon(new ImageIcon(disconnectImg));
            connectBtn.setToolTipText("Disconnect");
        }
        catch (SocketTimeoutException e5) {
            disconnect();
            ChatUtils.appendToPane(mainTextArea, "[" + ChatUtils.getTime() + "] *** Socket Timeout! ***", Color.RED, Color.YELLOW);
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
        InputHelper helper = new InputHelper(this);
        Thread read = new Thread(() -> {
            String line;
            try {
                while((line = in.readLine()) != null) {
                    helper.processInput(line);
                }
                disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
        });
        read.start();
    }

    public void createPopupAsync(final String header, final String paragraph) {
        SwingUtilities.invokeLater(() -> {
            if (!window.isActive()) {
                Notification.getInstance(connectImg, header, paragraph).execute();
            }
        });
    }

    public void setWindowTitle(String title) {
        window.setTitle(title);
    }
    public String getNickname() {
        return this.myNickname;
    }
    public void setNickname(String nickname) {
        this.myNickname = nickname;
    }
    public JTextPane getMainTextPane() {
        return mainTextArea;
    }
    public void putOnlineNameAndColor(String key) {
        onlineNameAndColor.put(key, ChatUtils.getRandomColor());
    }
    public Color getColorByName(String key) {
        return onlineNameAndColor.get(key);
    }
    public void removeOnlineNameAndColor(String key) {
        onlineNameAndColor.remove(key);
    }
    public PrivateChatWindow getPrivateChatWindow(String key) {
        return privateChatWindows.get(key);
    }
    public void putPrivateChatWindow(String key, PrivateChatWindow value) {
        privateChatWindows.put(key, value);
    }
    public String getAddress() {
        return this.address;
    }

    public void updateOnlineList() {
        model.clear();
        List<String> sortedKeys = new ArrayList(onlineNameAndColor.keySet());
        Collections.sort(sortedKeys);
        for(String key : sortedKeys) {
            model.addElement(key); //This will be sorted alphabetical
        }
    }

    public void write(String msg) throws BadLocationException, IOException {
        if (socket != null) {
            socket.setSoTimeout(5000);
            out.write(msg + "\r\n");
            out.flush();
            socket.setSoTimeout(0);
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
        String nickname = (String) onlineList.getSelectedValue();
        if (!messageTabExists(nickname)) {
            PrivateChatWindow pcw = new PrivateChatWindow(this, nickname);
            privateChatWindows.put(nickname, pcw);
        }
    }

    public boolean messageTabExists(String tabName) {
        for (String key : privateChatWindows.keySet()) {
            if (key.equals(tabName)) {
                return true;
            }
        }
        return false;
    }

    public static void removeTab(String tabValue) {
        privateChatWindows.remove(tabValue.substring(1, tabValue.length()));
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
    public void keyTyped(KeyEvent e) {}
    @Override
    public void keyPressed(KeyEvent e) {}

    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getSource() == chat && e.getKeyCode() == KeyEvent.VK_ENTER) {
            try {
                latestMessages.push(chat.getText());
                counter = 0;
                write(chat.getText());
                chat.setText("");
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
    public void mousePressed(MouseEvent e) {}
    @Override
    public void mouseReleased(MouseEvent e) {}
    @Override
    public void mouseEntered(MouseEvent e) {}
    @Override
    public void mouseExited(MouseEvent e) {}
}
