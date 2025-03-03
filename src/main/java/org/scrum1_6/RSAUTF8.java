package org.scrum1_6;

import org.scrum1_3.schnelleExponentiation;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Diese Klasse implementiert eine RSA-Ver- und Entschlüsselung für UTF-8-Strings.
 * Sie nutzt die vorhandenen Methoden aus RSAUtils (Schlüsselverwaltung)
 * und schnelleExponentiation (Modul-Exponentiation).
 */
public class RSAUTF8 {

    // Falls man mit "Partner"-Schlüsseln (z.B. Bob) arbeitest,
    // kannst man hier den Public Key und Modulus "des Partners" setzen.
    private BigInteger friendPubKey;
    private BigInteger friendModulus;

    /**
     * Konstruktor: Lädt beim Erzeugen direkt die Alice- und Bob-Schlüssel aus Dateien,
     * wie in RSAUtils implementiert.
     */
    public RSAUTF8(int bitLength) {
        try {
            RSAUtils.loadKeysFromFiles(); // Alice- & Bob-Schlüssel werden aus Dateien gelesen
        } catch (Exception e) {
            System.out.println("Fehler beim Laden der Schlüssel: " + e.getMessage());
        }
    }

    /**
     * Wandelt einen UTF-8-String in BigInteger-Blöcke um.
     * Hier wird nach Byte-Blockgröße (anstatt Zeichen) getrennt, um Probleme
     * mit mehrbyteigen UTF-8-Zeichen zu vermeiden.
     */
    public List<BigInteger> textToBigIntegerBlocks(String text) {
        // UTF-8-kodierte Bytes
        byte[] textBytes = text.getBytes(StandardCharsets.UTF_8);

        // Die maximale Blockgröße in Bytes (RSA benötigt Blöcke < n).
        // "-1" weil man etwas Platz für die Verschlüsselung selbst (Padding) haben.
        int blockSize = (RSAUtils.getAliceModulus().bitLength() / 8) - 1;

        List<BigInteger> blocks = new ArrayList<>();
        for (int i = 0; i < textBytes.length; i += blockSize) {
            int length = Math.min(blockSize, textBytes.length - i);
            byte[] blockBytes = new byte[length];
            System.arraycopy(textBytes, i, blockBytes, 0, length);

            // BigInteger-Konstruktor mit signum=1 (positiv)
            blocks.add(new BigInteger(1, blockBytes));
        }
        return blocks;
    }

    /**
     * Wandelt BigInteger-Blöcke zurück in einen UTF-8-String.
     * Hierbei werden führende Null-Bytes ggf. entfernt.
     */
    public String bigIntegerBlocksToText(List<BigInteger> blocks) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        for (BigInteger block : blocks) {
            // Die interne Byte-Darstellung kann ein führendes 0-Byte enthalten,
            // wenn das höchstwertige Bit nicht gesetzt war. Das entfernt man hier.
            byte[] blockBytes = block.toByteArray();
            if (blockBytes.length > 1 && blockBytes[0] == 0) {
                byte[] tmp = new byte[blockBytes.length - 1];
                System.arraycopy(blockBytes, 1, tmp, 0, tmp.length);
                blockBytes = tmp;
            }
            baos.write(blockBytes, 0, blockBytes.length);
        }
        return new String(baos.toByteArray(), StandardCharsets.UTF_8);
    }

    /**
     * Verschlüsselt BigInteger-Blöcke und wandelt sie danach in Hex-Strings um.
     * (Für eine kompakte Darstellung könnte man auch Base64 verwenden; hier Hex.)
     */
    public String numbersToString(List<BigInteger> encryptedBlocks) {
        // Byte-Länge von n (Bob) in Bytes, aufgerundet
        int modByteLength = (RSAUtils.getBobModulus().bitLength() + 7) / 8;

        StringBuilder sb = new StringBuilder();
        for (BigInteger block : encryptedBlocks) {
            // Hex-String
            String hex = block.toString(16);
            // Mit führenden Nullen auf modByteLength * 2 (Hex-Zeichen) auffüllen
            while (hex.length() < modByteLength * 2) {
                hex = "0" + hex;
            }
            sb.append(hex).append(" ");
        }
        return sb.toString().trim();
    }

    /**
     * Wandelt den zuvor erstellten Hex-String wieder in BigInteger-Blöcke um.
     */
    public List<BigInteger> stringToNumbers(String encodedText) {
        List<BigInteger> blocks = new ArrayList<>();
        String[] parts = encodedText.split("\\s+");
        for (String part : parts) {
            blocks.add(new BigInteger(part, 16));
        }
        return blocks;
    }

    /**
     * Verschlüsselt einen UTF-8-String, indem er in Blöcke aufgeteilt und
     * blockweise mit (e, n) ver-„hoch“-potenziert wird.
     * - Standardmäßig mit Bobs Schlüssel, falls kein friendKey gesetzt wurde.
     */
    public List<BigInteger> encrypt(String message) {
        // Text -> BigInteger-Blöcke
        List<BigInteger> blocks = textToBigIntegerBlocks(message);
        List<BigInteger> encryptedBlocks = new ArrayList<>();

        // Falls friendKey gesetzt, nimm den; sonst nimm Bob
        BigInteger pubKey = (friendPubKey != null) ? friendPubKey : RSAUtils.getBobPublicKey();
        BigInteger modulus = (friendModulus != null) ? friendModulus : RSAUtils.getBobModulus();

        // RSA-Verschlüsselung pro Block
        for (BigInteger block : blocks) {
            BigInteger cipherBlock = schnelleExponentiation.schnelleExponentiation(block, pubKey, modulus);
            encryptedBlocks.add(cipherBlock);
        }
        return encryptedBlocks;
    }

    /**
     * Entschlüsselt zuvor verschlüsselte Blöcke mit dem eigenen privaten Schlüssel (Bob).
     */
    public String decrypt(List<BigInteger> encryptedBlocks) {
        List<BigInteger> decryptedBlocks = new ArrayList<>();

        for (BigInteger block : encryptedBlocks) {
            BigInteger plainBlock = schnelleExponentiation.schnelleExponentiation(
                    block, RSAUtils.getBobPrivateKey(), RSAUtils.getBobModulus());
            decryptedBlocks.add(plainBlock);
        }
        // Blöcke -> Klartext
        return bigIntegerBlocksToText(decryptedBlocks);
    }

    /**
     * Setzt (falls nötig) einen Partner-Schlüssel (e, n), z.B. wenn du explizit
     * nicht Bob, sondern einen anderen Empfänger verschlüsseln möchtest.
     */
    public void setPublicKey(BigInteger pubKey, BigInteger modulus) {
        this.friendPubKey = pubKey;
        this.friendModulus = modulus;
        System.out.println("Öffentlicher Schlüssel des Empfängers gesetzt: e=" + pubKey + ", n=" + modulus);
    }

    // Beispiel main(), kann auch in einer GUI o.ä. verwendet werden
    public static void main(String[] args) {
        // Initialisierung mit Bitlänge 1024 (oder was du in RSAUtils eingestellt hast)
        RSAUTF8 rsa = new RSAUTF8(1024);

        String klartext = "Hallo, dies ist ein längerer Text mit Umlauten: ÄÖÜß und Emojis: 🚀!";
        System.out.println("Original:\n" + klartext);

        // Verschlüsseln
        List<BigInteger> encryptedBlocks = rsa.encrypt(klartext);
        String encryptedString = rsa.numbersToString(encryptedBlocks);
        System.out.println("\nVerschlüsselt:\n" + encryptedString);

        // Entschlüsseln
        List<BigInteger> decodeBlocks = rsa.stringToNumbers(encryptedString);
        String decrypted = rsa.decrypt(decodeBlocks);
        System.out.println("\nEntschlüsselt:\n" + decrypted);
    }
}
