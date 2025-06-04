package org.ellipticCurveFinal;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * ECC-ElGamal Blockchiffre mit API-Design analog zu RSAUTF8.
 */
public class ECCElgamalBlockCipher {

    /**
     * Ergebnisstruktur mit je vier Arrays für a.x, a.y, b1, b2 und dem Base64-String.
     */
    public static class Result {
        public final BigInteger[] ax;
        public final BigInteger[] ay;
        public final BigInteger[] b1;
        public final BigInteger[] b2;
        public final String base64;

        public Result(BigInteger[] ax, BigInteger[] ay,
                      BigInteger[] b1, BigInteger[] b2,
                      String base64) {
            this.ax = ax;
            this.ay = ay;
            this.b1 = b1;
            this.b2 = b2;
            this.base64 = base64;
        }
    }

    /**
     * Zerlegt Klartext in Blocks, führt Verschlüsselung je Block aus,
     * liefert Result mit rohen BigInteger-Arrays und Base64-String.
     */
    public static Result encrypt(String plaintext,
                                 ECPoint generator,
                                 ECPoint publicKey,
                                 BigInteger p,
                                 BigInteger q,
                                 FiniteFieldEllipticCurve curve) {
        byte[] data = plaintext.getBytes(StandardCharsets.UTF_8);
        int blockSize = (p.bitLength() + 7) / 8;    // Anzahl Bytes, um Feld­element < p darzustellen
        int chunkSize = blockSize - 1;              // Jeder Klartext-Chunk muss < 256^(chunkSize) ≤ p sein
        // → Pro Tuple verschlüsseln wir 2*chunkSize Bytes Klartext

        // 1) Zerlege data in chunkSize-Byte-Blöcke (pad mit 0x00, falls nötig)
        List<byte[]> chunkList = new ArrayList<>();
        int idx = 0;
        while (idx < data.length) {
            int remaining = data.length - idx;
            if (remaining >= chunkSize) {
                chunkList.add(Arrays.copyOfRange(data, idx, idx + chunkSize));
                idx += chunkSize;
            } else {
                byte[] last = new byte[chunkSize];
                System.arraycopy(data, idx, last, 0, remaining);
                // die restlichen Bytes in 'last' bleiben 0x00
                chunkList.add(last);
                idx += remaining;
            }
        }
        // 2) Wenn Anzahl Chunks ungerade, füge ein reines Null-Chunk (Padding)
        if (chunkList.size() % 2 != 0) {
            chunkList.add(new byte[chunkSize]);
        }

        int numTuples = chunkList.size() / 2;
        BigInteger[] ax = new BigInteger[numTuples];
        BigInteger[] ay = new BigInteger[numTuples];
        BigInteger[] b1 = new BigInteger[numTuples];
        BigInteger[] b2 = new BigInteger[numTuples];

        SecureRandom rnd = new SecureRandom();
        ByteArrayOutputStream outBytes = new ByteArrayOutputStream();

        // 3) Für jedes Paar (m1Bytes, m2Bytes) in chunkList
        for (int t = 0; t < numTuples; t++) {
            byte[] part1 = chunkList.get(2 * t);       // Länge = chunkSize
            byte[] part2 = chunkList.get(2 * t + 1);   // Länge = chunkSize

            BigInteger m1 = new BigInteger(1, part1);   // garantiert < 256^(chunkSize) ≤ p
            BigInteger m2 = new BigInteger(1, part2);

            // 4) Zufälliges k in [1 .. q-1]
            BigInteger k;
            do {
                k = new BigInteger(q.bitLength(), rnd);
            } while (k.compareTo(BigInteger.ONE) < 0 || k.compareTo(q) >= 0);

            // 5) a = k·G, c = k·Y
            ECPoint aPoint = generator.multiply(k, curve).normalize(curve);
            ECPoint cPoint = publicKey.multiply(k, curve).normalize(curve);

            // 6) bb1 = cPoint.x * m1 mod p,  bb2 = cPoint.y * m2 mod p
            BigInteger c1 = cPoint.getX().mod(p);
            BigInteger c2 = cPoint.getY().mod(p);
            BigInteger bb1 = c1.multiply(m1).mod(p);
            BigInteger bb2 = c2.multiply(m2).mod(p);

            // 7) Speichere Rohdaten
            ax[t] = aPoint.getX();
            ay[t] = aPoint.getY();
            b1[t] = bb1;
            b2[t] = bb2;

            // 8) Serialisiere A.x, A.y, bb1, bb2 jeweils in blockSize Bytes
            try {
                outBytes.write(toFixedLength(aPoint.getX(), blockSize));
                outBytes.write(toFixedLength(aPoint.getY(), blockSize));
                outBytes.write(toFixedLength(bb1, blockSize));
                outBytes.write(toFixedLength(bb2, blockSize));
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }

        String b64 = Base64.getEncoder().encodeToString(outBytes.toByteArray());

        return new Result(ax, ay, b1, b2, b64);
    }

    /**
     * Wandelt Base64-String zurück in rohe Block-Arrays.
     */
    public static Result base64ToResult(String base64,
                                        BigInteger p,
                                        FiniteFieldEllipticCurve curve) {
        byte[] cipher = Base64.getDecoder().decode(base64);
        int blockSize = (p.bitLength() + 7) / 8;
        int tupleBytes = 4 * blockSize;
        int num = cipher.length / tupleBytes;
        BigInteger[] ax = new BigInteger[num];
        BigInteger[] ay = new BigInteger[num];
        BigInteger[] b1 = new BigInteger[num];
        BigInteger[] b2 = new BigInteger[num];
        for (int i = 0; i < num; i++) {
            int off = i * tupleBytes;
            ax[i] = new BigInteger(1, slice(cipher, off, blockSize));
            ay[i] = new BigInteger(1, slice(cipher, off + blockSize, blockSize));
            b1[i] = new BigInteger(1, slice(cipher, off + 2*blockSize, blockSize));
            b2[i] = new BigInteger(1, slice(cipher, off + 3*blockSize, blockSize));
        }
        return new Result(ax, ay, b1, b2, base64);
    }

    /**
     * Entschlüsselt ein Result zurück zum Klartext.
     */
    public static String decrypt(Result r,
                                 BigInteger privateKey,
                                 BigInteger p,
                                 FiniteFieldEllipticCurve curve) {
        int num = r.ax.length;
        int blockSize = (p.bitLength() + 7) / 8;
        int chunkSize = blockSize - 1;            // Jeder Klartext-Chunk war chunkSize groß
        int tupleBytes = 4 * blockSize;           // A.x||A.y||b1||b2

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        for (int i = 0; i < num; i++) {
            // 1) Rekonstruiere Punkt A
            ECPoint a = new FiniteFieldECPoint(r.ax[i], r.ay[i]).normalize(curve);
            // 2) c = x·A
            ECPoint c = a.multiply(privateKey, curve).normalize(curve);
            BigInteger c1 = c.getX().mod(p);
            BigInteger c2 = c.getY().mod(p);
            // 3) m1 = b1 * c1^{-1} mod p,  m2 = b2 * c2^{-1} mod p
            BigInteger m1 = r.b1[i].multiply(c1.modInverse(p)).mod(p);
            BigInteger m2 = r.b2[i].multiply(c2.modInverse(p)).mod(p);

            // 4) Schreibe m1 und m2 als jeweils chunkSize Bytes
            byte[] tmp1 = m1.toByteArray();
            byte[] fixed1 = new byte[chunkSize];
            int off1 = chunkSize - tmp1.length;
            if (off1 < 0) {
                // tmp1.length == chunkSize+1 → führende 0x00 entfernen
                System.arraycopy(tmp1, 1, fixed1, 0, chunkSize);
            } else {
                // tmp1.length ≤ chunkSize → linkspad mit off1 Nullen
                for (int k = 0; k < off1; k++) {
                    fixed1[k] = 0;
                }
                System.arraycopy(tmp1, 0, fixed1, off1, tmp1.length);
            }
            out.write(fixed1, 0, chunkSize);

            byte[] tmp2 = m2.toByteArray();
            byte[] fixed2 = new byte[chunkSize];
            int off2 = chunkSize - tmp2.length;
            if (off2 < 0) {
                System.arraycopy(tmp2, 1, fixed2, 0, chunkSize);
            } else {
                for (int k = 0; k < off2; k++) {
                    fixed2[k] = 0;
                }
                System.arraycopy(tmp2, 0, fixed2, off2, tmp2.length);
            }
            out.write(fixed2, 0, chunkSize);
        }

        // 5) Ganz am Ende trimme alle Null-Bytes, die nur Padding waren
        byte[] res = out.toByteArray();
        int trim = res.length;
        while (trim > 0 && res[trim - 1] == 0x00) {
            trim--;
        }

        return new String(res, 0, trim, StandardCharsets.UTF_8);
    }

    private static byte[] slice(byte[] src, int off, int len) {
        byte[] dst = new byte[len]; System.arraycopy(src, off, dst, 0, len); return dst;
    }

    private static byte[] toFixedLength(BigInteger v, int len) {
        byte[] tmp = v.toByteArray(); // Länge kann zwischen 1 und len+1 liegen
        byte[] fixed = new byte[len];
        int off = len - tmp.length;
        if (off < 0) {
            // tmp.length == len+1 → führende 0x00 wegwerfen
            System.arraycopy(tmp, 1, fixed, 0, len);
        } else {
            // tmp.length ≤ len → linkspad mit off Nullen
            for (int i = 0; i < off; i++) {
                fixed[i] = 0;
            }
            System.arraycopy(tmp, 0, fixed, off, tmp.length);
        }
        return fixed;
    }

}
