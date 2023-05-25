package ro.pub.cs.systems.eim.practicaltest02.network;

import android.util.Log;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

import cz.msebera.android.httpclient.client.ClientProtocolException;
import ro.pub.cs.systems.eim.practicaltest02.general.Constants;
import ro.pub.cs.systems.eim.practicaltest02.model.WeatherForecastInformation;

public class ServerThread extends Thread {

    private ServerSocket serverSocket;
    private HashMap<String, WeatherForecastInformation> data = null;

    public ServerThread(int port) {
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            Log.e(Constants.SERVER_THREAD_TAG, "An exception occured: " + e.getMessage());
            e.printStackTrace();
        }
        this.data = new HashMap<>();
    }

    @Override
    public void run() {
        try{
            while(!Thread.currentThread().isInterrupted()) {
                Log.i(Constants.SERVER_THREAD_TAG, "[SERVER THREAD] Waiting for a client invocation...");
                Socket socket = serverSocket.accept();
                Log.i(Constants.SERVER_THREAD_TAG, "[SERVER THREAD] A connection request was received from " + socket.getInetAddress() + ":" + socket.getLocalPort());
                CommunicationThread communicationThread = new CommunicationThread(this, socket);
                communicationThread.start();
            }
        } catch(ClientProtocolException clientProtocolException) {
            Log.e(Constants.SERVER_THREAD_TAG, "[SERVER THREAD] An exception has occurred: " + clientProtocolException.getMessage());
            clientProtocolException.printStackTrace();
        } catch (IOException e) {
            Log.e(Constants.SERVER_THREAD_TAG, "[SERVER THREAD] An exception has occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public ServerSocket getServerSocket() {
        return serverSocket;
    }

    public synchronized void setData(String city, WeatherForecastInformation weatherForecastInformation) {
        this.data.put(city, weatherForecastInformation);
    }
    public synchronized HashMap<String, WeatherForecastInformation> getData() {
        return data;
    }

    public void stopThread() {
        interrupt();
        if(serverSocket != null) {
            try {
                serverSocket.close();
            } catch(IOException e) {
                Log.e(Constants.SERVER_THREAD_TAG, "[SERVER THREAD] An exception has occurred: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
