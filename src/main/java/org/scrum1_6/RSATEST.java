package org.scrum1_6;

import org.scrum1_3.schnelleExponentiation;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Diese Klasse demonstriert ein reines RSA-Verfahren,
 * bei dem der Klartext (UTF‑8) in BigInteger‑Blöcke mit fester Länge (b=6 Bytes)
 * aufgeteilt, mit den Testschlüsseln verschlüsselt und anschließend wieder
 * entschlüsselt wird.
 *
 * Zusätzlich wird ein CP437-kodierter String ausgegeben (wie bei RSAUTF8).
 */
public class RSATEST {

    private static final Charset CP437 = Charset.forName("Cp437");

    // Testschlüssel
    public static final BigInteger testModulus = new BigInteger("791569306435939");
    public static final BigInteger testEncryptionExponent = new BigInteger("15485863");
    public static final BigInteger testDecryptionExponent = new BigInteger("577322589362687");

    /**
     * Berechnet die maximale Blockgröße in Bytes: b = floor((bitLength(n)-1)/8).
     */
    private int getBlockSize(BigInteger modulus) {
        int bitLength = modulus.bitLength();
        return (bitLength - 1) / 8;
    }

    /**
     * Wandelt einen UTF‑8-String in BigInteger-Blöcke um (feste Blocklänge b).
     */
    public List<BigInteger> textToBigIntegerBlocks(String text, BigInteger modulus) {
        byte[] textBytes = text.getBytes(StandardCharsets.UTF_8);
        int blockSize = getBlockSize(modulus);
        List<BigInteger> blocks = new ArrayList<>();
        for (int i = 0; i < textBytes.length; i += blockSize) {
            byte[] blockBytes = new byte[blockSize]; // Fester Puffer der Länge b
            int remaining = textBytes.length - i;
            System.arraycopy(textBytes, i, blockBytes, 0, Math.min(blockSize, remaining));
            // ggf. Padding mit 0x00
            blocks.add(new BigInteger(1, blockBytes));
        }
        return blocks;
    }

    /**
     * Wandelt eine Liste von BigInteger-Blöcken wieder in einen UTF‑8-String um.
     * Dabei werden bei Bedarf führende 0-Bytes entfernt (positive Darstellung).
     */
    public String bigIntegerBlocksToText(List<BigInteger> blocks, int originalLength, BigInteger modulus) {
        int blockSize = getBlockSize(modulus);
        byte[] allBytes = new byte[blocks.size() * blockSize];
        int pos = 0;
        for (BigInteger block : blocks) {
            byte[] blockBytes = block.toByteArray();
            byte[] fixed = new byte[blockSize];
            int copyStart = Math.max(0, blockBytes.length - blockSize);
            int copyLength = Math.min(blockBytes.length, blockSize);
            System.arraycopy(blockBytes, copyStart, fixed, blockSize - copyLength, copyLength);
            System.arraycopy(fixed, 0, allBytes, pos, blockSize);
            pos += blockSize;
        }
        // Padding entfernen, indem nur originalLength Bytes genommen werden
        byte[] result = new byte[originalLength];
        System.arraycopy(allBytes, 0, result, 0, originalLength);
        return new String(result, StandardCharsets.UTF_8);
    }

    /**
     * Verschlüsselt blockweise mit den Testschlüsseln (e, n).
     */
    public RSATestResult encryptTest(String message) {
        byte[] textBytes = message.getBytes(StandardCharsets.UTF_8);
        int originalLength = textBytes.length;
        List<BigInteger> blocks = textToBigIntegerBlocks(message, testModulus);
        List<BigInteger> encryptedBlocks = new ArrayList<>();
        for (BigInteger block : blocks) {
            BigInteger cipherBlock = schnelleExponentiation.schnelleExponentiation(block, testEncryptionExponent, testModulus);
            encryptedBlocks.add(cipherBlock);
        }
        return new RSATestResult(encryptedBlocks, originalLength);
    }

    /**
     * Entschlüsselt blockweise mit den Testschlüsseln (d, n).
     */
    public String decryptTest(RSATestResult result) {
        List<BigInteger> decryptedBlocks = new ArrayList<>();
        for (BigInteger block : result.blocks) {
            BigInteger plainBlock = schnelleExponentiation.schnelleExponentiation(block, testDecryptionExponent, testModulus);
            decryptedBlocks.add(plainBlock);
        }
        return bigIntegerBlocksToText(decryptedBlocks, result.originalLength, testModulus);
    }

    /**
     * Konvertiert eine Liste von BigInteger-Blöcken in einen CP437-String.
     * Jeder Block wird in ein Bytearray fester Länge (basierend auf testModulus) überführt.
     */
    public static String blocksToCp437String(List<BigInteger> blocks, BigInteger modulus) {
        final int modByteLength = (modulus.bitLength() + 7) / 8;
        final StringBuilder sb = new StringBuilder();

        for (BigInteger block : blocks) {
            byte[] blockBytes = block.toByteArray();
            // Auffüllen oder Abschneiden
            if (blockBytes.length < modByteLength) {
                final byte[] tmp = new byte[modByteLength];
                int diff = modByteLength - blockBytes.length;
                System.arraycopy(blockBytes, 0, tmp, diff, blockBytes.length);
                blockBytes = tmp;
            } else if (blockBytes.length > modByteLength) {
                if (blockBytes[0] == 0 && blockBytes.length == modByteLength + 1) {
                    final byte[] tmp = new byte[modByteLength];
                    System.arraycopy(blockBytes, 1, tmp, 0, modByteLength);
                    blockBytes = tmp;
                }
            }
            sb.append(new String(blockBytes, CP437));
        }
        return sb.toString();
    }

    /**
     * Umgekehrte Konvertierung: CP437-String -> BigInteger-Blöcke
     */
    public static List<BigInteger> cp437StringToBlocks(String text, BigInteger modulus) {
        final byte[] allBytes = text.getBytes(CP437);
        final int modByteLength = (modulus.bitLength() + 7) / 8;
        final List<BigInteger> blocks = new ArrayList<>();

        for (int i = 0; i < allBytes.length; i += modByteLength) {
            int length = Math.min(modByteLength, allBytes.length - i);
            byte[] blockBytes = new byte[length];
            System.arraycopy(allBytes, i, blockBytes, 0, length);
            blocks.add(new BigInteger(1, blockBytes));
        }
        return blocks;
    }

    /**
     * Hilfsklasse zur Speicherung der verschlüsselten BigInteger-Blöcke sowie der Originallänge.
     */
    public static class RSATestResult {
        public final List<BigInteger> blocks;
        public final int originalLength;
        public RSATestResult(List<BigInteger> blocks, int originalLength) {
            this.blocks = blocks;
            this.originalLength = originalLength;
        }
    }

    /**
     * Testprogramm:
     * 1. Klartext -> verschlüsseln (BigInteger-Blöcke)
     * 2. BigInteger-Blöcke -> CP437-String (Chiffrat)
     * 3. CP437-String -> BigInteger-Blöcke -> entschlüsseln
     */
    public static void main(String[] args) {
        RSATEST rsa = new RSATEST();
        String klartext = "hallo";
        System.out.println("Original:\n" + klartext);

        // 1) Verschlüsseln: BigInteger-Blöcke
        RSATestResult result = rsa.encryptTest(klartext);

        // Anzeige der BigInteger-Blöcke
        System.out.println("\nVerschlüsselte Blöcke (als BigInteger):");
        for (BigInteger block : result.blocks) {
            System.out.println(block);
        }

        // 2) Aus BigInteger-Blöcken einen CP437-String erstellen
        String encryptedCp437 = blocksToCp437String(result.blocks, testModulus);
        System.out.println("\nChiffrat als CP437-String:\n" + encryptedCp437);

        // 3) Zur Entschlüsselung: CP437-String zurück in Blöcke wandeln
        List<BigInteger> recoveredBlocks = cp437StringToBlocks(encryptedCp437, testModulus);
        // Daraus ein neues RSATestResult bauen (um Originallänge zu behalten)
        RSATestResult recoveredResult = new RSATestResult(recoveredBlocks, result.originalLength);

        // Entschlüsseln
        String decrypted = rsa.decryptTest(recoveredResult);
        System.out.println("\nEntschlüsselt:\n" + decrypted);
    }
}
