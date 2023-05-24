package ro.pub.cs.systems.eim.practicaltest02;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;

import java.util.ArrayList;

public class PokemonDetails {
    final String name;
    final String image_url;
    final ArrayList<String> types;
    final ArrayList<String>abilities;
    final Bitmap imageBmp;

    public PokemonDetails(String name, String image_url, ArrayList<String> types, ArrayList<String> abilities, Bitmap imageBmp) {
        this.name = name;
        this.image_url = image_url;
        this.types = types;
        this.abilities = abilities;
        this.imageBmp = imageBmp;
    }

    public String getName() {
        return name;
    }

    public String getImage_url() {
        return image_url;
    }

    public ArrayList<String> getTypes() {
        return types;
    }

    public ArrayList<String> getAbilities() {
        return abilities;
    }

    @NonNull
    @Override
    public String toString() {
        return "PokemonDetails{" + "name='" + name + '\'' + ", types='" + types.toString() + '\'' + ", abilities='" + abilities.toString() + '}';
    }
}
