package ro.pub.cs.systems.eim.practicaltest02;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientThread extends Thread {

    private final String address;
    private final int port;
    private final String pokemonName;
    private final ImageView pokemonImageView;
    private final TextView pokemonDetailsTextView;

    private Socket socket;

    public ClientThread(String address, int port, String pokemonName, ImageView pokemonImageView, TextView pokemonDetailsTextView) {
        this.address = address;
        this.port = port;
        this.pokemonName = pokemonName;
        this.pokemonImageView = pokemonImageView;
        this.pokemonDetailsTextView = pokemonDetailsTextView;
    }

    @Override
    public void run() {
        try {
            // tries to establish a socket connection to the server
            socket = new Socket(address, port);

            // gets the reader and writer for the socket
            BufferedReader bufferedReader = Utilities.getReader(socket);
            PrintWriter printWriter = Utilities.getWriter(socket);

            // sends the city and information type to the server
            printWriter.println(pokemonName);
            printWriter.flush();
            String pokemonDetails;
            String pokemonImageBitmapString;

            // reads the weather information from the server
            pokemonDetails = bufferedReader.readLine();
            final String finalizedPokemonDetails = pokemonDetails;
            Log.i(Constants.TAG, finalizedPokemonDetails);

            // updates the UI with the weather information. This is done using postt() method to ensure it is executed on UI thread
            pokemonDetailsTextView.post(() -> pokemonDetailsTextView.setText(finalizedPokemonDetails));

            pokemonImageBitmapString = bufferedReader.readLine();
            pokemonImageView.post(() -> pokemonImageView.setImageBitmap(StringToBitMap(pokemonImageBitmapString)));
//            while ((pokemonDetails = bufferedReader.readLine()) != null) {
//                final String finalizedPokemonDetails = pokemonDetails;
//                Log.i(Constants.TAG, finalizedPokemonDetails);
//
//                // updates the UI with the weather information. This is done using postt() method to ensure it is executed on UI thread
//                pokemonDetailsTextView.post(() -> pokemonDetailsTextView.setText(finalizedPokemonDetails));
//                pokemonImageView.post(() -> pokemonImageView.setImageBitmap(Str))
//            }
        } // if an exception occurs, it is logged
        catch (IOException ioException) {
            Log.e(Constants.TAG, "[CLIENT THREAD] An exception has occurred: " + ioException.getMessage());
            ioException.printStackTrace();
        } finally {
            if (socket != null) {
                try {
                    // closes the socket regardless of errors or not
                    socket.close();
                } catch (IOException ioException) {
                    Log.e(Constants.TAG, "[CLIENT THREAD] An exception has occurred: " + ioException.getMessage());
                    ioException.printStackTrace();
                }
            }
        }
    }

    public Bitmap StringToBitMap(String encodedString){
        try {
            byte [] encodeByte= Base64.decode(encodedString,Base64.DEFAULT);
            Bitmap bitmap= BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
            return bitmap;
        } catch(Exception e) {
            e.getMessage();
            return null;
        }
    }

}