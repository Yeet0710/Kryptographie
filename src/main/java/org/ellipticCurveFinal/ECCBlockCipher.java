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
 * ECCBlockCipher gemäß DV: verarbeitet Block-Tupel (m1, m2) als Einheit.
 */
public class ECCBlockCipher {
    private static final Charset CP437 = Charset.forName("Cp437");

    public static class CipherResult {
        public final BigInteger Rx;
        public final BigInteger Ry;
        public final String cipherText;
        public CipherResult(BigInteger rx, BigInteger ry, String ct) {
            this.Rx = rx; this.Ry = ry; this.cipherText = ct;
        }
    }

    /** Verschlüsselt mit Block-Tupel-Verfahren. */
    public static CipherResult encrypt(
            String plaintext,
            ECPoint G,
            ECPoint recipientQ,
            BigInteger p,
            BigInteger q,
            FiniteFieldEllipticCurve curve) {
        int blockSize = getEncryptionBlockSize(p);
        // 1) Byte-Padding und Tuple-Erstellung
        byte[] textBytes = plaintext.getBytes(StandardCharsets.UTF_8);
        int missing = textBytes.length % blockSize;
        if (missing != 0) {
            textBytes = Arrays.copyOf(textBytes, textBytes.length + (blockSize - missing));
        }
        List<byte[]> tuples = new ArrayList<>();
        for (int i = 0; i < textBytes.length; i += 2*blockSize) {
            byte[] t = new byte[2*blockSize];
            int len = Math.min(2*blockSize, textBytes.length - i);
            System.arraycopy(textBytes, i, t, 0, len);
            tuples.add(t);
        }
        // 2) ECC-Ephemeral
        SecureRandom rnd = new SecureRandom();
        BigInteger k;
        do { k = new BigInteger(q.bitLength(), rnd); }
        while (k.compareTo(BigInteger.ONE) < 0 || k.compareTo(q) >= 0);
        ECPoint R = G.multiply(k, curve);
        ECPoint shared = recipientQ.multiply(k, curve);
        byte[] key = shared.getX().toByteArray();
        // 3) XOR auf Tuple-Ebene
        List<BigInteger> cipherBlocks = new ArrayList<>();
        for (byte[] t : tuples) {
            byte[] ct = new byte[t.length];
            for (int j=0; j<t.length; j++) ct[j] = (byte)(t[j] ^ key[j % key.length]);
            cipherBlocks.add(new BigInteger(1, ct));
        }
        // 4) CP437-Kodierung
        int cpBlockSize = getDecryptionBlockSize(p) * 2;
        byte[] all = bigIntegerBlocksToBytes(cipherBlocks, cpBlockSize);
        String cpText = new String(all, CP437);
        return new CipherResult(R.getX(), R.getY(), cpText);
    }

    /** Entschlüsselt Block-Tupel-Verfahren. */
    public static String decrypt(
            String cipherText,
            BigInteger Rx,
            BigInteger Ry,
            BigInteger d,
            BigInteger p,
            FiniteFieldEllipticCurve curve) {
        int blockSize = getEncryptionBlockSize(p);
        int cpBlockSize = getDecryptionBlockSize(p) * 2;
        // 1) CP437 -> BigInteger-Tupel
        byte[] allBytes = cipherText.getBytes(CP437);
        List<BigInteger> cipherBlocks = new ArrayList<>();
        for (int i=0; i<allBytes.length; i+=cpBlockSize) {
            byte[] chunk = Arrays.copyOfRange(allBytes, i, i+cpBlockSize);
            cipherBlocks.add(new BigInteger(1, chunk));
        }
        // 2) gemeinsames Geheimnis
        ECPoint R = new FiniteFieldECPoint(Rx, Ry).normalize(curve);
        ECPoint shared = R.multiply(d, curve);
        byte[] key = shared.getX().toByteArray();
        // 3) XOR und Reassemblierung
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        for (BigInteger cb : cipherBlocks) {
            byte[] ct = toFixedLength(cb.toByteArray(), 2*blockSize);
            byte[] pt = new byte[ct.length];
            for (int j=0; j<ct.length; j++) pt[j] = (byte)(ct[j] ^ key[j % key.length]);
            try { os.write(pt); } catch (IOException ignored) {}
        }
        byte[] result = os.toByteArray();
        // 4) abschneiden von Padding
        int trimLen = result.length;
        while (trimLen>0 && result[trimLen-1]==0) trimLen--;
        return new String(result, 0, trimLen, StandardCharsets.UTF_8);
    }

    // --- Hilfsmethoden ---
    private static byte[] toFixedLength(byte[] src, int len) {
        if (src.length==len) return src;
        byte[] dst = new byte[len];
        int copy = Math.min(src.length, len);
        System.arraycopy(src, src.length-copy, dst, len-copy, copy);
        return dst;
    }

    private static double logBigInteger(BigInteger val) {
        int blex = val.bitLength() - 8;
        if (blex>0) val = val.shiftRight(blex);
        return Math.log(val.doubleValue()) + blex*Math.log(2);
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

    private static byte[] bigIntegerBlocksToBytes(
            List<BigInteger> blocks, int blockSize) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        for (BigInteger b : blocks) os.writeBytes(toFixedLength(b.toByteArray(), blockSize));
        return os.toByteArray();
    }
}
