package ro.pub.cs.systems.eim.practicaltest02;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    EditText portEditText = null;
    Button startServerButton = null;
    EditText pokemonNameEditText = null;
    Button fetchDataButton = null;
    ImageView pokemonImageView = null;
    TextView pokemonDetailsTextView = null;

    ServerThread serverThread = null;

    private final StartServerButtonClickListener startServerButtonClickListener = new StartServerButtonClickListener();

    private class StartServerButtonClickListener implements Button.OnClickListener {

        @Override
        public void onClick(View view) {
            String serverPort = portEditText.getText().toString();
            if (serverPort.isEmpty()) {
                Toast.makeText(getApplicationContext(), "[MAIN ACTIVITY] Server port should be filled!", Toast.LENGTH_SHORT).show();
                return;
            }
            serverThread = new ServerThread(Integer.parseInt(serverPort));
            if (serverThread.getServerSocket() == null) {
                Log.e(Constants.TAG, "[MAIN ACTIVITY] Could not create server thread!");
                return;
            }
            serverThread.start();
        }
    }

    private final GetPokemonDetailsButtonClickListener getPokemonDetailsButtonClickListener = new GetPokemonDetailsButtonClickListener();

    private class GetPokemonDetailsButtonClickListener implements Button.OnClickListener {

        @Override
        public void onClick(View view) {

            // Retrieves the client address and port. Checks if they are empty or not
            //  Checks if the server thread is alive. Then creates a new client thread with the address, port, city and information type
            //  and starts it
            String clientAddress = "127.0.0.1";
            String clientPort = portEditText.getText().toString();
            if (clientPort.isEmpty()) {
                Toast.makeText(getApplicationContext(), "[MAIN ACTIVITY] Client connection parameters should be filled!", Toast.LENGTH_SHORT).show();
                return;
            }
            if (serverThread == null || !serverThread.isAlive()) {
                Toast.makeText(getApplicationContext(), "[MAIN ACTIVITY] There is no server to connect to!", Toast.LENGTH_SHORT).show();
                return;
            }
            String pokemonName = pokemonNameEditText.getText().toString();
            if (pokemonName.isEmpty()) {
                Toast.makeText(getApplicationContext(), "[MAIN ACTIVITY] Parameters from client (city / information type) should be filled", Toast.LENGTH_SHORT).show();
                return;
            }

            pokemonDetailsTextView.setText("");

            ClientThread clientThread = new ClientThread(clientAddress, Integer.parseInt(clientPort), pokemonName, pokemonImageView, pokemonDetailsTextView);
            clientThread.start();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        portEditText = findViewById(R.id.server_port_edit_text);
        startServerButton = findViewById(R.id.connect_button);
        pokemonNameEditText = findViewById(R.id.pokemon_name);
        fetchDataButton = findViewById(R.id.get_pokemon_button);
        pokemonImageView = findViewById(R.id.pokemon_image);
        pokemonDetailsTextView = findViewById(R.id.pokemon_details);

        startServerButton.setOnClickListener(startServerButtonClickListener);
        fetchDataButton.setOnClickListener(getPokemonDetailsButtonClickListener);
    }

    @Override
    protected void onDestroy() {
        Log.i(Constants.TAG, "[MAIN ACTIVITY] onDestroy() callback method has been invoked");
        if (serverThread != null) {
            serverThread.stopThread();
        }
        super.onDestroy();
    }
}