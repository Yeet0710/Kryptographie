package org.ellipticCurveFinal;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

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
        int blockSize = (p.bitLength() + 7) / 8;
        int tupleSize = 2 * blockSize;
        int total = ((data.length + tupleSize - 1) / tupleSize) * tupleSize;
        byte[] bufData = new byte[total];
        System.arraycopy(data, 0, bufData, 0, data.length);

        int num = bufData.length / tupleSize;
        BigInteger[] ax = new BigInteger[num];
        BigInteger[] ay = new BigInteger[num];
        BigInteger[] b1 = new BigInteger[num];
        BigInteger[] b2 = new BigInteger[num];

        SecureRandom rnd = new SecureRandom();
        ByteArrayOutputStream outBytes = new ByteArrayOutputStream();

        for (int i = 0; i < num; i++) {
            int off = i * tupleSize;
            BigInteger m1 = new BigInteger(1, slice(bufData, off, blockSize));
            BigInteger m2 = new BigInteger(1, slice(bufData, off + blockSize, blockSize));
            // Choose random k in [1,q-1]
            BigInteger k;
            do {
                k = new BigInteger(q.bitLength(), rnd);
            } while (k.compareTo(BigInteger.ONE) < 0 || k.compareTo(q) >= 0);
            // a = k·g, c = k·y
            ECPoint a = generator.multiply(k, curve).normalize(curve);
            ECPoint c = publicKey.multiply(k, curve).normalize(curve);
            BigInteger c1 = c.getX().mod(p);
            BigInteger c2 = c.getY().mod(p);
            BigInteger bb1 = c1.multiply(m1).mod(p);
            BigInteger bb2 = c2.multiply(m2).mod(p);
            // speichere rohdaten
            ax[i] = a.getX();
            ay[i] = a.getY();
            b1[i] = bb1;
            b2[i] = bb2;
            // schreibe fix-länge
            try {
                outBytes.write(toFixedLength(a.getX(), blockSize));
                outBytes.write(toFixedLength(a.getY(), blockSize));
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
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        for (int i = 0; i < num; i++) {
            ECPoint a = new FiniteFieldECPoint(r.ax[i], r.ay[i]).normalize(curve);
            ECPoint c = a.multiply(privateKey, curve).normalize(curve);
            BigInteger c1 = c.getX().mod(p);
            BigInteger c2 = c.getY().mod(p);
            BigInteger m1 = r.b1[i].multiply(c1.modInverse(p)).mod(p);
            BigInteger m2 = r.b2[i].multiply(c2.modInverse(p)).mod(p);
            try {
                out.write(toFixedLength(m1, blockSize));
                out.write(toFixedLength(m2, blockSize));
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
        byte[] res = out.toByteArray();
        int trim = res.length;
        while (trim>0 && res[trim-1]==0) trim--;
        return new String(res, 0, trim, StandardCharsets.UTF_8);
    }

    private static byte[] slice(byte[] src, int off, int len) {
        byte[] dst = new byte[len]; System.arraycopy(src, off, dst, 0, len); return dst;
    }

    private static byte[] toFixedLength(BigInteger v, int len) {
        byte[] tmp = v.toByteArray();
        if (tmp.length==len) return tmp;
        byte[] out = new byte[len];
        int start = Math.max(0, tmp.length-len);
        int l = Math.min(tmp.length, len);
        System.arraycopy(tmp, start, out, len-l, l);
        return out;
    }

    /**
     * Demo wie RSAUTF8.main: zeigt Blöcke, Base64 und entschlüsselten Text.
     */
    public static void main(String[] args) {
        // Demo: Parameter & Schlüssel aus API
        ECCApi api = ECCApi.getInstance();
        BigInteger p = api.getP();
        BigInteger q = api.getQ();
        ECPoint g = api.getG();
        ECPoint y = api.getPublicKey();
        BigInteger x = api.getPrivateKey();
        FiniteFieldEllipticCurve curve = api.getCurve();

        String message = "Hallo ECC-ElGamal!";
        System.out.println("Klartext: " + message);

        // Verschlüsseln
        Result enc = encrypt(message, g, y, p, q, curve);
        System.out.println("\n-- Roh-Blöcke --");
        for (int i=0; i<enc.ax.length; i++) {
            System.out.printf("Block %d: a=(%s,%s), b=(%s,%s)%n",
                    i, enc.ax[i], enc.ay[i], enc.b1[i], enc.b2[i]);
        }
        System.out.println("Base64-Chiffretext: " + enc.base64);

        // Aus Base64 wieder in Blöcke
        Result parsed = base64ToResult(enc.base64, p, curve);
        // Entschlüsseln
        String dec = decrypt(parsed, x, p, curve);
        System.out.println("\nEntschlüsselter Text: " + dec);
    }
}
