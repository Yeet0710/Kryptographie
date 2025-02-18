package org.scrum1_6;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.scrum1_3.schnelleExponentiation;

public class RSAUTF8 {
    private BigInteger friendPubKey;
    private BigInteger friendModulus;

    public RSAUTF8(int bitLength) {
        try {
            RSAUtils.loadKeysFromFiles(); // Lade Alice & Bobs Schlüssel aus Datei
        } catch (Exception ex) {
            System.out.println("Fehler beim Laden der Schlüssel: " + ex.getMessage());
        }
    }

    // Text in BigInteger-Blöcke umwandeln
    public List<BigInteger> textToBigIntegerBlocks(String text) {
        List<BigInteger> blocks = new ArrayList<>();
        int BLOCK_SIZE = (RSAUtils.getAliceModulus().bitLength() / 8) - 1;

        for (int i = 0; i < text.length(); i += BLOCK_SIZE) {
            String block = text.substring(i, Math.min(i + BLOCK_SIZE, text.length()));
            byte[] blockBytes = block.getBytes(StandardCharsets.UTF_8);
            blocks.add(new BigInteger(1, blockBytes));
        }
        return blocks;
    }

    // BigInteger-Blöcke zurück in Text umwandeln
    public String bigIntegerBlocksToText(List<BigInteger> blocks) {
        StringBuilder text = new StringBuilder();
        for (BigInteger block : blocks) {
            text.append(new String(block.toByteArray(), StandardCharsets.UTF_8));
        }
        return text.toString();
    }

    // Erste Verschlüsselung: Klartext → Zahlen → RSA
    // falls friendPubKey und friendModulus nicht gesetz sind, verwende dann die von Bobeee
    public List<BigInteger> encrypt(String message) {
        List<BigInteger> blocks = textToBigIntegerBlocks(message);
        List<BigInteger> encryptedBlocks = new ArrayList<>();

        BigInteger pubKey = (friendPubKey != null) ? friendPubKey : RSAUtils.getBobPublicKey();
        BigInteger modulus = (friendModulus != null) ? friendModulus : RSAUtils.getBobModulus();

        for (BigInteger block : blocks) {
            encryptedBlocks.add(schnelleExponentiation.schnelleExponentiation(block, pubKey, modulus));
        }
        return encryptedBlocks;
    }

    // Zahlen in Zeichen umwandeln (Basis 36 für kompaktere Darstellung)
    public String numbersToString(List<BigInteger> encryptedBlocks) {
        StringBuilder result = new StringBuilder();
        for (BigInteger block : encryptedBlocks) {
            result.append(block.toString(36)).append(" "); // Basis 36 für kompakte Darstellung
        }
        return result.toString().trim();
    }

    // Zeichen zurück in Zahlen umwandeln
    public List<BigInteger> stringToNumbers(String encodedText) {
        List<BigInteger> blocks = new ArrayList<>();
        String[] parts = encodedText.split(" ");
        for (String part : parts) {
            blocks.add(new BigInteger(part, 36));
        }
        return blocks;
    }

    // Entschlüsselung: Zahlen in Klartext zurückführen
    public String decrypt(List<BigInteger> encryptedBlocks) {
        List<BigInteger> decryptedBlocks = new ArrayList<>();

        for (BigInteger block : encryptedBlocks) {
            decryptedBlocks.add(schnelleExponentiation.schnelleExponentiation(block, RSAUtils.getBobPrivateKey(), RSAUtils.getBobModulus()));
        }

        return bigIntegerBlocksToText(decryptedBlocks);
    }

    public void setPublicKey(BigInteger pubKey, BigInteger modulus) {
        this.friendPubKey = pubKey;
        this.friendModulus = modulus;
        System.out.println("Öffentlicher Schlüssel des Empfängers gesetzt!");
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Lade RSA-Schlüssel für Alice und Bob...");
        RSAUTF8 aliceRSA = new RSAUTF8(1024);
        RSAUTF8 bobRSA = new RSAUTF8(1024);

        System.out.println("\n==== Alice's Schlüssel ====");
        System.out.println("Öffentlicher Schlüssel (e): " + RSAUtils.getAlicePublicKey());
        System.out.println("Modulus (n): " + RSAUtils.getAliceModulus());
        System.out.println("Privater Schlüssel (d): " + RSAUtils.getAlicePrivateKey());

        System.out.println("\n==== Bob's Schlüssel ====");
        System.out.println("Öffentlicher Schlüssel (e): " + RSAUtils.getBobPublicKey());
        System.out.println("Modulus (n): " + RSAUtils.getBobModulus());
        System.out.println("Privater Schlüssel (d): " + RSAUtils.getBobPrivateKey());

        System.out.println("\nGebe die Nachricht ein, die verschlüsselt werden soll:");
        String message = scanner.nextLine();
        System.out.println("\nOriginal Nachricht: " + message);

        // Signatur & Verifikation
        try {
            System.out.println("\nSignieren der Nachricht mit Alice's privatem Schlüssel...");
            BigInteger signature = RSAUtils.sign(message);
            System.out.println("Generierte Signatur: " + signature);

            System.out.println("\nVerifizieren der Signatur mit Alice's öffentlichem Schlüssel...");
            boolean isValid = RSAUtils.verify(message, signature);
            System.out.println("Verifikation erfolgreich: " + isValid);
        } catch (NoSuchAlgorithmException e) {
            System.out.println("Fehler bei der Hash-Signierung: " + e.getMessage());
        }

        // 1. Verschlüsselung (Text → Zahlen → RSA)
        System.out.println("\n Starte Verschlüsselung (Klartext → Zahlen)... ");
        List<BigInteger> encryptedNumbers = aliceRSA.encrypt(message);
        encryptedNumbers.forEach(System.out::println);

        // 2. Umwandlung der verschlüsselten Zahlen in Zeichen
        System.out.println("\n Wandelt die verschlüsselten Zahlen in eine Zeichenkette um...");
        String encodedText = aliceRSA.numbersToString(encryptedNumbers);
        System.out.println(encodedText);

        // 3. Zeichenkette zurück in Zahlen umwandeln
        System.out.println("\nWandelt die Zeichenkette zurück in Zahlen...");
        List<BigInteger> decodedNumbers = bobRSA.stringToNumbers(encodedText);
        decodedNumbers.forEach(System.out::println);

        // 4. Entschlüsselung der Zahlen in Klartext
        System.out.println("\nEntschlüsselt die Zahlen zurück in Klartext...");
        String decryptedMessage = bobRSA.decrypt(decodedNumbers);
        System.out.println(decryptedMessage);

        System.out.println("\nProzess abgeschlossen!");
    }
}
