package se.lanfear.observers.view.tab;

import se.lanfear.chatclient.ChatClient;
import se.lanfear.entities.MessageEntity;
import se.lanfear.observers.view.CustomUI;
import se.lanfear.observers.view.WrapEditorKit;
import se.lanfear.observers.view.GUI;
import se.lanfear.entities.AppendEntity;
import se.lanfear.entities.JoinedEntity;
import se.lanfear.entities.QuitEntity;
import se.lanfear.chatclient.util.*;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.util.*;

public class ChannelTab extends Tab implements MouseListener {
    private HashMap<String, Color> onlineNameAndColor;
    private DefaultListModel model;
    private JList onlineList;

    public ChannelTab(String id, ChatClient chatClient, GUI gui) throws IOException {
        super(id, chatClient, gui);
    }

    public void joined(JoinedEntity entity) {
        onlineNameAndColor.put(entity.getNickname(), ChatUtils.getRandomColor());
        String text = "[" + entity.getTime() + "] " + entity.getNickname() + " has just joined the channel";
        appendToPane(new AppendEntity(Color.MAGENTA, Color.CYAN, text));
        updateOnlineList();
    }

    public void quit(QuitEntity entity) {
        onlineNameAndColor.remove(entity.getNickname());
        String text = "[" + entity.getTime() + "] " + entity.getNickname() + " has left the channel";
        appendToPane(new AppendEntity(Color.RED, Color.DARK_GRAY, text));
        updateOnlineList();
    }

    private void updateOnlineList() {
        model.clear();
        java.util.List<String> sortedKeys = new ArrayList(onlineNameAndColor.keySet());
        Collections.sort(sortedKeys);
        for(String key : sortedKeys) {
            model.addElement(key); //This will be sorted alphabetical
        }
    }

    @Override
    public void message(MessageEntity entity) {
        String msg = "[" + entity.getTime() + "] <" + entity.getFrom() + "> " + entity.getMessage();
        appendToPane(new AppendEntity(onlineNameAndColor.get(entity.getFrom()), null, msg));
    }

    @Override
    protected void createTab() {
        onlineNameAndColor = new HashMap<>();
        model = new DefaultListModel();
        onlineList = new JList(model);
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

        JLabel onlineLab = new JLabel("ONLINE LIST");
        onlineLab.setFont(consoleFont);
        onlineLab.setForeground(Color.YELLOW);
        onlineLab.setHorizontalAlignment(JLabel.CENTER);
        onlineLab.setVerticalAlignment(JLabel.CENTER);

        onlineList.setSelectionBackground(Color.DARK_GRAY);
        onlineList.setSelectionForeground(Color.WHITE);
        onlineList.setFixedCellWidth(150);
        onlineList.setFont(consoleFont);
        onlineList.setBackground(Color.BLACK);
        onlineList.setForeground(Color.WHITE);
        onlineList.setBorder(blackBorder);

        JScrollPane scrollableOnlineList = new JScrollPane(onlineList);
        scrollableOnlineList.setBackground(Color.BLACK);
        scrollableOnlineList.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(3, 8, 5, 0), BorderFactory.createEmptyBorder(2, 2, 2, 2)));
        scrollableOnlineList.getHorizontalScrollBar().setPreferredSize(new Dimension(0, 0));
        scrollableOnlineList.getHorizontalScrollBar().setMinimumSize(new Dimension(0, 0));
        scrollableOnlineList.getHorizontalScrollBar().setMaximumSize(new Dimension(0, 0));
        scrollableOnlineList.getVerticalScrollBar().setPreferredSize(new Dimension(0, 0));
        scrollableOnlineList.getVerticalScrollBar().setMinimumSize(new Dimension(0, 0));
        scrollableOnlineList.getVerticalScrollBar().setMaximumSize(new Dimension(0, 0));

        JPanel eastPanel = new JPanel(new BorderLayout());
        eastPanel.setBackground(Color.BLACK);
        eastPanel.add(onlineLab, BorderLayout.NORTH);
        eastPanel.add(scrollableOnlineList, BorderLayout.CENTER);
        onlineList.addMouseListener(this);

        chat = new JTextField();
        chat.setFont(font);
        chat.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0), BorderFactory.createEmptyBorder(2, 2, 2, 2)));
        chat.addKeyListener(this);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(scrollableTextArea, BorderLayout.CENTER);
        centerPanel.add(chat, BorderLayout.SOUTH);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(eastPanel, BorderLayout.EAST);
        panel.add(centerPanel, BorderLayout.CENTER);
        gui.addTab(id, panel);
    }

    @Override
    protected void write() {
        chatClient.write("MSG " + id + " :" + chat.getText());
        chat.setText("");
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getSource() == onlineList && e.getClickCount() == 2) {
            gui.createNewPrivateMessageTab((String) onlineList.getSelectedValue());
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
}
