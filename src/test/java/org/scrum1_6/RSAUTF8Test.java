package org.scrum1_6;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class RSAUTF8Test {

    // Verwende einen ausreichend großen Modulus (mindestens 512 Bit)
    // Hier: 2^(511) hat genau 512 Bit.
    private static final BigInteger LARGE_MODULUS = BigInteger.valueOf(2).pow(511);

    @Test
    void testZeroPadData_NoPaddingNeeded() {
        int blockSize = 4;
        byte[] data = { 0x01, 0x02, 0x03, 0x04 };
        byte[] result = RSAUTF8.zeroPadData(data, blockSize);
        Assertions.assertArrayEquals(data, result, "Kein Padding erwartet, wenn die Länge schon ein Vielfaches ist.");
    }

    @Test
    void testZeroPadData_WithPaddingNeeded() {
        int blockSize = 4;
        byte[] data = { 0x01, 0x02, 0x03 };
        byte[] result = RSAUTF8.zeroPadData(data, blockSize);
        Assertions.assertEquals(4, result.length, "Arraylänge muss auf ein Vielfaches von 4 aufgefüllt werden.");
        Assertions.assertEquals(0x00, result[3], "Das letzte Byte muss 0x00 (Padding) sein.");
    }

    @Test
    void testTextToBigIntegerBlocks() {
        String text = "Hallo RSA!";
        List<BigInteger> blocks = RSAUTF8.textToBigIntegerBlocks(text, LARGE_MODULUS);
        Assertions.assertFalse(blocks.isEmpty(), "Es sollten Blöcke erzeugt werden.");
        for (BigInteger block : blocks) {
            Assertions.assertTrue(block.compareTo(LARGE_MODULUS) < 0, "Jeder Block muss kleiner als der Modulus sein.");
        }
    }

    @Test
    void testBlocksToCp437String() {
        List<BigInteger> blocks = List.of(
                BigInteger.valueOf(1000),
                BigInteger.valueOf(2000),
                BigInteger.valueOf(3000)
        );
        String cp437String = RSAUTF8.blocksToCp437String(blocks, LARGE_MODULUS);
        Assertions.assertNotNull(cp437String, "Der CP437-String darf nicht null sein.");
        Assertions.assertFalse(cp437String.isEmpty(), "Der CP437-String darf nicht leer sein.");
    }

    @Test
    void testCp437StringToBlocks() {
        // Erzeuge einen CP437-String, der genau so lang ist wie der erwartete Block (für die Darstellung des Chiffrats)
        int decryptionBlockSize = RSAUTF8.getDecryptionBlockSize(LARGE_MODULUS);
        Charset cp437 = Charset.forName("Cp437");
        byte[] dummyBytes = new byte[decryptionBlockSize]; // mit 0x00 gefüllt
        String cp437Example = new String(dummyBytes, cp437);
        List<BigInteger> blocks = RSAUTF8.cp437StringToBlocks(cp437Example, LARGE_MODULUS);
        Assertions.assertFalse(blocks.isEmpty(), "Aus dem CP437-String müssen Blöcke extrahiert werden.");
    }

    @Test
    void testBigIntegerBlocksToBytes() {
        List<BigInteger> blocks = List.of(
                new BigInteger("010203", 16),
                new BigInteger("040506", 16)
        );
        int blockSize = 3;
        byte[] result = RSAUTF8.bigIntegerBlocksToBytes(blocks, blockSize);
        byte[] expected = { 0x01, 0x02, 0x03, 0x04, 0x05, 0x06 };
        Assertions.assertArrayEquals(expected, result, "Die zusammengefügten Bytes stimmen nicht.");
    }

    @Test
    void testCalculateBlockSize() {
        // Für LARGE_MODULUS gilt:
        // log256(2^(511)) = (511 * ln2) / (8 * ln2) = 511/8 ~ 63.875 -> floor = 63
        int blockSize = RSAUTF8.calculateBlockSize(LARGE_MODULUS, false);
        Assertions.assertEquals(63, blockSize, "Erwartete Verschlüsselungsblockgröße muss 63 Byte betragen.");
        int blockSizePlusOne = RSAUTF8.calculateBlockSize(LARGE_MODULUS, true);
        Assertions.assertEquals(64, blockSizePlusOne, "Erwartete CP437-Blockgröße muss 64 Byte betragen.");
    }

    @Test
    void testEncryptionAndDecryption() {
        // Dieser Test setzt voraus, dass RSAUtils und RSAUtils2047 gültige Schlüssel laden.
        RSAUTF8 rsa = new RSAUTF8(2048); // Beispiel: 2048 Bit
        String originalMessage = "Hallo, dies ist ein UTF‑8 Test mit RSA! äöüß";
        RSAUTF8.RSAResult encryptedResult = rsa.encrypt(originalMessage, true);
        // Simuliere den Transport als CP437-String:
        String cipherString = RSAUTF8.blocksToCp437String(encryptedResult.blocks, RSAUtils.getBobModulus());
        List<BigInteger> recoveredBlocks = RSAUTF8.cp437StringToBlocks(cipherString, RSAUtils.getBobModulus());
        RSAUTF8.RSAResult recoveredResult = new RSAUTF8.RSAResult(recoveredBlocks);
        String decryptedMessage = rsa.decrypt(recoveredResult, false);
        Assertions.assertEquals(originalMessage, decryptedMessage, "Der entschlüsselte Text muss dem Original entsprechen.");
    }

    @Test
    void testLogBigInteger() {
        // Teste logBigInteger mit einem Wert, der mindestens 512 Bit hat.
        // Verwende z. B. 2^(511): ln(2^(511)) = 511 * ln2 ~ 511 * 0.693147 = ca. 354.218
        double expected = 511 * Math.log(2);
        double actual = RSAUTF8.logBigInteger(BigInteger.valueOf(2).pow(511));
        Assertions.assertTrue(Math.abs(actual - expected) < 0.001,
                "logBigInteger muss den korrekten Wert zurückgeben. Erwartet: " + expected + ", erhalten: " + actual);
    }
}
