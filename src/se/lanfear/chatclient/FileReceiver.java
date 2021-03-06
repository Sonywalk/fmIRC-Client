package se.lanfear.chatclient;

import se.lanfear.chatclient.util.ChatUtils;
import se.lanfear.chatclient.util.Constants;

import javax.swing.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.List;

public class FileReceiver extends SwingWorker<Void, String> {
    private int port;
    private String filename;
    private ChatClient client;
    private long size;
    private final static int BUFF_SIZE = 8*1024;

    public FileReceiver(String filename, String size, ChatClient client, int port) {
        this.port = port;
        this.filename = filename;
        this.client = client;
        this.size = Long.parseLong(size);
    }

    @Override
    protected Void doInBackground() throws IOException {
        Socket socket = new Socket(client.getAddress(), port);
        socket.setSoTimeout(5000);
        InputStream in = socket.getInputStream();
        File downloadDir = new File(Constants.DOWNLOAD_PATH);
        if (!downloadDir.exists()) {
            downloadDir.mkdir();
        }
        FileOutputStream fout = new FileOutputStream(Constants.DOWNLOAD_PATH + "/" + filename);
        try {
            byte[] buff = new byte[BUFF_SIZE];
            int len;
            long startTime = System.nanoTime();
            long count = 0;

            while ((len = in.read(buff)) != -1) {
                count += len;
                if (count % (1024*2) == 0 || count == size) {
                    //Using publish to make a gui update
                    publish("Downloading: " + ChatUtils.getPercent(count, size) + " %" +
                            ChatUtils.getMegabyteDifference(count, size) + ChatUtils.getDownloadRate(startTime, count) + " - [" + filename + "]");
                }
                fout.write(buff, 0, len);
            }
        }
        catch (SocketTimeoutException e) {
            client.write("ERROR " + client.getNickname() + " :Connection timed out");
            e.printStackTrace();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            fout.flush();
            fout.close();
            if (in != null) {
                in.close();
            }
            socket.close();
            publish("fmIRC+ - " + client.getNickname());
        }
        return null;
    }
    @Override
    protected void process(final List<String> chunks) {
        for (final String item : chunks) {
            client.setWindowTitle(item);
        }
    }
}
