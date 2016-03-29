package se.lanfear.observers.view.tab;

import se.lanfear.chatclient.ChatClient;
import se.lanfear.entities.AppendEntity;
import se.lanfear.entities.MessageEntity;
import se.lanfear.observers.view.GUI;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.io.IOException;

public class ConsoleTab extends Tab {

    public ConsoleTab(String id, ChatClient chatClient, GUI gui) throws IOException {
        super(id, chatClient, gui);
    }

    @Override
    public void message(MessageEntity entity) {
        String msg = "[" + entity.getTime() + "] " + entity.getMessage();
        appendToPane(new AppendEntity(null, null, msg));
    }

    @Override
    public void appendToPane(AppendEntity entity) {
        Color backgroundColor = entity.getBackgroundColor();
        Color foregroundColor = entity.getForegroundColor();

        if (backgroundColor == null) {
            backgroundColor = new Color(0, 0, 0, 1);
        }
        if (foregroundColor == null) {
            foregroundColor = Color.WHITE;
        }

        try {
            //Some calls to this function may be on EDT already, if not run it on EDT
            if (SwingUtilities.isEventDispatchThread()) {
                StyledDocument doc = textPane.getStyledDocument();
                SimpleAttributeSet sas = new SimpleAttributeSet();
                StyleConstants.setForeground(sas, foregroundColor);
                StyleConstants.setBackground(sas, backgroundColor);
                doc.insertString(doc.getLength(), entity.getText() + "\n", sas);
            }
            else {
                final Color finalForegroundColor = foregroundColor;
                final Color finalBackgroundColor = backgroundColor;
                SwingUtilities.invokeLater(() -> {
                    try {
                        StyledDocument doc = textPane.getStyledDocument();
                        SimpleAttributeSet sas = new SimpleAttributeSet();
                        StyleConstants.setForeground(sas, finalForegroundColor);
                        StyleConstants.setBackground(sas, finalBackgroundColor);
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
}
