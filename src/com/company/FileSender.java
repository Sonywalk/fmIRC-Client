package com.company;

import com.company.Util.ChatUtils;

import javax.swing.*;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Created by LanfeaR on 2016-02-11.
 */
public class FileSender extends SwingWorker<Void, Void> {

    private final static String ADDR = "127.0.0.1";
    private final static int PORT = 1338;
    private String filename;
    private ChatClient client;
    private long size;

    public FileSender(String filename, String size, ChatClient client) {
        this.filename = filename;
        this.client = client;
        this.size = Long.parseLong(size);
    }

    @Override
    protected Void doInBackground() throws IOException {
        Socket socket = new Socket(ADDR, PORT);
        FileInputStream fin = new FileInputStream(filename);
        BufferedOutputStream out = new BufferedOutputStream(socket.getOutputStream());
        try {
            int b;
            int counter = 0;
            long startTime = System.nanoTime();
            while ((b = fin.read()) != -1) {
                //TODO very warning this is not how it should be done, use progress update, never update Swing from background worker...
                if (counter % 100 == 0 || counter == size) {
                    client.setTitle("Uploading: " + ChatUtils.getPercent(counter, size) + " %" +
                            "  (" + ChatUtils.byteToMB((double) counter) + "mb / " + ChatUtils.byteToMB((double) size) + "mb) "
                            + ChatUtils.getDownloadRate(startTime, counter) + " kb/s");
                }
                counter++;
                out.write(b);
            }
            out.flush();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            fin.close();
            out.close();
            socket.close();
        }
        return null;
    }

    @Override
    protected void done() {
        client.setTitle("fmIRC+ - " + client.getNickname());
    }
}
