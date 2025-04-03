package org.ellipticCurve;

import java.io.*;
import java.math.BigInteger;
import java.util.List;

import org.scrum1_27.ElGamalPublicKeyEncryption;
import org.scrum1_6.RSAUTF8;
import org.scrum1_3.schnelleExponentiation;
import org.scrum1_10.DateitypCip; // Importiert Datei-IO für Ciphertext

public class ECCUtils {
    private static final String pEcc_Key = "ecc_prime.txt";
    private static final String gEcc_Key = "ecc_primeroot.txt";
    private static final String yEcc_Key = "ecc_publicKey.txt";
    private static final String xEcc_Key = "ecc_privateKey.txt";

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

    public static void saveCiphertextToFile(String ciphertext, String filename) {
        DateitypCip.write(ciphertext, filename);
    }

    public static String loadCiphertextFromFile(String filename) {
        return DateitypCip.read(filename);
    }

    private static BigInteger deriveSharedKey(BigInteger privateKey, BigInteger publicKey, BigInteger modulus) {
        return schnelleExponentiation.schnelleExponentiation(publicKey, privateKey, modulus);
    }

    private static BigInteger[] encrypt(String message, BigInteger p, BigInteger g, BigInteger y) {
        BigInteger messageAsNumber = new BigInteger(message.getBytes());
        return ElGamalPublicKeyEncryption.encrypt(messageAsNumber, p, g, y);
    }

    private static String decrypt(BigInteger a, BigInteger b, BigInteger p, BigInteger x) {
        BigInteger decryptedNumber = ElGamalPublicKeyEncryption.decrypt(a, b, p, x);
        return new String(decryptedNumber.toByteArray());
    }

    public static void main(String[] args) {
        try {
            // 1. Schlüssel generieren und speichern (dieser Schritt ist implementierungsabhängig)
            System.out.println("Lade ECC Schlüssel...");
            ECCUtils.loadKeysFromFiles();

            // Schlüssel abrufen
            BigInteger privateKey = ECCUtils.getECCPrime();
            BigInteger publicKey = ECCUtils.getECCPrimeRoot();
            BigInteger modulus = ECCUtils.getECCPublicKey();

            System.out.println("Private Key: " + privateKey);
            System.out.println("Public Key: " + publicKey);
            System.out.println("Modulus: " + modulus);

            // 2. Testnachricht zum Verschlüsseln
            String originalMessage = "Hello, ECC!";
            System.out.println("Original Message: " + originalMessage);

            // 3. Verschlüsseln
            BigInteger sharedKey = privateKey.multiply(publicKey).mod(modulus); // Einfache Kombination
            String encryptedMessage = ECCUtils.encrypt(originalMessage, sharedKey, modulus);
            System.out.println("Encrypted Message: " + encryptedMessage);

            // 4. Speichern des Ciphertexts in Datei
            ECCUtils.saveCiphertextToFile(encryptedMessage, "testCipher");
            System.out.println("Ciphertext gespeichert.");

            // 5. Laden des Ciphertexts aus Datei
            String loadedCiphertext = ECCUtils.loadCiphertextFromFile("testCipher");
            System.out.println("Geladener Ciphertext: " + loadedCiphertext);

            // 6. Entschlüsseln
            String decryptedMessage = ECCUtils.decrypt(loadedCiphertext, sharedKey, modulus);
            System.out.println("Decrypted Message: " + decryptedMessage);

            // Prüfen, ob die ursprüngliche Nachricht wiederhergestellt wurde
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
