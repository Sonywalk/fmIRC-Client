package se.lanfear.observers.view;

import javax.swing.*;
import javax.swing.plaf.ScrollBarUI;
import javax.swing.plaf.TabbedPaneUI;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import java.awt.*;

public class CustomUI {

    public static ScrollBarUI getCustomScrollUI() {
        return new BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                super.configureScrollBarColors();

                LookAndFeel.installColors(scrollbar, "ScrollBar.background",
                        "ScrollBar.foreground");
                thumbHighlightColor = Color.DARK_GRAY;
                thumbLightShadowColor = Color.DARK_GRAY;
                thumbDarkShadowColor = Color.DARK_GRAY;
                thumbColor = Color.BLACK;
                trackColor = Color.BLACK;
                trackHighlightColor = Color.BLACK;
            }

            @Override
            protected JButton createDecreaseButton(int orientation) {
                return createZeroButton();
            }

            @Override
            protected JButton createIncreaseButton(int orientation) {
                return createZeroButton();
            }

            private JButton createZeroButton() {
                JButton jbutton = new JButton();
                jbutton.setPreferredSize(new Dimension(0, 0));
                jbutton.setMinimumSize(new Dimension(0, 0));
                jbutton.setMaximumSize(new Dimension(0, 0));
                return jbutton;
            }
        };
    }

    public static TabbedPaneUI getCustomTabbedUI() {
        return new BasicTabbedPaneUI() {
            @Override
            protected void installDefaults() {
                super.installDefaults();
                highlight = Color.GRAY;
                lightHighlight = Color.WHITE;
                shadow = Color.DARK_GRAY;
                darkShadow = Color.BLACK;
                focus = Color.DARK_GRAY;
            }
        };
    }
}
