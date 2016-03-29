package se.lanfear.chatclient;

import se.lanfear.chatclient.util.ChatUtils;
import se.lanfear.chatclient.util.Constants;

import javax.swing.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.List;

public class FileSender extends SwingWorker<Void, String> {

    private int port;
    private String filename;
    private ChatClient client;
    private long size;
    private final static int BUFF_SIZE = 8*1024;

    public FileSender(String filename, String size, ChatClient client, int port) {
        this.port = port;
        this.filename = filename;
        this.client = client;
        this.size = Long.parseLong(size);
    }

    @Override
    protected Void doInBackground() throws IOException {
        Socket socket = new Socket(client.getAddress(), port);
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
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            if (out != null) {
                out.flush();
                out.close();
            }
            fin.close();
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
