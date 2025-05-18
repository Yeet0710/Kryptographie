package org.ellipticCurveFinal;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

/**
 * Vereinfachte ECC-Blockchiffre mit Null-Padding und wie bei RSAUTF8: Entfernen mit trim().
 */
public class ECCBlockCipher {
    private static final java.nio.charset.Charset CP437 = java.nio.charset.Charset.forName("Cp437");

    public static class CipherResult {
        public final BigInteger Rx, Ry;
        public final String cipherText;
        public CipherResult(BigInteger Rx, BigInteger Ry, String cipherText) {
            this.Rx = Rx;
            this.Ry = Ry;
            this.cipherText = cipherText;
        }
    }

    public static int getEncryptionBlockSize(BigInteger p) {
        return (int) Math.floor(BigInteger.valueOf(p.bitLength()).doubleValue() / Math.log(256));
    }

    public static int getDecryptionBlockSize(BigInteger p) {
        return getEncryptionBlockSize(p) + 1;
    }

    public static CipherResult encrypt(String plaintext,
                                       ECPoint G, ECPoint recipientQ,
                                       BigInteger p, BigInteger q,
                                       FiniteFieldEllipticCurve curve) {
        int blockSize = getEncryptionBlockSize(p);
        int tupleSize = 2 * blockSize;
        // Null-Padding: Auffüllen mit 0x00
        byte[] textBytes = plaintext.getBytes(StandardCharsets.UTF_8);
        int padLen = (tupleSize - (textBytes.length % tupleSize)) % tupleSize;
        byte[] padded = new byte[textBytes.length + padLen];
        System.arraycopy(textBytes, 0, padded, 0, textBytes.length);

        // Ephemeraler Schlüssel
        BigInteger k;
        do { k = new BigInteger(q.bitLength(), new java.security.SecureRandom()); }
        while (k.compareTo(BigInteger.ONE) < 0 || k.compareTo(q) >= 0);
        ECPoint R = G.multiply(k, curve);
        ECPoint S = recipientQ.multiply(k, curve);
        byte[] keyBytes = S.getX().toByteArray();

        // XOR für jeden Tupelblock
        List<BigInteger> cipherBlocks = new ArrayList<>();
        for (int i = 0; i < padded.length; i += tupleSize) {
            byte[] block = Arrays.copyOfRange(padded, i, i + tupleSize);
            for (int j = 0; j < tupleSize; j++) {
                block[j] ^= keyBytes[j % keyBytes.length];
            }
            cipherBlocks.add(new BigInteger(1, block));
        }

        // CP437-Darstellung
        int cpBlockSize = getDecryptionBlockSize(p) * 2;
        byte[] all = new byte[cipherBlocks.size() * cpBlockSize];
        for (int i = 0; i < cipherBlocks.size(); i++) {
            byte[] b = cipherBlocks.get(i).toByteArray();
            byte[] fixed = new byte[cpBlockSize];
            int len = Math.min(b.length, cpBlockSize);
            System.arraycopy(b, b.length - len, fixed, cpBlockSize - len, len);
            System.arraycopy(fixed, 0, all, i * cpBlockSize, cpBlockSize);
        }
        String ct = Base64.getEncoder().encodeToString(all);
        return new CipherResult(R.getX(), R.getY(), ct);
    }

    public static String decrypt(String cipherText,
                                 BigInteger rx, BigInteger ry,
                                 BigInteger d,
                                 BigInteger p,
                                 FiniteFieldEllipticCurve curve) {
        int blockSize = getEncryptionBlockSize(p);
        int tupleSize = 2 * blockSize;
        byte[] all = Base64.getDecoder().decode(cipherText);
        int cpBlockSize = getDecryptionBlockSize(p) * 2;

        List<BigInteger> cipherBlocks = new ArrayList<>();
        for (int i = 0; i < all.length; i += cpBlockSize) {
            byte[] slice = Arrays.copyOfRange(all, i, i + cpBlockSize);
            cipherBlocks.add(new BigInteger(1, slice));
        }

        ECPoint R = new FiniteFieldECPoint(rx, ry).normalize(curve);
        ECPoint S = R.multiply(d, curve);
        byte[] keyBytes = S.getX().toByteArray();

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        for (BigInteger cb : cipherBlocks) {
            byte[] b = cb.toByteArray();
            byte[] fixed = new byte[tupleSize];
            int len = Math.min(b.length, tupleSize);
            System.arraycopy(b, b.length - len, fixed, tupleSize - len, len);
            for (int j = 0; j < tupleSize; j++) {
                os.write((byte) (fixed[j] ^ keyBytes[j % keyBytes.length]));
            }
        }
        byte[] result = os.toByteArray();
        // Einfach wie in RSAUTF8: .trim() entfernt 0x00 am Ende
        return new String(result, StandardCharsets.UTF_8).trim();
    }
}
