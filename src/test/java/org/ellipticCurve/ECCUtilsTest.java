package org.ellipticCurve;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;

public class ECCUtilsTest {

    // Die Dateinamen entsprechen denen in ECCUtils
    private static final String PRIME_FILE = "ecc_prime.txt";
    private static final String PRIMEROOT_FILE = "ecc_primeroot.txt";
    private static final String PUBLIC_FILE = "ecc_publicKey.txt";
    private static final String PRIVATE_FILE = "ecc_privateKey.txt";
    private static final String CIPHER_FILE = "ciphertext.txt";

    // Testschlüssel – Beachte:
    // Damit die Verschlüsselung mit ElGamal funktioniert, muss die Nachricht (als BigInteger) kleiner als p sein.
    // Für diesen Test wählen wir ein relativ kleines p und eine kurze Nachricht.
    private static final BigInteger p = new BigInteger("1000003"); // p (Primzahl)
    private static final BigInteger g = new BigInteger("2");        // g (Primitivwurzel)
    private static final BigInteger x = new BigInteger("12345");      // privater Schlüssel
    private static BigInteger y;                                     // öffentlicher Schlüssel, y = g^x mod p

    @BeforeAll
    static void setUpKeys() throws IOException {
        // Berechne den öffentlichen Schlüssel: y = g^x mod p
        y = g.modPow(x, p);
        // Schreibe die Schlüssel in die entsprechenden Dateien
        writeToFile(PRIME_FILE, p.toString());
        writeToFile(PRIMEROOT_FILE, g.toString());
        writeToFile(PUBLIC_FILE, y.toString());
        writeToFile(PRIVATE_FILE, x.toString());
    }

    @AfterAll
    static void tearDown() {
        // Lösche alle Testdateien nach den Tests
        new File(PRIME_FILE).delete();
        new File(PRIMEROOT_FILE).delete();
        new File(PUBLIC_FILE).delete();
        new File(PRIVATE_FILE).delete();
        new File(CIPHER_FILE).delete();
        new File("test_ciphertext.txt").delete();
    }

    private static void writeToFile(String filename, String content) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            writer.write(content);
        }
    }

    @Test
    void testLoadKeysFromFiles() throws IOException {
        // Lade die Schlüssel aus den Dateien
        ECCUtils.loadKeysFromFiles();

        // Überprüfe, ob die geladenen Schlüssel den erwarteten Werten entsprechen
        assertEquals(p, ECCUtils.getECCPrime(), "Der geladene Primzahlwert muss übereinstimmen.");
        assertEquals(g, ECCUtils.getECCPrimeRoot(), "Der geladene Primwurzelwert muss übereinstimmen.");
        assertEquals(y, ECCUtils.getECCPublicKey(), "Der geladene öffentliche Schlüssel muss übereinstimmen.");
        assertEquals(x, ECCUtils.getECCPrivateKey(), "Der geladene private Schlüssel muss übereinstimmen.");
    }

    @Test
    void testSaveAndLoadCiphertext() {
        // Verwende für diesen Test ein eigenes Dateinamen (um den Standard-Ciphertext nicht zu überschreiben)
        String testFilename = "test_ciphertext.txt";
        BigInteger a = new BigInteger("123456");
        BigInteger b = new BigInteger("654321");

        // Speichere den Chiffrat-Paar (a, b)
        ECCUtils.saveCiphertextToFile(a, b, testFilename);

        // Lese das gespeicherte Chiffrat wieder ein
        BigInteger[] loaded = ECCUtils.loadCiphertextFromFile(testFilename);
        assertEquals(a, loaded[0], "Der geladene Wert a muss übereinstimmen.");
        assertEquals(b, loaded[1], "Der geladene Wert b muss übereinstimmen.");
    }

    @Test
    void testIntegrationEncryptionDecryption() throws IOException {
        // Zuerst müssen die Schlüssel geladen werden
        ECCUtils.loadKeysFromFiles();

        // Wähle eine kurze Nachricht, die (als BigInteger) kleiner als p sein muss.
        String originalMessage = "Hi";
        BigInteger m = new BigInteger(originalMessage.getBytes());
        assertTrue(m.compareTo(p) < 0, "Die Nachricht muss kleiner als die Primzahl p sein.");

        // Verschlüsselung mittels ElGamal (aus org.scrum1_27.ElGamalPublicKeyEncryption)
        // Hier wird m verschlüsselt: ciphertext = [a, b]
        BigInteger[] ciphertext = org.scrum1_27.ElGamalPublicKeyEncryption.encrypt(m, p, g, y);

        // Speichere das Chiffrat in die Datei (hier wird der Standard-Dateiname genutzt)
        ECCUtils.saveCiphertextToFile(ciphertext[0], ciphertext[1], CIPHER_FILE);

        // Lade das Chiffrat wieder ein
        BigInteger[] loadedCiphertext = ECCUtils.loadCiphertextFromFile(CIPHER_FILE);

        // Entschlüssle das geladene Chiffrat
        BigInteger decrypted = org.scrum1_27.ElGamalPublicKeyEncryption.decrypt(
                loadedCiphertext[0], loadedCiphertext[1], p, x);

        String decryptedMessage = new String(decrypted.toByteArray());
        assertEquals(originalMessage, decryptedMessage, "Die entschlüsselte Nachricht muss dem Original entsprechen.");
    }
}
