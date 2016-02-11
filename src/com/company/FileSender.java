package com.company;

import com.company.Util.ChatUtils;
import com.google.common.io.CountingOutputStream;

import javax.swing.*;
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
        CountingOutputStream out = new CountingOutputStream(socket.getOutputStream());
        try {
            long startTime = System.nanoTime();
            byte[] buff = new byte[8 * 1024];
            int len;
            while ((len = fin.read(buff)) != -1) {
                //TODO very warning this is not how it should be done, use progress update, never update Swing from background worker...
                if (out.getCount() % 1000 == 0 || out.getCount() == size) {
                    client.setTitle("Uploading: " + ChatUtils.getPercent(out.getCount(), size) + " %" +
                            ChatUtils.getMegabyteDifference(out.getCount(), size) + ChatUtils.getDownloadRate(startTime, out.getCount()));
                }
                out.write(buff, 0, len);
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
