package ro.pub.cs.systems.eim.practicaltest02;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.methods.HttpGet;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;
import cz.msebera.android.httpclient.util.EntityUtils;



public class CommunicationThread extends Thread {

    private final ServerThread serverThread;
    private final Socket socket;

    // Constructor of the thread, which takes a ServerThread and a Socket as parameters
    public CommunicationThread(ServerThread serverThread, Socket socket) {
        this.serverThread = serverThread;
        this.socket = socket;
    }

    // run() method: The run method is the entry point for the thread when it starts executing.
    // It's responsible for reading data from the client, interacting with the server,
    // and sending a response back to the client.
    @Override
    public void run() {
        // It first checks whether the socket is null, and if so, it logs an error and returns.
        if (socket == null) {
            Log.e(Constants.TAG, "[COMMUNICATION THREAD] Socket is null!");
            return;
        }
        try {
            // Create BufferedReader and PrintWriter instances for reading from and writing to the socket
            BufferedReader bufferedReader = Utilities.getReader(socket);
            PrintWriter printWriter = Utilities.getWriter(socket);
            Log.i(Constants.TAG, "[COMMUNICATION THREAD] Waiting for parameters from client (city / information type!");

            // Read the city and informationType values sent by the client
            String pokemonName = bufferedReader.readLine();
            if (pokemonName == null || pokemonName.isEmpty()) {
                Log.e(Constants.TAG, "[COMMUNICATION THREAD] Error receiving parameters from client (city / information type!");
                return;
            }

            // It checks whether the serverThread has already received the weather forecast information for the given city.
            HashMap<String, PokemonDetails> data = serverThread.getData();
            PokemonDetails pokemonData;
            if (data.containsKey(pokemonName)) {
                Log.i(Constants.TAG, "[COMMUNICATION THREAD] Getting the information from the cache...");
                pokemonData = data.get(pokemonName);
            } else {
                Log.i(Constants.TAG, "[COMMUNICATION THREAD] Getting the information from the webservice...");
                HttpClient httpClient = new DefaultHttpClient();
                String pokemonDetailsJSON = "";

                // make the HTTP request to the web service
                HttpGet httpGet = new HttpGet(Constants.WEB_SERVICE_ADDRESS + "/" + pokemonName);
                HttpResponse httpGetResponse = httpClient.execute(httpGet);
                HttpEntity httpGetEntity = httpGetResponse.getEntity();
                if (httpGetEntity != null) {
                    pokemonDetailsJSON = EntityUtils.toString(httpGetEntity);
                }
                if (pokemonDetailsJSON == null) {
                    Log.e(Constants.TAG, "[COMMUNICATION THREAD] Error getting the information from the webservice!");
                    return;
                } else Log.i(Constants.TAG, pokemonDetailsJSON);

                // Parse the page source code into a JSONObject and extract the needed information
                JSONObject content = new JSONObject(pokemonDetailsJSON);

                String fetchedPokemonName = content.getString("name");
                Log.i(Constants.TAG, fetchedPokemonName);

                JSONObject sprites = content.getJSONObject("sprites");
                String pokemonImage = sprites.getString("front_default");
                HttpGet httpGetImage = new HttpGet(pokemonImage);
                HttpResponse httpGetImageResponse = httpClient.execute(httpGetImage);
                HttpEntity httpImageEntity = httpGetImageResponse.getEntity();
                InputStream imageStream = httpImageEntity.getContent();
                Bitmap bmp = BitmapFactory.decodeStream(imageStream);
                Log.i(Constants.TAG, pokemonImage);


                // Create a pokemonData object with the information extracted from the JSONObject
                ArrayList<String> pokemonTypes = new ArrayList<String>();
                ArrayList<String> pokemonAbilities = new ArrayList<String>();

                JSONArray types = content.getJSONArray("types");
                JSONArray abilities = content.getJSONArray("abilities");
                Log.i(Constants.TAG, String.valueOf(types));
                Log.i(Constants.TAG, String.valueOf(abilities));

                for (int i = 0; i < types.length(); i++) {
                    JSONObject object = types.getJSONObject(i);
                    JSONObject typeObject = object.getJSONObject("type");
                    pokemonTypes.add(typeObject.getString("name"));

                }

                for (int i = 0; i < abilities.length(); i++) {
                    JSONObject object = abilities.getJSONObject(i);
                    Log.i(Constants.TAG, String.valueOf(object));
                    JSONObject abilityObject = object.getJSONObject("ability");
                    pokemonAbilities.add(abilityObject.getString("name"));

                }

                pokemonData = new PokemonDetails(fetchedPokemonName, pokemonImage, pokemonTypes , pokemonAbilities, bmp);

                // Cache the information for the given city
                serverThread.setData(pokemonName, pokemonData);
            }

            if (pokemonData == null) {
                Log.e(Constants.TAG, "[COMMUNICATION THREAD] Pokemon data is null!");
                return;
            }

            // Send the information back to the client
            String result = pokemonData.toString();

            // Send the result back to the client
            printWriter.println(result);
            printWriter.flush();
            printWriter.println(pokemonData.image_url);
        } catch (IOException | JSONException ioException) {
            Log.e(Constants.TAG, "[COMMUNICATION THREAD] An exception has occurred: " + ioException.getMessage());
            ioException.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException ioException) {
                Log.e(Constants.TAG, "[COMMUNICATION THREAD] An exception has occurred: " + ioException.getMessage());
                ioException.printStackTrace();
            }
        }
    }

    public String BitMapToString(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] b = baos.toByteArray();
        String temp = Base64.encodeToString(b, Base64.DEFAULT);
        return temp;
    }
}
