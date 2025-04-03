package org.ellipticCurve;

import java.io.*;
import java.math.BigInteger;

import org.scrum1_27.ElGamalPublicKeyEncryption;
import org.scrum1_10.DateitypCip; // Importiert Datei-IO für Ciphertext

public class ECCUtils {
    private static final String pEcc_Key = "ecc_prime.txt";
    private static final String gEcc_Key = "ecc_primeroot.txt";
    private static final String yEcc_Key = "ecc_publicKey.txt";
    private static final String xEcc_Key = "ecc_privateKey.txt";
    private static final String cipherFile = "ciphertext.txt"; // Datei für gespeichertes Chiffrat

    private static BigInteger pEcc, gEcc, yEcc, xEcc;

    public static void loadKeysFromFiles() throws IOException {
        pEcc = loadKey(pEcc_Key);
        gEcc = loadKey(gEcc_Key);
        yEcc = loadKey(yEcc_Key);
        xEcc = loadKey(xEcc_Key);
    }

    private static BigInteger loadKey(String filename) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            return new BigInteger(reader.readLine());
        }
    }

    public static BigInteger getECCPrime() {
        return pEcc;
    }
    public static BigInteger getECCPrimeRoot() {
        return gEcc;
    }
    public static BigInteger getECCPublicKey() {
        return yEcc;
    }
    public static BigInteger getECCPrivateKey() {
        return xEcc;
    }

    public static void saveCiphertextToFile(BigInteger a, BigInteger b, String filename) {
        String ciphertext = a.toString() + "\n" + b.toString();
        DateitypCip.write(ciphertext, filename);
    }

    public static BigInteger[] loadCiphertextFromFile(String filename) {
        String[] parts = DateitypCip.read(filename).split("\n");
        return new BigInteger[]{new BigInteger(parts[0]), new BigInteger(parts[1])};
    }

    public static void main(String[] args) {
        try {
            // 1. Schlüssel laden
            System.out.println("Lade ECC Schlüssel...");
            ECCUtils.loadKeysFromFiles();

            // Schlüssel abrufen
            BigInteger privateKey = ECCUtils.getECCPrivateKey(); // x (privater Schlüssel)
            BigInteger publicKey = ECCUtils.getECCPublicKey();   // y (öffentlicher Schlüssel)
            BigInteger prime = ECCUtils.getECCPrime();           // p (Primzahl)
            BigInteger primeroot = ECCUtils.getECCPrimeRoot();   // g (Primitivwurzel)

            System.out.println("Privater Schlüssel (x): " + privateKey);
            System.out.println("Öffentlicher Schlüssel (y): " + publicKey);
            System.out.println("Primzahl (p): " + prime);
            System.out.println("Primitivwurzel (g): " + primeroot);

            // 2. Testnachricht verschlüsseln mit ElGamal
            String originalMessage = "Hello, ECC!";
            System.out.println("Original Message: " + originalMessage);

            // Verschlüsselung
            BigInteger[] encryptedMessage = ElGamalPublicKeyEncryption.encrypt(
                    new BigInteger(originalMessage.getBytes()), prime, primeroot, publicKey);

            System.out.println("Verschlüsselter Wert a: " + encryptedMessage[0]);
            System.out.println("Verschlüsselter Wert b: " + encryptedMessage[1]);

            // 3. Speichern des Chiffrats
            saveCiphertextToFile(encryptedMessage[0], encryptedMessage[1], cipherFile);
            System.out.println("Ciphertext gespeichert.");

            // 4. Laden des Chiffrats
            BigInteger[] loadedCiphertext = loadCiphertextFromFile(cipherFile);
            System.out.println("Geladener Chiffrat: a = " + loadedCiphertext[0] + ", b = " + loadedCiphertext[1]);

            // 5. Entschlüsselung
            BigInteger decryptedNumber = ElGamalPublicKeyEncryption.decrypt(
                    loadedCiphertext[0], loadedCiphertext[1], prime, privateKey);

            String decryptedMessage = new String(decryptedNumber.toByteArray());
            System.out.println("Entschlüsselte Nachricht: " + decryptedMessage);

            // 6. Prüfen, ob die entschlüsselte Nachricht korrekt ist
            if (originalMessage.equals(decryptedMessage)) {
                System.out.println("Test erfolgreich: Entschlüsselte Nachricht stimmt überein!");
            } else {
                System.out.println("Test fehlgeschlagen: Entschlüsselte Nachricht weicht ab!");
            }

        } catch (IOException e) {
            System.err.println("Fehler beim Laden der Schlüssel: " + e.getMessage());
        }
    }
}
