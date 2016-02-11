package com.company;

import com.company.Util.ChatUtils;
import com.google.common.io.CountingInputStream;

import javax.swing.*;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.List;

/**
 * Created by LanfeaR on 2016-02-11.
 */
public class FileReceiver extends SwingWorker<Void, String> {
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
        Socket socket = new Socket(client.getAddress(), PORT);
        CountingInputStream in = new CountingInputStream(socket.getInputStream());
        FileOutputStream fout = new FileOutputStream(System.getProperty("user.home") + "/Desktop/" + filename); //TODO change this to something else?
        try {
            byte[] buff = new byte[8 * 1024];
            int len;
            long startTime = System.nanoTime();
            while ((len = in.read(buff)) != -1) {
                if (in.getCount() % 1000 == 0 || in.getCount() == size) {
                    //Using publish to make a gui update
                    publish("Downloading: " + ChatUtils.getPercent(in.getCount(), size) + " %" +
                            ChatUtils.getMegabyteDifference(in.getCount(), size) + ChatUtils.getDownloadRate(startTime, in.getCount()) + " - [" + filename + "]");
                }
                fout.write(buff, 0, len);
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
    protected void process(final List<String> chunks) {
        for (final String item : chunks) {
            client.setTitle(item);
        }
    }

    @Override
    protected void done() {
        client.setTitle("fmIRC+ - " + client.getNickname());
    }
}
