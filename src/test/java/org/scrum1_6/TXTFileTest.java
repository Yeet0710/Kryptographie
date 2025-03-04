package org.scrum1_6;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class TXTFileTest {

    private TXTFile txtFile;
    private final String testFilePath = "resources/eingabe.txt"; // Erwarteter Pfad laut TXTFile

    @BeforeEach
    void setUp() throws IOException {
        txtFile = new TXTFile(); // Verwende den Standardkonstruktor ohne Parameter

        File file = new File(testFilePath);
        file.getParentFile().mkdirs(); // Erstelle ggf. fehlende Verzeichnisse

        try (FileWriter writer = new FileWriter(file)) {
            writer.write("Hallo Welt!\nDas ist ein Test.");
        }
    }

    @Test
    void testReadTXTFile_Success() {
        String content = txtFile.readTXTFile();
        assertTrue(content.contains("Hallo Welt!"));
        assertTrue(content.contains("Das ist ein Test."));
    }

    @Test
    void testReadTXTFile_FileNotFound() {
        File file = new File(testFilePath);
        if (file.exists() && !file.delete()) {
            fail("Die Testdatei konnte nicht gelöscht werden!");
        }
        String result = txtFile.readTXTFile();
        assertEquals("Fehler beim Lesen der Datei", result);
    }
}
