package com.company;

import com.company.Util.ChatUtils;
import com.company.Util.Constants;

import javax.swing.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.List;

/**
 * Created by LanfeaR on 2016-02-11.
 */
public class FileSender extends SwingWorker<Void, String> {

    private final static int PORT = 1338;
    private String filename;
    private ChatClient client;
    private long size;
    private final static int BUFF_SIZE = 8*1024;

    public FileSender(String filename, String size, ChatClient client) {
        this.filename = filename;
        this.client = client;
        this.size = Long.parseLong(size);
    }

    @Override
    protected Void doInBackground() throws IOException {
        Socket socket = new Socket(client.getAddress(), PORT);
        FileInputStream fin = new FileInputStream(Constants.SHARED_PATH + "/" + filename);
        OutputStream out = socket.getOutputStream();
        try {
            long startTime = System.nanoTime();
            byte[] buff = new byte[BUFF_SIZE];
            int len;
            long count = 0;
            while ((len = fin.read(buff)) != -1) {
                count += len;
                if (count % (1024*2) == 0 || count == size) {
                    //Using publish to make a gui update
                    publish("Uploading: " + ChatUtils.getPercent(count, size) + " %" +
                            ChatUtils.getMegabyteDifference(count, size) + ChatUtils.getDownloadRate(startTime, count) + " - [" + filename + "]");
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
