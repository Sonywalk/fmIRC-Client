package se.lanfear.chatclient;

import se.lanfear.chatclient.util.ChatUtils;
import se.lanfear.chatclient.util.Constants;

import javax.swing.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.List;

/**
 * Created by LanfeaR on 2016-02-11.
 */
//TODO make this class an observer
public class FileReceiver extends SwingWorker<Void, String> {
    private final static int PORT = 1338;
    private String filename;
    private ChatClient client;
    private long size;
    private final static int BUFF_SIZE = 8*1024;

    public FileReceiver(String filename, String size, ChatClient client) {
        this.filename = filename;
        this.client = client;
        this.size = Long.parseLong(size);
    }

    @Override
    protected Void doInBackground() throws IOException {
        Socket socket = null;
        InputStream in = null;
        try {
            System.out.println("Before connect");
            socket = new Socket(client.getAddress(), PORT);
            System.out.println("Have socket");
            in = socket.getInputStream();
            System.out.println("Have stream");
        }
        catch (Exception e) {
            e.printStackTrace();
        }


        File downloadDir = new File(Constants.DOWNLOAD_PATH);
        if (!downloadDir.exists()) {
            downloadDir.mkdir();
        }
        System.out.println("Before file out");
        FileOutputStream fout = new FileOutputStream(Constants.DOWNLOAD_PATH + "/" + filename);
        System.out.println("File output open");

        try {
            byte[] buff = new byte[BUFF_SIZE];
            int len;
            long startTime = System.nanoTime();
            long count = 0;
            while ((len = in.read(buff)) != -1) {
                count += len;
                if (count % (1024*2) == 0 || count == size) {
                    //Using publish to make a gui update
                    System.out.println("Receiving byte: " + len);
                    publish("Downloading: " + ChatUtils.getPercent(count, size) + " %" +
                            ChatUtils.getMegabyteDifference(count, size) + ChatUtils.getDownloadRate(startTime, count) + " - [" + filename + "]");
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
            client.setWindowTitle(item);
        }
    }
    @Override
    protected void done() {
        client.setWindowTitle("fmIRC+ - " + client.getNickname());
    }
}
