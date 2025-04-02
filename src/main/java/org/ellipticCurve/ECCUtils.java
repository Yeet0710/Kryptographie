package org.ellipticCurve;

import java.io.*;
import java.math.BigInteger;
import java.util.List;

import org.scrum1_6.RSAUTF8;
import org.scrum1_3.schnelleExponentiation;
import org.scrum1_10.DateitypCip; // Importiert Datei-IO für Ciphertext

public class ECCUtils {
    private static final String eEcc_Key = "ecc_private.txt";
    private static final String nEcc_Key = "ecc_public.txt";
    private static final String dEcc_Key = "ecc_modulus.txt";

    private static BigInteger eEcc, nEcc, dEcc;

    public static void loadKeysFromFiles() throws IOException {
        eEcc = loadKey(eEcc_Key);
        nEcc = loadKey(nEcc_Key);
        dEcc = loadKey(dEcc_Key);
    }

    private static BigInteger loadKey(String filename) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            return new BigInteger(reader.readLine());
        }
    }

    public static BigInteger getECCPublicKey() {
        return eEcc;
    }
    public static BigInteger getECCModulus() {
        return nEcc;
    }
    public static BigInteger getECCPrivateKey() {
        return dEcc;
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

    private static String encrypt(String message, BigInteger sharedKey, BigInteger modulus) {
        List<BigInteger> blocks = RSAUTF8.textToBigIntegerBlocks(message, modulus);
        List<BigInteger> encryptedBlocks = modEncryptBlocks(blocks, sharedKey, modulus);
        return RSAUTF8.blocksToCp437String(encryptedBlocks, modulus);
    }

    private static String decrypt(String cipherText, BigInteger sharedKey, BigInteger modulus) {
        List<BigInteger> blocks = RSAUTF8.cp437StringToBlocks(cipherText, modulus);
        List<BigInteger> decryptedBlocks = modDecryptBlocks(blocks, sharedKey, modulus);
        return RSAUTF8.blocksToCp437String(decryptedBlocks, modulus);
    }

    private static List<BigInteger> modEncryptBlocks(List<BigInteger> blocks, BigInteger key, BigInteger modulus) {
        return blocks.stream().map(block -> block.multiply(key).mod(modulus)).toList();
    }

    private static List<BigInteger> modDecryptBlocks(List<BigInteger> blocks, BigInteger key, BigInteger modulus) {
        BigInteger keyInverse = key.modInverse(modulus);
        return blocks.stream().map(block -> block.multiply(keyInverse).mod(modulus)).toList();
    }

    public static void main(String[] args) {
        try {
            // 1. Schlüssel generieren und speichern (dieser Schritt ist implementierungsabhängig)
            System.out.println("Lade ECC Schlüssel...");
            ECCUtils.loadKeysFromFiles();

            // Schlüssel abrufen
            BigInteger privateKey = ECCUtils.getECCPrivateKey();
            BigInteger publicKey = ECCUtils.getECCPublicKey();
            BigInteger modulus = ECCUtils.getECCModulus();

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
