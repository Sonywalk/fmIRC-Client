package com.company;

import com.company.UI.CustomUI;
import com.company.Util.ChatUtils;
import com.company.Util.WrapEditorKit;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;

/**
 * Created by LanfeaR on 2016-02-08.
 */
public class PrivateChatWindow implements Runnable, KeyListener {

    private JTextPane textPane;
    private JTextField privateChat;
    private ChatClient client;
    private String nickname;
    private Color myColor;
    private Color buddyColor;

    public PrivateChatWindow(ChatClient client, String nickname) throws IOException {
        SwingUtilities.invokeLater(this);
        myColor = ChatUtils.getRandomColor();
        buddyColor = ChatUtils.getRandomColor();
        this.nickname = nickname;
        this.client = client;
    }

    private void createPrivateChatWindow() {
        Border blackBorder = BorderFactory.createLineBorder(Color.BLACK);
        Font consoleFont = new Font("Consolas", Font.BOLD, 12);
        Font font = new Font("Arial", Font.BOLD, 12);

        textPane = new JTextPane();
        textPane.setFont(consoleFont);
        textPane.setEditable(false);
        textPane.setBorder(blackBorder);
        textPane.setBackground(Color.BLACK);
        textPane.setEditorKit(new WrapEditorKit());

        JScrollPane scrollableTextArea = new JScrollPane(textPane);
        scrollableTextArea.setBackground(Color.BLACK);
        scrollableTextArea.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(5, 0, 5, 5), BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        scrollableTextArea.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollableTextArea.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollableTextArea.getHorizontalScrollBar().setUI(CustomUI.getCustomScrollUI());
        scrollableTextArea.getVerticalScrollBar().setUI(CustomUI.getCustomScrollUI());
        DefaultCaret caret = (DefaultCaret) textPane.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

        privateChat = new JTextField();
        privateChat.setFont(font);
        privateChat.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0), BorderFactory.createEmptyBorder(2, 2, 2, 2)));
        privateChat.addKeyListener(this);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(scrollableTextArea, BorderLayout.CENTER);
        panel.add(privateChat, BorderLayout.SOUTH);

        ChatUtils.addTab("#" + nickname, panel, client.tabbedPane);
    }

    public void appendToPane(String msg, String writer) throws BadLocationException {
        Color foreground;
        if (writer.equals(nickname)) {
            foreground = buddyColor;
        }
        else {
            foreground = myColor;
        }
        SwingUtilities.invokeLater(() -> {
            try {
                ChatUtils.appendToPane(textPane, msg, foreground, null);
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
        });
    }

    private void sendPrivateMessage() throws IOException, BadLocationException {
        if (privateChat.getText().equals("/list")) {
            client.write("LIST " + nickname);
        }
        else {
            client.write("PRIVMSG " + nickname + " :" + privateChat.getText());
            privateChat.setText("");
        }
    }

    @Override
    public void run() {
        createPrivateChatWindow();
    }

    @Override
    public void keyTyped(KeyEvent e) {}
    @Override
    public void keyPressed(KeyEvent e) {}

    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getSource() == privateChat && e.getKeyCode() == KeyEvent.VK_ENTER) {
            try {
                sendPrivateMessage();
            } catch (IOException e1) {
                e1.printStackTrace();
            } catch (BadLocationException e1) {
                e1.printStackTrace();
            }
        }
    }
}
