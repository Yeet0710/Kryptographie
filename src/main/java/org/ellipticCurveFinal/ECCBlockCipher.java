package org.ellipticCurveFinal;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Utility-Klasse für ECC-basierte Block-Chiffrierung (ElGamal-Stil + Block-Verarbeitung).
 * Enthält alle benötigten Methoden zum Padding, Block-Splitting und CP437-Encoding.
 */
public class ECCBlockCipher {
    private static final Charset CP437 = Charset.forName("Cp437");

    public static class CipherResult {
        public final BigInteger Rx;
        public final BigInteger Ry;
        public final String cipherText;
        public CipherResult(BigInteger rx, BigInteger ry, String ct) {
            this.Rx = rx;
            this.Ry = ry;
            this.cipherText = ct;
        }
    }

    /**
     * Verschlüsselt einen UTF-8-String mit ECC-Block-XOR.
     */
    public static CipherResult encrypt(
            String plaintext,
            ECPoint G,
            ECPoint recipientQ,
            BigInteger p,
            BigInteger q,
            FiniteFieldEllipticCurve curve) {
        List<BigInteger> blocks = textToBigIntegerBlocks(plaintext, p);
        SecureRandom rnd = new SecureRandom();
        BigInteger k;
        do { k = new BigInteger(q.bitLength(), rnd); }
        while (k.compareTo(BigInteger.ONE) < 0 || k.compareTo(q) >= 0);
        ECPoint R = G.multiply(k, curve);
        ECPoint shared = recipientQ.multiply(k, curve);
        BigInteger key = shared.getX();
        List<BigInteger> cipherBlocks = new ArrayList<>();
        for (BigInteger m : blocks) {
            cipherBlocks.add(m.xor(key));
        }
        String ct = blocksToCp437String(cipherBlocks, p);
        return new CipherResult(R.getX(), R.getY(), ct);
    }

    /**
     * Entschlüsselt eine CP437-codierte Block-Chiffre.
     */
    public static String decrypt(
            String cipherText,
            BigInteger Rx,
            BigInteger Ry,
            BigInteger d,
            BigInteger p,
            FiniteFieldEllipticCurve curve) {
        List<BigInteger> cipherBlocks = cp437StringToBlocks(cipherText, p);
        ECPoint R = new FiniteFieldECPoint(Rx, Ry).normalize(curve);
        ECPoint shared = R.multiply(d, curve);
        BigInteger key = shared.getX();
        List<BigInteger> plainBlocks = new ArrayList<>();
        for (BigInteger c : cipherBlocks) {
            plainBlocks.add(c.xor(key));
        }
        int blockSize = getEncryptionBlockSize(p);
        byte[] bytes = bigIntegerBlocksToBytes(plainBlocks, blockSize);
        return new String(bytes, StandardCharsets.UTF_8).trim();
    }

    // --- Hilfsmethoden für Blockverarbeitung ---

    private static double logBigInteger(BigInteger val) {
        int blex = val.bitLength() - 8;
        if (blex > 0) val = val.shiftRight(blex);
        double result = Math.log(val.doubleValue());
        return result + blex * Math.log(2);
    }

    private static int calculateBlockSize(BigInteger modulus, boolean plusOne) {
        int size = (int)Math.floor(logBigInteger(modulus)/Math.log(256));
        return plusOne ? size+1 : size;
    }

    private static int getEncryptionBlockSize(BigInteger modulus) {
        return calculateBlockSize(modulus, false);
    }

    private static int getDecryptionBlockSize(BigInteger modulus) {
        return calculateBlockSize(modulus, true);
    }

    private static byte[] zeroPadData(byte[] data, int blockSize) {
        int missing = data.length % blockSize;
        if (missing == 0) return data;
        byte[] padded = new byte[data.length + (blockSize-missing)];
        System.arraycopy(data, 0, padded, 0, data.length);
        return padded;
    }

    private static List<BigInteger> textToBigIntegerBlocks(String text, BigInteger modulus) {
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        int blockSize = getEncryptionBlockSize(modulus);
        byte[] padded = zeroPadData(bytes, blockSize);
        List<BigInteger> blocks = new ArrayList<>();
        for (int i=0; i<padded.length; i+=blockSize) {
            blocks.add(new BigInteger(1, Arrays.copyOfRange(padded,i,i+blockSize)));
        }
        return blocks;
    }

    private static byte[] bigIntegerBlocksToBytes(List<BigInteger> blocks, int blockSize) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        for (BigInteger b : blocks) {
            byte[] bb = b.toByteArray();
            byte[] fixed = new byte[blockSize];
            int copyLen = Math.min(bb.length, blockSize);
            System.arraycopy(bb, bb.length-copyLen, fixed, blockSize-copyLen, copyLen);
            os.writeBytes(fixed);
        }
        return os.toByteArray();
    }

    private static String blocksToCp437String(List<BigInteger> blocks, BigInteger modulus) {
        int dsize = getDecryptionBlockSize(modulus);
        byte[] all = bigIntegerBlocksToBytes(blocks, dsize);
        return new String(all, CP437);
    }

    private static List<BigInteger> cp437StringToBlocks(String text, BigInteger modulus) {
        byte[] all = text.getBytes(CP437);
        int dsize = getDecryptionBlockSize(modulus);
        List<BigInteger> blocks = new ArrayList<>();
        for (int i=0; i<all.length; i+=dsize) {
            blocks.add(new BigInteger(1, Arrays.copyOfRange(all,i,i+dsize)));
        }
        return blocks;
    }
}
