package ro.pub.cs.systems.eim.practicaltest02;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import ro.pub.cs.systems.eim.practicaltest02.general.Constants;
import ro.pub.cs.systems.eim.practicaltest02.network.ClientThread;
import ro.pub.cs.systems.eim.practicaltest02.network.ServerThread;

public class PracticalTest02MainActivity extends AppCompatActivity {

    private EditText etServerPort, etClientAddr, etClientPort, etWord;
    private Button butServerConnect, butGetWeatherForecast;
    private ServerThread serverThread;
    private TextView tvDefinition;
    private ClientThread clientThread = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_practical_test02_main);

        etServerPort = findViewById(R.id.server_port);
        etClientAddr = findViewById(R.id.client_addr);
        etClientPort = findViewById(R.id.client_port);
        etWord = findViewById(R.id.word);
        butServerConnect = findViewById(R.id.server_connect);
        butGetWeatherForecast = findViewById(R.id.get_word_definition);
        tvDefinition = findViewById(R.id.weather_forecast_text_view);

        butServerConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String serverPort = etServerPort.getText().toString();
                if(serverPort == null || serverPort.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "[MAIN ACTIVITY] Server port should be filled!", Toast.LENGTH_SHORT).show();
                    return;
                }
                serverThread = new ServerThread(Integer.parseInt(serverPort));
                if(serverThread.getServerSocket() == null) {
                    Log.e(Constants.MAIN_TAG, "[MAIN ACTIVITY] Could not create server thread!");
                    return;
                }
                serverThread.start();
            }
        });

        butGetWeatherForecast.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String clientAddress = etClientAddr.getText().toString();
                String clientPort = etClientPort.getText().toString();
                if (clientAddress == null || clientAddress.isEmpty()
                        || clientPort == null || clientPort.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "[MAIN ACTIVITY] Client connection parameters should be filled!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (serverThread == null || !serverThread.isAlive()) {
                    Toast.makeText(getApplicationContext(), "[MAIN ACTIVITY] There is no server to connect to!", Toast.LENGTH_SHORT).show();
                    return;
                }
                String word = etWord.getText().toString();
                if (word == null || word.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "[MAIN ACTIVITY] Parameters from client (word / information type) should be filled", Toast.LENGTH_SHORT).show();
                    return;
                }

                tvDefinition.setText("");

                clientThread = new ClientThread(
                        clientAddress, Integer.parseInt(clientPort), word, tvDefinition
                );
                clientThread.start();
            }
        });
    }

    @Override
    protected void onDestroy() {
        Log.i(Constants.MAIN_TAG, "[MAIN ACTIVITY] onDestroy() callback method has been invoked");
        if (serverThread != null) {
           serverThread.stopThread();
        }
        super.onDestroy();
    }
}