package com.company;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

/**
 * Created by LanfeaR on 2016-02-08.
 */
public class ChatHelper {


    public static String getTime() {
        Date date = Calendar.getInstance().getTime();
        SimpleDateFormat sdt = new SimpleDateFormat("HH:mm:ss");
        return sdt.format(date);
    }

    public static Color getRandomColor() {
        Random random = new Random();
        final float hue = random.nextFloat();
        final float saturation = 0.9f; //1.0 for brilliant, 0.0 for dull
        final float luminance = 1.0f; //1.0 for brighter, 0.0 for black
        return Color.getHSBColor(hue, saturation, luminance);
    }

    //Pass null as backgroundColor or foregroundColor and it will return a default value
    public static void appendToPane(JTextPane tp, String msg, Color foregroundColor, Color backgroundColor) throws BadLocationException {
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

}
