package se.lanfear.chatclient.util;

import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

public class ChatUtils {

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

    public static int getPercent(long a, long b) {
        return (int)((float)a / (float)b * 100);
    }
    public static String getMegabyteDifference(long count, long size) {
        return " (" + Math.round(((double)count / (1024 * 1024)) * 10d) / 10d + "MB / " + Math.round(((double)size / (1024 * 1024)) * 10d) / 10d + "MB) ";
    }
    public static String getDownloadRate(long startTime, long bytesRead) {
        double elapsedTime = ((double)System.nanoTime() - (double)startTime)/1000000000;
        double result = ((double)bytesRead/1024)/elapsedTime;
        if (result > 1024) {
            result = result/1024;
            return Double.toString(Math.round(result * 10d) / 10d) + " MB/s";
        }
        else {
            return Double.toString(Math.round(result * 10d) / 10d) + " KB/s";
        }
    }

}
