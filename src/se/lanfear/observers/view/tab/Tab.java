package se.lanfear.observers.view.tab;

import se.lanfear.chatclient.ChatClient;
import se.lanfear.chatclient.util.ChatUtils;
import se.lanfear.entities.AppendEntity;
import se.lanfear.entities.MessageEntity;
import se.lanfear.observers.view.CustomUI;
import se.lanfear.observers.view.GUI;
import se.lanfear.observers.view.WrapEditorKit;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Stack;

public class Tab implements Runnable, KeyListener {

    protected JTextPane textPane;
    protected JTextField chat;
    protected GUI gui;
    protected String id;
    protected Color myColor;
    protected ChatClient chatClient;
    protected int counter;
    protected Stack<String> latestMessages;

    public Tab(String id, ChatClient chatClient, GUI gui) {
        this.gui = gui;
        this.chatClient = chatClient;
        myColor = ChatUtils.getRandomColor();
        this.id = id;
        latestMessages = new Stack<>();
        latestMessages.push("");
        SwingUtilities.invokeLater(this);
    }

    public void message(MessageEntity entity) {
        String msg = "[" + entity.getTime() + "] <" + entity.getFrom() + "> " + entity.getMessage();
        appendToPane(new AppendEntity(myColor, null, msg));
    }

    protected void createTab() {
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

        chat = new JTextField();
        chat.setFont(font);
        chat.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0), BorderFactory.createEmptyBorder(2, 2, 2, 2)));
        chat.addKeyListener(this);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(scrollableTextArea, BorderLayout.CENTER);
        mainPanel.add(chat, BorderLayout.SOUTH);
        gui.addTab(id, mainPanel);
    }

    //Pass null as backgroundColor or foregroundColor and it will return a default value
    public void appendToPane(AppendEntity entity) {
        Color backgroundColor;
        if (entity.getBackgroundColor() == null) {
            backgroundColor = new Color(0, 0, 0, 1);
        }
        else {
            backgroundColor = entity.getBackgroundColor();
        }
        try {
            //Some calls to this function may be on EDT already, if not run it on EDT
            if (SwingUtilities.isEventDispatchThread()) {
                StyledDocument doc = textPane.getStyledDocument();
                SimpleAttributeSet sas = new SimpleAttributeSet();
                StyleConstants.setForeground(sas, entity.getForegroundColor());
                StyleConstants.setBackground(sas, backgroundColor);
                doc.insertString(doc.getLength(), entity.getText() + "\n", sas);
            }
            else {
                SwingUtilities.invokeLater(() -> {
                    try {
                        StyledDocument doc = textPane.getStyledDocument();
                        SimpleAttributeSet sas = new SimpleAttributeSet();
                        StyleConstants.setForeground(sas, entity.getForegroundColor());
                        StyleConstants.setBackground(sas, backgroundColor);
                        doc.insertString(doc.getLength(), entity.getText() + "\n", sas);
                    } catch (BadLocationException e) {
                        e.printStackTrace();
                    }
                });
            }
        }
        catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    protected void write() {
        chatClient.write(chat.getText());
        chat.setText("");
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


    @Override
    public void run() {
        createTab();
    }

    @Override
    public void keyTyped(KeyEvent e) {}
    @Override
    public void keyPressed(KeyEvent e) {}

    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getSource() == chat && e.getKeyCode() == KeyEvent.VK_ENTER) {
            latestMessages.push(chat.getText());
            write();
        }
        if (e.getSource() == chat && e.getKeyCode() == KeyEvent.VK_DOWN) {
            handleMessageHistory(1);
        }
        if (e.getSource() == chat && e.getKeyCode() == KeyEvent.VK_UP) {
            handleMessageHistory(-1);
        }
    }
}
