package ro.pub.cs.systems.eim.practicaltest02.network;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.NameValuePair;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.ResponseHandler;
import cz.msebera.android.httpclient.client.entity.UrlEncodedFormEntity;
import cz.msebera.android.httpclient.client.methods.HttpGet;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.impl.client.BasicResponseHandler;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;
import cz.msebera.android.httpclient.message.BasicNameValuePair;
import cz.msebera.android.httpclient.protocol.HTTP;
import cz.msebera.android.httpclient.util.EntityUtils;
import ro.pub.cs.systems.eim.practicaltest02.general.Constants;
import ro.pub.cs.systems.eim.practicaltest02.general.Utils;
import ro.pub.cs.systems.eim.practicaltest02.model.WeatherForecastInformation;

public class CommunicationThread extends Thread {

    private ServerThread serverThread;
    private Socket socket;

    public CommunicationThread(ServerThread serverThread, Socket socket) {
        this.serverThread = serverThread;
        this.socket = socket;
    }

    @Override
    public void run() {
        if (socket == null) {
            Log.e(Constants.COMMUNICATION_TAG, "[COMMUNICATION THREAD] Socket is null!");
            return;
        }
        try {
            BufferedReader bufferedReader = Utils.getReader(socket);
            PrintWriter printWriter = Utils.getWriter(socket);
            if (bufferedReader == null || printWriter == null) {
                Log.e(Constants.COMMUNICATION_TAG, "[COMMUNICATION THREAD] Buffered Reader / Print Writer are null!");
                return;
            }
            Log.i(Constants.COMMUNICATION_TAG, "[COMMUNICATION THREAD] Waiting for parameters from client (city / information type!");
            String city = bufferedReader.readLine();
            String informationType = bufferedReader.readLine();
            if (city == null || city.isEmpty() || informationType == null || informationType.isEmpty()) {
                Log.e(Constants.COMMUNICATION_TAG, "[COMMUNICATION THREAD] Error receiving parameters from client (city / information type!");
                return;
            }
            HashMap<String, WeatherForecastInformation> data = serverThread.getData();
            WeatherForecastInformation weatherForecastInformation = null;
            if (data.containsKey(city)) {
                Log.i(Constants.COMMUNICATION_TAG, "[COMMUNICATION THREAD] Getting the information from the cache...");
                weatherForecastInformation = data.get(city);
            } else {
                Log.i(Constants.COMMUNICATION_TAG, "[COMMUNICATION THREAD] Getting the information from the webservice...");
                HttpClient httpClient = new DefaultHttpClient();
                String pageSourceCode = "";
                HttpGet httpGet = new HttpGet(Constants.WEB_SERVICE_ADDRESS + "?q=" + city + "&APPID=" + Constants.WEB_SERVICE_API_KEY + "&units=" + Constants.UNITS);
                HttpResponse httpGetResponse = httpClient.execute(httpGet);
                HttpEntity httpGetEntity = httpGetResponse.getEntity();
                if (httpGetEntity != null) {
                    pageSourceCode = EntityUtils.toString(httpGetEntity);
                }

                if (pageSourceCode == null) {
                    Log.e(Constants.COMMUNICATION_TAG, "[COMMUNICATION THREAD] Error getting the information from the webservice!");
                    return;
                } else
                    Log.i(Constants.COMMUNICATION_TAG, pageSourceCode);

                JSONObject content = new JSONObject(pageSourceCode);

                JSONArray weatherArray = content.getJSONArray("weather");
                JSONObject weather;
                String condition = "";
                for (int i = 0; i < weatherArray.length(); i++) {
                    weather = weatherArray.getJSONObject(i);
                    condition += weather.getString("main") + " : " + weather.getString("description");

                    if (i < weatherArray.length() - 1) {
                        condition += ";";
                    }
                }

                JSONObject main = content.getJSONObject("main");
                String temperature = main.getString("temp");
                String pressure = main.getString(Constants.PRESSURE);
                String humidity = main.getString(Constants.HUMIDITY);
                JSONObject wind = content.getJSONObject("wind");
                String windSpeed = wind.getString("speed");

                weatherForecastInformation = new WeatherForecastInformation(
                        temperature, windSpeed, condition, pressure, humidity
                );
                serverThread.setData(city, weatherForecastInformation);
            }
            if (weatherForecastInformation == null) {
                Log.e(Constants.COMMUNICATION_TAG, "[COMMUNICATION THREAD] Weather Forecast Information is null!");
                return;
            }
            String result = null;
            switch (informationType) {
                case Constants.ALL:
                    result = weatherForecastInformation.toString();
                    break;
                case Constants.TEMPERATURE:
                    result = weatherForecastInformation.getTemperature();
                    break;
                case Constants.WIND_SPEED:
                    result = weatherForecastInformation.getWindSpeed();
                    break;
                case Constants.CONDITION:
                    result = weatherForecastInformation.getCondition();
                    break;
                case Constants.HUMIDITY:
                    result = weatherForecastInformation.getHumidity();
                    break;
                case Constants.PRESSURE:
                    result = weatherForecastInformation.getPressure();
                    break;
                default:
                    result = "[COMMUNICATION THREAD] Wrong information type (all / temperature / wind_speed / condition / humidity / pressure)!";
            }
            printWriter.println(result);
            printWriter.flush();
        } catch (IOException ioException) {
            Log.e(Constants.COMMUNICATION_TAG, "[COMMUNICATION THREAD] An exception has occurred: " + ioException.getMessage());
            ioException.printStackTrace();
        } catch (JSONException jsonException) {
            Log.e(Constants.COMMUNICATION_TAG, "[COMMUNICATION THREAD] An exception has occurred: " + jsonException.getMessage());
            jsonException.printStackTrace();
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException ioException) {
                    Log.e(Constants.COMMUNICATION_TAG, "[COMMUNICATION THREAD] An exception has occurred: " + ioException.getMessage());
                    ioException.printStackTrace();
                }
            }
        }
    }

}
