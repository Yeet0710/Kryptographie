package org.scrum1_6;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class TXTFileTest {

    private TXTFile txtFile;
    private final String testFilePath = "resources/eingabe.txt";

    @BeforeEach
    void setUp() throws IOException {
        txtFile = new TXTFile();

        // Testdatei erstellen
        File file = new File(testFilePath);
        file.getParentFile().mkdirs(); // Falls Ordner fehlt
        FileWriter writer = new FileWriter(file);
        writer.write("Hallo Welt!\nDas ist ein Test.");
        writer.close();
    }

    @Test
    void testReadTXTFile_Success() {
        String content = txtFile.readTXTFile();
        assertTrue(content.contains("Hallo Welt!"));
        assertTrue(content.contains("Das ist ein Test."));
    }

    @Test
    void testReadTXTFile_FileNotFound() {
        // Datei umbenennen oder löschen, um den Fehler zu simulieren
        File file = new File(testFilePath);
        assertTrue(file.delete());

        String result = txtFile.readTXTFile();
        assertEquals("Fehler beim Lesen der Datei", result);
    }
}
