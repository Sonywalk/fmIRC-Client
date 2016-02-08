package com.company;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by LanfeaR on 2016-02-01.
 */
public class Notification extends SwingWorker<Void, Void> implements ActionListener {

    JFrame frame;
    private static Notification instance;

    public static Notification getInstance(Image headingIcon, String header, String paragraph) {
        if (instance == null) {
            instance = new Notification(headingIcon, header, paragraph);
        }
        return instance;
    }

    private Notification(Image headingIcon, String header, String paragraph) {
        frame = new JFrame();
        frame.setIconImage(headingIcon);
        frame.setSize(300, 125);
        frame.setUndecorated(true);
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.setVisible(true);
        frame.setAlwaysOnTop(true);

        Dimension scrSize = Toolkit.getDefaultToolkit().getScreenSize(); //size of the screen
        Insets toolHeight = Toolkit.getDefaultToolkit().getScreenInsets(frame.getGraphicsConfiguration()); //height of the task bar
        frame.setLocation(scrSize.width - frame.getWidth(), scrSize.height - toolHeight.bottom - frame.getHeight());

        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(Color.BLACK);

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.weightx = 1.0f;
        constraints.weighty = 1.0f;
        constraints.insets = new Insets(5, 5, 5, 5);
        constraints.fill = GridBagConstraints.BOTH;

        JLabel headingLabel = new JLabel(header);
        headingLabel.setIcon(new ImageIcon(headingIcon));
        headingLabel.setOpaque(false);
        headingLabel.setForeground(Color.WHITE);
        p.add(headingLabel, constraints);

        constraints.gridx++;
        constraints.weightx = 0f;
        constraints.weighty = 0f;
        constraints.fill = GridBagConstraints.NONE;
        constraints.anchor = GridBagConstraints.NORTH;

        JButton closeButton = new JButton("X");
        closeButton.setMargin(new Insets(1, 4, 1, 4));
        closeButton.setFocusable(false);
        closeButton.addActionListener(this);
        closeButton.setBackground(Color.BLACK);
        closeButton.setForeground(Color.WHITE);
        p.add(closeButton, constraints);

        constraints.gridx = 0;
        constraints.gridy++;
        constraints.weightx = 1.0f;
        constraints.weighty = 1.0f;
        constraints.insets = new Insets(5, 5, 5, 5);
        constraints.fill = GridBagConstraints.BOTH;

        JLabel messageLabel = new JLabel(paragraph);
        messageLabel.setForeground(Color.WHITE);
        p.add(messageLabel, constraints);

        frame.add(p);
    }

    private void dispose() {
        this.frame.dispose();
        instance = null;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        dispose();
    }

    @Override
    protected Void doInBackground() throws Exception {
        Thread.sleep(5000);
        dispose();
        return null;
    }
}
