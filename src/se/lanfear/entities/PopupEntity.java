package se.lanfear.entities;

/**
 * Created by LanfeaR on 2016-02-23.
 */
public class PopupEntity {
    private String header;
    private String paragraph;

    public PopupEntity(String header, String paragraph) {
        this.header = header;
        this.paragraph = paragraph;
    }

    public String getHeader() {
        return header;
    }

    public String getParagraph() {
        return paragraph;
    }
}
