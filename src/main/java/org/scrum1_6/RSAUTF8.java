package org.scrum1_6;

import org.scrum1_3.schnelleExponentiation;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Diese Klasse implementiert ein RSA-Ver- und Entschlüsselungsverfahren für UTF‑8-Strings.
 *
 * Vorgehensweise:
 * 1. Der Klartext wird als UTF‑8-String in ein Bytearray konvertiert.
 * 2. Das Bytearray wird mittels Null‑Padding (d.h. Auffüllen mit 0x00) so erweitert,
 *    dass seine Länge ein Vielfaches der dynamisch berechneten Blocklänge ist.
 * 3. Das gepaddete Bytearray wird in Blöcke umgewandelt.
 * 4. Die Blöcke werden einzeln verschlüsselt (RSA: c = m^e mod n).
 * 5. Das Chiffrat wird in Blöcke der Länge b' (dynamisch berechnet) konvertiert und als CP437-String ausgegeben.
 * 6. Beim Entschlüsseln werden die Blöcke wieder zusammengefügt und durch .trim()
 *    (das alle Steuerzeichen, inklusive 0x00, entfernt) bereinigt.
 */
public class RSAUTF8 {

    // Charset für CP437, das alle 256 Byte-Werte korrekt darstellt
    private static final Charset CP437 = Charset.forName("Cp437");

    private BigInteger friendPubKey;
    private BigInteger friendModulus;

    /**
     * Hilfsklasse zur Speicherung der verschlüsselten Blöcke.
     */
    public static class RSAResult {
        public final List<BigInteger> blocks;
        public RSAResult(List<BigInteger> blocks) {
            this.blocks = blocks;
        }
    }

    /**
     * Konstruktor: Lädt beim Erzeugen die Schlüssel aus den entsprechenden Dateien.
     */
    public RSAUTF8(int bitLength) {
        try {
            RSAUtils.loadKeysFromFiles();
            RSAUtils2047.loadKeysFromFiles();
        } catch (Exception e) {
            System.out.println("Fehler beim Laden der Schlüssel: " + e.getMessage());
        }
    }

    /**
     * Berechnet den natürlichen Logarithmus eines BigIntegers.
     */
    public static double logBigInteger(BigInteger val) {
        int blex = val.bitLength() - 512;
        if (blex > 0) {
            val = val.shiftRight(blex);
        }
        double result = Math.log(val.doubleValue());
        return result + blex * Math.log(2);
    }

    /**
     * Berechnet die Blocklänge basierend auf log256(n).
     * Wird plusOne true gesetzt, so wird 1 addiert (für die Darstellung des Chiffrats).
     */
    public static int calculateBlockSize(BigInteger modulus, boolean plusOne) {
        int blockSize = (int) Math.floor(logBigInteger(modulus) / Math.log(256));
        if (plusOne) {
            blockSize++;
        }
       // System.out.println("DEBUG: modulus = " + modulus);
        //System.out.println("DEBUG: Berechnete Blockgröße (plusOne=" + plusOne + "): " + blockSize + " Bytes");
        return blockSize;
    }

    /**
     * Liefert die Blocklänge für die Verschlüsselung (Klartext-Blöcke).
     */
    public static int getEncryptionBlockSize(BigInteger modulus) {
        return calculateBlockSize(modulus, false);
    }

    /**
     * Liefert die Blocklänge für die Darstellung des Chiffrats (CP437-Blöcke).
     */
    public static int getDecryptionBlockSize(BigInteger modulus) {
        return calculateBlockSize(modulus, true);
    }

    /**
     * Wendet Null-Padding an, sodass die Länge des Bytearrays ein Vielfaches der blockSize ist.
     * (mit 0x00 aufgefüllt.)
     */
    public static byte[] zeroPadData(byte[] data, int blockSize) {
        int missing = data.length % blockSize;
        if (missing == 0) {
            return data; // bereits Vielfaches
        }
        int padLength = blockSize - missing;
        byte[] padded = new byte[data.length + padLength];
        System.arraycopy(data, 0, padded, 0, data.length);
        // Java-Arrays sind per Default mit 0 initialisiert.
        return padded;
    }

    /**
     * Wandelt einen UTF‑8-String in BigInteger-Blöcke um.
     * Hierbei wird zuerst das UTF‑8-Bytearray mittels Null-Padding aufgefüllt.
     */
    public static List<BigInteger> textToBigIntegerBlocks(final String text, final BigInteger modulus) {
        byte[] textBytes = text.getBytes(StandardCharsets.UTF_8);
        System.out.println("-----------------------");
        System.out.println("-----------------------");
        System.out.println("Anzahl der Zeichen: " + text.length());
        System.out.println("DEBUG: Ursprünglicher Text: " + text);
        System.out.println("DEBUG: Länge des ursprünglichen Bytearrays: " + textBytes.length);
        System.out.println("-----------------------");
        System.out.println("-----------------------");

        int blockSize = getEncryptionBlockSize(modulus);
        System.out.println("DEBUG: Berechnete Verschlüsselungsblockgröße: " + blockSize + " Bytes");

        byte[] paddedBytes = zeroPadData(textBytes, blockSize);
        System.out.println("DEBUG: Länge des gepaddeten Bytearrays: " + paddedBytes.length);
        System.out.println("DEBUG: Anzahl der erzeugten Blöcke: " + (paddedBytes.length / blockSize));

        List<BigInteger> blocks = new ArrayList<>();
        for (int i = 0; i < paddedBytes.length; i += blockSize) {
            byte[] blockBytes = Arrays.copyOfRange(paddedBytes, i, i + blockSize);
            blocks.add(new BigInteger(1, blockBytes));
        }
        return blocks;
    }

    /**
     * Stellt sicher, dass aus einem BigInteger exakt blockLength Byte gewonnen werden,
     * indem ggf. aufgefüllt oder abgeschnitten wird.
     */
    public static byte[] bigIntegerBlocksToBytes(List<BigInteger> blocks, int blockLength) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        //System.out.println("DEBUG: Anzahl der Blöcke zum Zusammenfügen: " + blocks.size());
        try {
            for (int i = 0; i < blocks.size(); i++) {
                BigInteger block = blocks.get(i);
                byte[] blockBytes = block.toByteArray();
                //System.out.println("DEBUG: Block " + i + " original Länge: " + blockBytes.length);
                byte[] fixedBlock = new byte[blockLength];
                if (blockBytes.length > blockLength) {
                    System.arraycopy(blockBytes, blockBytes.length - blockLength, fixedBlock, 0, blockLength);
                   // System.out.println("DEBUG: Block " + i + " wurde gekürzt auf " + blockLength + " Bytes");
                } else if (blockBytes.length < blockLength) {
                    System.arraycopy(blockBytes, 0, fixedBlock, blockLength - blockBytes.length, blockBytes.length);
                  //  System.out.println("DEBUG: Block " + i + " wurde aufgefüllt auf " + blockLength + " Bytes");
                } else {
                    fixedBlock = blockBytes;
                  //  System.out.println("DEBUG: Block " + i + " hat bereits die korrekte Länge: " + blockLength + " Bytes");
                }
                outputStream.write(fixedBlock);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        byte[] result = outputStream.toByteArray();
       // System.out.println("DEBUG: Gesamtzahl der Bytes nach Zusammenfügen: " + result.length);
        return result;
    }

    /**
     * Wandelt eine Liste von BigInteger-Blöcken in einen CP437-kodierten String um.
     */
    public static String blocksToCp437String(List<BigInteger> blocks, BigInteger modulus) {
        int modBlockSize = getDecryptionBlockSize(modulus);
       // System.out.println("DEBUG: Berechnete Blockgröße für CP437-Darstellung: " + modBlockSize + " Bytes");
        byte[] allBytes = bigIntegerBlocksToBytes(blocks, modBlockSize);
       // System.out.println("DEBUG: Anzahl der Bytes im zusammengesetzten CP437-Bytearray: " + allBytes.length);
        String cp437String = new String(allBytes, CP437);
       // System.out.println("DEBUG: CP437-String: " + cp437String);
        return cp437String;
    }

    /**
     * Wandelt einen CP437-kodierten String in eine Liste von BigInteger-Blöcken um.
     */
    public static List<BigInteger> cp437StringToBlocks(final String text, final BigInteger modulus) {
        byte[] allBytes = text.getBytes(CP437);
        int modBlockSize = getDecryptionBlockSize(modulus);
        System.out.println("-----------------------");
        System.out.println("-----------------------");
        System.out.println("DEBUG: Gesamtlänge des eingelesenen CP437-Bytearrays: " + allBytes.length);
        System.out.println("DEBUG: Erwartete Blockgröße: " + modBlockSize + " Bytes");
        System.out.println("-----------------------");
        System.out.println("-----------------------");

        List<BigInteger> blocks = new ArrayList<>();
        for (int i = 0; i < allBytes.length; i += modBlockSize) {
            byte[] blockBytes = Arrays.copyOfRange(allBytes, i, i + modBlockSize);
            blocks.add(new BigInteger(1, blockBytes));
        }
       // System.out.println("DEBUG: Anzahl der extrahierten Blöcke: " + blocks.size());
        return blocks;
    }

    /**
     * Verschlüsselt einen UTF‑8-String mittels RSA.
     * Hierbei wird beispielsweise Alice Bobs öffentlichen Schlüssel (oder umgekehrt) verwenden.
     */
    public RSAResult encrypt(String message, boolean fromAlice) {
        BigInteger pubKey, modulus;
        if (fromAlice) {
            pubKey = RSAUtils.getBobPublicKey();
            modulus = RSAUtils.getBobModulus();
        } else {
            pubKey = RSAUtils2047.getAlicePublicKey();
            modulus = RSAUtils2047.getAliceModulus();
        }
        List<BigInteger> blocks = textToBigIntegerBlocks(message, modulus);
        List<BigInteger> encryptedBlocks = new ArrayList<>();
        long startTime = System.currentTimeMillis();
        for (BigInteger block : blocks) {
            BigInteger cipherBlock = schnelleExponentiation.schnelleExponentiation(block, pubKey, modulus);
            encryptedBlocks.add(cipherBlock);
        }
        long encryptionTime = System.currentTimeMillis() - startTime;
        //System.out.println("Verschlüsselungszeit: " + encryptionTime + " ms");
        return new RSAResult(encryptedBlocks);
    }

    /**
     * Entschlüsselt ein RSAResult.
     * Die verschlüsselten Blöcke werden entschlüsselt, in ein Bytearray umgewandelt und anschließend
     * mittels String.trim() bereinigt (um das durch Null-Padding entstandene, überflüssige 0x00 zu entfernen).
     */
    public String decrypt(RSAResult result, boolean toAlice) {
        BigInteger privKey, modulus;
        if (toAlice) {
            privKey = RSAUtils2047.getAlicePrivateKey();
            modulus = RSAUtils2047.getAliceModulus();
        } else {
            privKey = RSAUtils.getBobPrivateKey();
            modulus = RSAUtils.getBobModulus();
        }
        List<BigInteger> decryptedBlocks = new ArrayList<>();
        long startTime = System.currentTimeMillis();
        for (BigInteger block : result.blocks) {
            BigInteger plainBlock = schnelleExponentiation.schnelleExponentiation(block, privKey, modulus);
            decryptedBlocks.add(plainBlock);
        }
       // System.out.println("verwendeter modulus: " + modulus);
        long decryptionTime = System.currentTimeMillis() - startTime;
        int blockSize = getEncryptionBlockSize(modulus);
        byte[] allBytes = bigIntegerBlocksToBytes(decryptedBlocks, blockSize);
        // Verwende trim(), um führende/trailing Nullbytes zu entfernen (wie es auch im BlockCipher erfolgt)
        String clearText = new String(allBytes, StandardCharsets.UTF_8).trim();
       // System.out.println("Entschlüsselungszeit: " + decryptionTime + " ms");
        return clearText;
    }

    public void setPublicKey(BigInteger pubKey, BigInteger modulus) {
        this.friendPubKey = pubKey;
        this.friendModulus = modulus;
        if (pubKey == null || modulus == null) {
            System.out.println("Partner-Schlüssel zurückgesetzt. Es wird Bobs Schlüssel verwendet.");
        } else {
            System.out.println("Öffentlicher Schlüssel des Partners gesetzt: e=" + pubKey + ", n=" + modulus);
        }
    }

    /**
     * Alice verschlüsselt, Bob entschlüsselt
     */
    public static void main(String[] args) {
        // Für 255/256 Byte-Blöcke muss ein ausreichend großer Modulus (mindestens 2048 Bit) verwendet werden.
        RSAUTF8 rsa = new RSAUTF8(2047);

        String messageAliceToBob = "Möge die Macht mit dir sein!";


        System.out.println("Bobs Schlüssel: " + RSAUtils.getBobPublicKey());
        System.out.println("Bitlänge von Bobs Modulus: " + RSAUtils.getBobModulus().bitLength());
        System.out.println("Bitlänge von Alices Modulus: " + RSAUtils2047.getAliceModulus().bitLength());



        int encryptionBlockLength = getEncryptionBlockSize(RSAUtils.getBobModulus());
        int decryptionBlockLength = getDecryptionBlockSize(RSAUtils.getBobModulus());
        System.out.println("Blocklänge (Verschlüsselung) = " + encryptionBlockLength + " Byte");
        System.out.println("Blocklänge (CP437)           = " + decryptionBlockLength + " Byte");

        RSAResult result = rsa.encrypt(messageAliceToBob, true);
        System.out.println("\nVerschlüsselte Blöcke (als BigInteger):");
        for (BigInteger block : result.blocks) {
            System.out.println(block);
        }
        String cp437String = blocksToCp437String(result.blocks, RSAUtils.getBobModulus());
        System.out.println("\nGesamtes Chiffrat (Alice→Bob, CP437):\n" + cp437String);

        List<BigInteger> recoveredBlocks = cp437StringToBlocks(cp437String, RSAUtils.getBobModulus());
        RSAResult recoveredResult = new RSAResult(recoveredBlocks);
        String decrypted = rsa.decrypt(recoveredResult, false);
        System.out.println("\nBob entschlüsselt:\n" + decrypted);
    }
}
