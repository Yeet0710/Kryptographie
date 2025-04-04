package org.scrum1_6;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.*;

class RSAUtils2047Test {

    // Vor jedem Test sicherstellen, dass Schlüssel existieren und geladen werden.
    @BeforeEach
    void setUp() throws Exception {
        File eFile = new File("rsa2047_e.txt");
        File nFile = new File("rsa2047_n.txt");
        File dFile = new File("rsa2047_d.txt");
        if (!eFile.exists() || !nFile.exists() || !dFile.exists()) {
            // Schlüssel generieren, falls sie noch nicht existieren
            RSAUtils2047.generateAndSaveKeys("rsa2047_e.txt", "rsa2047_n.txt", "rsa2047_d.txt", 2047);
        }
        RSAUtils2047.loadKeysFromFiles();
    }

    @Test
    void testKeyGeneration() {
        BigInteger e = RSAUtils2047.getAlicePublicKey();
        BigInteger n = RSAUtils2047.getAliceModulus();
        BigInteger d = RSAUtils2047.getAlicePrivateKey();
        // Es wird erwartet, dass alle Schlüssel nicht null sind.
        assertNotNull(e, "Der öffentliche Schlüssel e sollte existieren.");
        assertNotNull(n, "Der Modulus n sollte existieren.");
        assertNotNull(d, "Der private Schlüssel d sollte existieren.");
    }

    @Test
    void testSignAndVerify() throws Exception {
        String message = "Test message for RSA";
        BigInteger signature = RSAUtils2047.sign(message);
        // Die Signatur darf nicht null sein
        assertNotNull(signature, "Die erzeugte Signatur darf nicht null sein.");
        // Verifikation der Signatur – das Ergebnis sollte true ergeben
        boolean valid = RSAUtils2047.verify(message, signature);
        assertTrue(valid, "Die Verifikation der Signatur muss erfolgreich sein.");
    }
}
