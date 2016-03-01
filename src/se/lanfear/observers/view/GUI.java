package se.lanfear.observers.view;

import se.lanfear.chatclient.ChatClient;
import se.lanfear.observers.ChatListener;
import se.lanfear.observers.view.tab.ChannelTab;
import se.lanfear.observers.view.tab.ConsoleTab;
import se.lanfear.observers.view.tab.PrivateChatTab;
import se.lanfear.observers.view.tab.Tab;
import se.lanfear.entities.*;
import se.lanfear.chatclient.util.Constants;
import se.lanfear.chatclient.util.Event;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;

public class GUI extends JFrame implements Runnable, ChatListener, ActionListener, KeyListener {

    private JButton connectBtn;
    private JTextField tfPort;
    private JTextField tfIp;
    private JTextField nick;

    private Image connectImg;
    private Image disconnectImg;

    private JTabbedPane tabbedPane;
    private int tabCount = 0;
    private ChatClient chatClient;
    private String nickname;
    private HashMap<String, Tab> openTabsMap;

    public GUI(ChatClient chatClient) {
        openTabsMap = new HashMap<>();
        this.chatClient = chatClient;
        SwingUtilities.invokeLater(this);
    }

    private void incomingMessage(MessageEntity entity) {
        String from = entity.getFrom();
        String to = entity.getTo();

        if (to.startsWith("#")) {
            if (tabExists(to)) {
                openTabsMap.get(to).message(entity);
            }
        }
        else if (from.equals("<")) {
            openTabsMap.get(Constants.CONSOLE_TAB).message(entity);
        }
        else {
            if (from.equals(nickname)) {
                if (tabExists(to)) {
                    openTabsMap.get(to).message(entity);
                }
                else {
                    createNewPrivateMessageTab(from).message(entity);
                }
            }
            else {
                createNewPrivateMessageTab(from).message(entity);
            }
        }
    }

    /*
    this method will create a new tab if it does not exists, if it exists it will return it.
    incomingMessage() method uses the returned tab to write a message to it.
     */
    public PrivateChatTab createNewPrivateMessageTab(String tabTitle) {
        if (!tabExists(tabTitle)) {
            PrivateChatTab pcw = new PrivateChatTab(tabTitle, chatClient, this);
            openTabsMap.put(tabTitle, pcw);
            return pcw;
        }
        else {
            return (PrivateChatTab) openTabsMap.get(tabTitle);
        }
    }

    public boolean tabExists(String tabName) {
        for (String key : openTabsMap.keySet()) {
            if (key.equals(tabName)) {
                return true;
            }
        }
        return false;
    }

    public void removeTab(String tabValue) {
        openTabsMap.remove(tabValue);
        tabCount--;
        if (tabValue.startsWith("#")) {
            chatClient.write("QUIT " + tabValue + " " + nickname);
        }
    }

    private void createAndShowGUI() throws IOException {
        //Set the active tab background to white
        UIManager.put("TabbedPane.selected", Color.DARK_GRAY);
        //Set the inactive tab background to black
        UIManager.put("TabbedPane.unselectedTabBackground", Color.BLACK);
        //Remove borders
        UIManager.put("TabbedPane.contentBorderInsets", new Insets(0, 0, 0, 0));

        connectImg = ImageIO.read(getClass().getResource("/resources/connect.png")).getScaledInstance(22, 22, Image.SCALE_SMOOTH);
        disconnectImg = ImageIO.read(getClass().getResource("/resources/disconnect.gif")).getScaledInstance(22, 22, Image.SCALE_SMOOTH);

        Border blackBorder = BorderFactory.createLineBorder(Color.BLACK);
        Font font = new Font("Arial", Font.BOLD, 12);
        Font consoleFont = new Font("Consolas", Font.BOLD, 12);

        setIconImage(connectImg);
        setTitle("fmIRC+");
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        setBounds(100, 100, (int) dim.getWidth(), (int) dim.getHeight());
        setLocationRelativeTo(null);
        setPreferredSize(dim);
        setVisible(true);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        JLabel ipLab = new JLabel("IP address: ");
        JLabel portLab = new JLabel("Port: ");
        JLabel nickLab = new JLabel("Nickname: ");
        ipLab.setFont(consoleFont);
        portLab.setFont(consoleFont);
        nickLab.setFont(consoleFont);
        ipLab.setForeground(Color.WHITE);
        portLab.setForeground(Color.WHITE);
        nickLab.setForeground(Color.WHITE);

        tfIp = new JTextField(10);
        tfIp.setBorder(blackBorder);
        tfIp.setFont(font);

        tfPort = new JTextField(10);
        tfPort.setBorder(blackBorder);
        tfPort.setFont(font);

        nick = new JTextField(10);
        nick.setBorder(blackBorder);
        nick.setFont(font);

        connectBtn = new JButton();
        connectBtn.setToolTipText("Connect");
        connectBtn.setMargin(new Insets(1, 3, 1, 3));
        connectBtn.setBackground(Color.BLACK);
        connectBtn.setIcon(new ImageIcon(connectImg));

        JPanel leftInnerPanel = new JPanel();
        leftInnerPanel.setBackground(Color.BLACK);
        leftInnerPanel.add(connectBtn);

        JPanel rightInnerPanel = new JPanel();
        rightInnerPanel.setBackground(Color.BLACK);
        rightInnerPanel.add(nickLab);
        rightInnerPanel.add(nick);
        rightInnerPanel.add(ipLab);
        rightInnerPanel.add(tfIp);
        rightInnerPanel.add(portLab);
        rightInnerPanel.add(tfPort);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Color.BLACK);
        topPanel.add(rightInnerPanel, BorderLayout.CENTER);
        topPanel.add(leftInnerPanel, BorderLayout.WEST);

        tabbedPane = new JTabbedPane();
        tabbedPane.setUI(CustomUI.getCustomTabbedUI());
        tabbedPane.setForeground(Color.WHITE);
        tabbedPane.setBackground(Color.BLACK);

        JPanel mainCenterPanel = new JPanel(new BorderLayout());
        mainCenterPanel.setBackground(Color.BLACK);
        mainCenterPanel.add(topPanel, BorderLayout.NORTH);
        mainCenterPanel.add(tabbedPane, BorderLayout.CENTER);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.BLACK);
        mainPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10), BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        mainPanel.add(mainCenterPanel, BorderLayout.CENTER);

        connectBtn.addActionListener(this);
        tfIp.addKeyListener(this);
        tfPort.addKeyListener(this);
        nick.addKeyListener(this);

        add(mainPanel);
        setVisible(true);
        pack();
        ConsoleTab console = new ConsoleTab(Constants.CONSOLE_TAB, chatClient, this);
        openTabsMap.put(Constants.CONSOLE_TAB, console);
    }

    public void addTab(String title, JPanel panel) {
        SwingUtilities.invokeLater(() -> {
            tabbedPane.add(title, new JLabel(title));
            tabbedPane.setTabComponentAt(tabCount, new ButtonTabComponent(tabbedPane, this));
            tabbedPane.setComponentAt(tabCount, panel);
            tabCount++;
        });
    }

    @Override
    public void run() {
        try {
            createAndShowGUI();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void createPopupAsync(PopupEntity entity) {
        SwingUtilities.invokeLater(() -> {
            if (!isActive()) {
                Notification.getInstance(connectImg, entity.getHeader(), entity.getParagraph()).execute();
            }
        });
    }

    @Override
    public void update(Event e, Object obj) {
        switch (e) {
            case DISCONNECT:
                disconnect();
                break;
            case SET_TITLE:
                updateTitle((String) obj);
                break;
            case APPEND_TEXT:
                //System message
                openTabsMap.get(Constants.CONSOLE_TAB).appendToPane((AppendEntity) obj);
                break;
            case INCOMING_MESSAGE:
                incomingMessage((MessageEntity) obj);
                break;
            case QUIT:
                quit((QuitEntity) obj);
                break;
            case JOINED:
                joined((JoinedEntity) obj);
                break;
            case CREATE_POPUP:
                createPopupAsync((PopupEntity) obj);
                break;
        }
    }

    private void updateTitle(String title) {
        SwingUtilities.invokeLater(() -> setTitle(title));
    }

    private void joined(JoinedEntity entity) {
        if (!tabExists(entity.getChannel())) {
            try {
                ChannelTab channelTab = new ChannelTab(entity.getChannel(), chatClient, this);
                openTabsMap.put(entity.getChannel(), channelTab);
                channelTab.joined(entity);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else {
            ((ChannelTab) openTabsMap.get(entity.getChannel())).joined(entity);
        }
        createPopupAsync(new PopupEntity(entity.getNickname() + " has just joined " + entity.getChannel(), ""));
    }
    private void quit(QuitEntity entity) {
        if (tabExists(entity.getChannel())) {
            ((ChannelTab)openTabsMap.get(entity.getChannel())).quit(entity);
        }
    }

    private void disconnect() {
        try {
            chatClient.disconnect();
            SwingUtilities.invokeLater(() -> {
                /*An for-each on the maps key set can produce concurrentModificationException
                get the array from the map and iterate that instead.*/
                Object[] keys = openTabsMap.keySet().toArray();
                for (int i = 0; i < keys.length; i++) {
                    if (!keys[i].equals(Constants.CONSOLE_TAB)) {
                        tabbedPane.remove(tabCount - 1);
                        openTabsMap.remove(keys[i]);
                        tabCount--;
                    }
                }
                connectBtn.setIcon(new ImageIcon(connectImg));
                connectBtn.setToolTipText("Connect");
                setTitle("fmIRC+");
                tfPort.setEditable(true);
                tfIp.setEditable(true);
                nick.setEditable(true);
            });
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void connect() throws NumberFormatException {
        ConnectEntity connectEntity = new ConnectEntity();
        connectEntity.setAddress(new InetSocketAddress(tfIp.getText(), Integer.parseInt(tfPort.getText())));
        connectEntity.setNickname(nick.getText());
        this.nickname = nick.getText();
        chatClient.connect(connectEntity);
        connectBtn.setIcon(new ImageIcon(disconnectImg));
        connectBtn.setToolTipText("Disconnect");
        tfPort.setEditable(false);
        tfIp.setEditable(false);
        nick.setEditable(false);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == connectBtn) {
            if (connectBtn.getToolTipText().equals("Disconnect")) {
                disconnect();
            }
            else {
                connect();
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
        //If enter is pressed when filling the port field it will connect
        if (e.getSource() == tfPort && e.getKeyCode() == KeyEvent.VK_ENTER) {
            connect();
        }
    }
}
