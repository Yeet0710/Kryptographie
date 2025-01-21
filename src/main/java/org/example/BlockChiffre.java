
package org.example;
import java.util.ArrayList;
import java.util.List;

public class BlockChiffre {
    public static List<String> zerlegeInBloecke(String text, int blockGroesse){
        List<String> bloecke = new ArrayList<>();
        for(int i = 0; i < text.length(); i+= blockGroesse) {
            bloecke.add(text.substring(i, Math.min(i + blockGroesse, text.length())));
        }
        return bloecke;
    }
}
