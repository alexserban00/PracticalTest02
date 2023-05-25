package ro.pub.cs.systems.eim.practicaltest02.network;

import android.util.Log;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

import ro.pub.cs.systems.eim.practicaltest02.general.Constants;
import ro.pub.cs.systems.eim.practicaltest02.general.Utils;

public class ClientThread extends Thread {

    private String address;
    private int port;
    private String word;
    private TextView tvDefinition;

    private Socket socket;

    public ClientThread(String address, int port, String word, TextView tvDefinition) {
        this.address = address;
        this.port = port;
        this.word = word;
        this.tvDefinition = tvDefinition;
    }

    @Override
    public void run() {
        try {
            socket = new Socket(address, port);
            if (socket == null) {
                Log.e(Constants.CLIENT_THREAD_TAG, "[CLIENT THREAD] Could not create socket!");
                return;
            }
            BufferedReader bufferedReader = Utils.getReader(socket);
            PrintWriter printWriter = Utils.getWriter(socket);
            if (bufferedReader == null || printWriter == null) {
                Log.e(Constants.CLIENT_THREAD_TAG, "[CLIENT THREAD] Buffered Reader / Print Writer are null!");
                return;
            }
            printWriter.println(word);
            printWriter.flush();
            String wordDefinition;
            while ((wordDefinition = bufferedReader.readLine()) != null) {
                final String finalizedWeateherInformation = wordDefinition;
                tvDefinition.post(new Runnable() {
                    @Override
                    public void run() {
                        tvDefinition.setText(finalizedWeateherInformation);
                    }
                });
            }
        } catch (IOException ioException) {
            Log.e(Constants.CLIENT_THREAD_TAG, "[CLIENT THREAD] An exception has occurred: " + ioException.getMessage());
            ioException.printStackTrace();
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException ioException) {
                    Log.e(Constants.CLIENT_THREAD_TAG, "[CLIENT THREAD] An exception has occurred: " + ioException.getMessage());
                    ioException.printStackTrace();
                }
            }
        }
    }

}
