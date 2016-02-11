package com.company;

import com.company.Util.ChatUtils;

import javax.swing.*;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Created by LanfeaR on 2016-02-11.
 */
public class FileReceiver extends SwingWorker<Void, Void> {
    private final static String ADDR = "127.0.0.1";
    private final static int PORT = 1338;
    private String filename;
    private ChatClient client;
    private long size;

    public FileReceiver(String filename, String size, ChatClient client) {
        this.filename = filename;
        this.client = client;
        this.size = Long.parseLong(size);
    }

    @Override
    protected Void doInBackground() throws IOException {
        Socket socket = new Socket(ADDR, PORT);
        BufferedInputStream in = new BufferedInputStream(socket.getInputStream());
        FileOutputStream fout = new FileOutputStream(System.getProperty("user.home") + "/Desktop/" + filename); //TODO change this to downloads?
        try {
            int b;
            int counter = 0;
            long startTime = System.nanoTime();
            while ((b = in.read()) != -1) {
                //TODO very warning this is not how it should be done, use progress update, never update Swing from background worker...
                if (counter % 100 == 0 || counter == size) {
                    client.setTitle("Downloading: " + ChatUtils.getPercent(counter, size) + " %" +
                            "  (" + ChatUtils.byteToMB((double) counter) + "mb / " + ChatUtils.byteToMB((double) size) + "mb) "
                            + ChatUtils.getDownloadRate(startTime, counter) + " kb/s");
                }
                counter++;
                fout.write(b);
            }
            fout.flush();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            fout.close();
            socket.close();
        }
        return null;
    }

    @Override
    protected void done() {
        client.setTitle("fmIRC+ - " + client.getNickname());
    }
}
