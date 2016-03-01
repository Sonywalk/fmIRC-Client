package se.lanfear.entities;

import java.awt.*;

/**
 * Created by LogiX on 2016-02-23.
 */
public class AppendEntity {

    private Color foregroundColor;
    private Color backgroundColor;
    private String text;
    private String writer;

    public AppendEntity(Color foregroundColor, Color backgroundColor, String text) {
        this.foregroundColor = foregroundColor;
        this.backgroundColor = backgroundColor;
        this.text = text;
    }

    public Color getForegroundColor() {
        return foregroundColor;
    }

    public void setForegroundColor(Color foregroundColor) {
        this.foregroundColor = foregroundColor;
    }

    public Color getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
