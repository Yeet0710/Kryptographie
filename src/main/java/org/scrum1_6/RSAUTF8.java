package org.scrum1_6;

import org.scrum1_3.schnelleExponentiation;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

public class RSAUTF8 {

    private BigInteger friendPubKey;
    private BigInteger friendModulus;

    public static class RSAResult {
        public final List<BigInteger> blocks;
        public RSAResult(List<BigInteger> blocks) {
            this.blocks = blocks;
        }
    }

    public RSAUTF8(int bitLength) {
        try {
            RSAUtils.loadKeysFromFiles();
            RSAUtils2047.loadKeysFromFiles();
        } catch (Exception e) {
            System.out.println("Fehler beim Laden der Schlüssel: " + e.getMessage());
        }
    }

    /**
     * Schätzt ln(val) mittels Bit-Shifts, um Overflow zu vermeiden.
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
     * Berechnet Blockgröße in Bytes: ⌊log256(modulus)⌋ (+ 1 falls plusOne=true).
     */
    public static int calculateBlockSize(BigInteger modulus, boolean plusOne) {
        int blockSize = (int) Math.floor(logBigInteger(modulus) / Math.log(256));
        if (plusOne) {
            blockSize++;
        }
        System.out.println("DEBUG: modulus = " + modulus);
        System.out.println("DEBUG: Berechnete Blockgröße (plusOne=" + plusOne + "): " + blockSize + " Bytes");
        return blockSize;
    }

    public static int getEncryptionBlockSize(BigInteger modulus) {
        return calculateBlockSize(modulus, false);
    }

    public static int getDecryptionBlockSize(BigInteger modulus) {
        return calculateBlockSize(modulus, true);
    }

    public static byte[] zeroPadData(byte[] data, int blockSize) {
        int missing = data.length % blockSize;
        if (missing == 0) {
            return data;
        }
        int padLength = blockSize - missing;
        byte[] padded = new byte[data.length + padLength];
        System.arraycopy(data, 0, padded, 0, data.length);
        // Java-Arrays sind per Default mit 0 initialisiert.
        return padded;
    }

    public static List<BigInteger> textToBigIntegerBlocks(final String text, final BigInteger modulus) {
        byte[] textBytes = text.getBytes(StandardCharsets.UTF_8);
        System.out.println("-----------------------");
        System.out.println("Anzahl der Zeichen: " + text.length());
        System.out.println("DEBUG: Ursprünglicher Text: " + text);
        System.out.println("DEBUG: Länge des ursprünglichen Bytearrays: " + textBytes.length);
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

    public static byte[] bigIntegerBlocksToBytes(List<BigInteger> blocks, int blockLength) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        System.out.println("DEBUG: Anzahl der Blöcke zum Zusammenfügen: " + blocks.size());
        try {
            for (int i = 0; i < blocks.size(); i++) {
                BigInteger block = blocks.get(i);
                byte[] blockBytes = block.toByteArray();
                System.out.println("DEBUG: Block " + i + " original Länge: " + blockBytes.length);
                byte[] fixedBlock = new byte[blockLength];
                if (blockBytes.length > blockLength) {
                    System.arraycopy(blockBytes, blockBytes.length - blockLength, fixedBlock, 0, blockLength);
                    System.out.println("DEBUG: Block " + i + " wurde gekürzt auf " + blockLength + " Bytes");
                } else if (blockBytes.length < blockLength) {
                    System.arraycopy(blockBytes, 0, fixedBlock, blockLength - blockBytes.length, blockBytes.length);
                    System.out.println("DEBUG: Block " + i + " wurde aufgefüllt auf " + blockLength + " Bytes");
                } else {
                    fixedBlock = blockBytes;
                    System.out.println("DEBUG: Block " + i + " hat bereits die korrekte Länge: " + blockLength + " Bytes");
                }
                outputStream.write(fixedBlock);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        byte[] result = outputStream.toByteArray();
        System.out.println("DEBUG: Gesamtzahl der Bytes nach Zusammenfügen: " + result.length);
        return result;
    }

    public static String blocksToBase64String(List<BigInteger> blocks, BigInteger modulus) {
        int modBlockSize = getDecryptionBlockSize(modulus);
        System.out.println("DEBUG: Berechnete Blockgröße für Base64-Darstellung: " + modBlockSize + " Bytes");
        byte[] allBytes = bigIntegerBlocksToBytes(blocks, modBlockSize);
        System.out.println("DEBUG: Anzahl der Bytes im zusammengesetzten Base64-Bytearray: " + allBytes.length);
        String base64String = Base64.getEncoder().encodeToString(allBytes);
        System.out.println("DEBUG: Base64-codiertes Chiffrat: " + base64String);
        return base64String;
    }

    public static List<BigInteger> base64StringToBlocks(String base64String, BigInteger modulus) {
        byte[] allBytes = Base64.getDecoder().decode(base64String);
        int modBlockSize = getDecryptionBlockSize(modulus);
        System.out.println("-----------------------");
        System.out.println("DEBUG: Gesamtlänge des eingelesenen Base64-Bytearrays: " + allBytes.length);
        System.out.println("DEBUG: Erwartete Blockgröße: " + modBlockSize + " Bytes");
        System.out.println("-----------------------");

        List<BigInteger> blocks = new ArrayList<>();
        for (int i = 0; i < allBytes.length; i += modBlockSize) {
            byte[] blockBytes = Arrays.copyOfRange(allBytes, i, i + modBlockSize);
            blocks.add(new BigInteger(1, blockBytes));
        }
        System.out.println("DEBUG: Anzahl der extrahierten Blöcke: " + blocks.size());
        return blocks;
    }

    /**
     * Verschlüsselt eine UTF-8-Nachricht.
     * Gibt eine RSAResult mit verschlüsselten Blöcken zurück und misst die Zeit.
     */
    public RSAResult encrypt(String message, boolean fromAlice) {
        BigInteger pubKey, modulus;
        if (fromAlice) {
            pubKey = RSAUtils.getBobPublicKey();
            modulus = RSAUtils.getBobModulus();
        } else {
            pubKey = RSAUtils.getAlicePublicKey();
            modulus = RSAUtils.getAliceModulus();
        }
        List<BigInteger> blocks = textToBigIntegerBlocks(message, modulus);
        List<BigInteger> encryptedBlocks = new ArrayList<>();
        long startTime = System.currentTimeMillis();
        for (BigInteger block : blocks) {
            BigInteger cipherBlock = schnelleExponentiation.schnelleExponentiation(block, pubKey, modulus);
            encryptedBlocks.add(cipherBlock);
        }
        long encryptionTime = System.currentTimeMillis() - startTime;
        System.out.println("Verschlüsselungszeit: " + encryptionTime + " ms");
        return new RSAResult(encryptedBlocks);
    }

    public String decrypt(RSAResult result, boolean toAlice) {
        BigInteger privKey, modulus;
        if (toAlice) {
            privKey = RSAUtils.getAlicePrivateKey();
            modulus = RSAUtils.getAliceModulus();
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
        System.out.println("verwendeter modulus: " + modulus);
        long decryptionTime = System.currentTimeMillis() - startTime;
        System.out.println("Entschlüsselungszeit: " + decryptionTime + " ms");

        int blockSize = getEncryptionBlockSize(modulus);
        byte[] allBytes = bigIntegerBlocksToBytes(decryptedBlocks, blockSize);
        return new String(allBytes, StandardCharsets.UTF_8).trim();
    }

    public void setPublicKey(BigInteger modulus, BigInteger pubKey) {
        this.friendPubKey = pubKey;
        this.friendModulus = modulus;
        if (pubKey == null || modulus == null) {
            System.out.println("Partner-Schlüssel zurückgesetzt. Es wird Bobs Schlüssel verwendet.");
        } else {
            System.out.println("Öffentlicher Schlüssel des Partners gesetzt: n=" + modulus + ", e=" + pubKey);
        }
    }

    public static void main(String[] args) {
        RSAUTF8 rsa = new RSAUTF8(1024);

        String messageAliceToBob = "Möge die Macht mit dir sein!";
        System.out.println("Klartext (UTF-8): " + messageAliceToBob);

        System.out.println("Bobs Schlüssel: " + RSAUtils.getBobPublicKey());
        System.out.println("Bitlänge von Bobs Modulus: " + RSAUtils.getBobModulus().bitLength());
        System.out.println("Bitlänge von Alices Modulus: " + RSAUtils2047.getAliceModulus().bitLength());

        int encryptionBlockLength = getEncryptionBlockSize(RSAUtils.getBobModulus());
        int decryptionBlockLength = getDecryptionBlockSize(RSAUtils.getBobModulus());
        System.out.println("Blocklänge (Verschlüsselung) = " + encryptionBlockLength + " Byte");
        System.out.println("Blocklänge (Base64)           = " + decryptionBlockLength + " Byte");

        RSAResult result = rsa.encrypt(messageAliceToBob, true);

        System.out.println("\nVerschlüsselte Blöcke (als BigInteger):");
        for (BigInteger block : result.blocks) {
            System.out.println(block);
        }

        String base64Cipher = blocksToBase64String(result.blocks, RSAUtils.getBobModulus());
        System.out.println("\nGesamtes Chiffrat (Alice→Bob, Base64):\n" + base64Cipher);

        List<BigInteger> recoveredBlocks = base64StringToBlocks(base64Cipher, RSAUtils.getBobModulus());
        RSAResult recoveredResult = new RSAResult(recoveredBlocks);
        String decrypted = rsa.decrypt(recoveredResult, false);
        System.out.println("\nBob entschlüsselt:\n" + decrypted);
    }
}
