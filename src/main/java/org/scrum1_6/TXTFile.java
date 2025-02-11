package org.scrum1_6;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

/**
 * Diese Klasse wird benutzt um eine Textdatei unter resources mit dem Namen "eingabe.txt" zu lesen.
 */
public class TXTFile {

    public String readTXTFile() {
        try {
            File file = new File("resources/eingabe.txt");
            Scanner scanner = new Scanner(file);
            String text = "";
            while (scanner.hasNextLine()) {
                text += scanner.nextLine() + "\n";
            }
            scanner.close();
            return text;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return "Fehler beim Lesen der Datei";
        }
    }

}
