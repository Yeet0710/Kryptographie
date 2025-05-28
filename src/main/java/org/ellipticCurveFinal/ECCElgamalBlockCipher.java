package org.ellipticCurveFinal;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

public class ECCElgamalBlockCipher {

    /**
     * Verschlüsselt einen Text per ElGamal
     * @param plaintext
     * @param generator
     * @param publickey
     * @param p
     * @param q
     * @param curve
     * @return
     */
    public static String encrypt(String plaintext, 
                                 ECPoint generator,     // g
                                 ECPoint publickey,     // y = x * g
                                 BigInteger p, 
                                 BigInteger q,
                                 FiniteFieldEllipticCurve curve) {
        
        System.out.println("ECC-ElGamal Verschlüsselung");
        System.out.println("Klartext: " + plaintext);
        
        // 1. Text zu UTF-8 Bytes speichern
        byte[] textBytes = plaintext.getBytes(StandardCharsets.UTF_8);

        // 2. Blockgröße berechnen
        int blocksize = calculateBlockSize(p);

        // 3. Tupel-Padding
        int tupleSize = 2 * blocksize;
        int totalTuples = (textBytes.length + tupleSize - 1) / tupleSize;
        int paddedLength = totalTuples * tupleSize;

        // Kopiert die Bytes in das neue byteArray
        byte[] paddedBytes = new byte[paddedLength];
        System.arraycopy(textBytes, 0, paddedBytes, 0, textBytes.length);

        // 4. Verschlüsselung für jedes Tupel
        SecureRandom secureRandom = new SecureRandom();
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        for (int i = 0; i < paddedBytes.length; i += tupleSize) {

            // Zuerst werden die Tupel extrahiert
            byte[] block1 = Arrays.copyOfRange(paddedBytes, i, i + blocksize);
            byte[] block2 = Arrays.copyOfRange(paddedBytes, i + blocksize, i + tupleSize);

            // Konvertierung zu BigInteger
            BigInteger m1 = new BigInteger(1, block1);
            BigInteger m2 = new BigInteger(1, block2);

            // Tupel M = (m1, m2) als EC-Punkt darstellen
            ECPoint M = new FiniteFieldECPoint(m1, m2).normalize(curve);

            // Algorithmus 3.3
            // Schritt 1: k e [1, q-1] wählen
            BigInteger k;
            do {
                k = new BigInteger(q.bitLength(), secureRandom);
            } while (k.compareTo(BigInteger.ONE) < 0 || k.compareTo(q) >= 0);

            //Schritt 2: Chiffrat berechnen
            ECPoint c1 = generator.multiply(k, curve);      // c1 = k * g
            ECPoint c2 = M.add(publickey.multiply(k, curve), curve);                  // c2 = M + k * y

            try {
                out.write(c1.getX().toByteArray());
                out.write(c1.getY().toByteArray());
                out.write(c2.getX().toByteArray());
                out.write(c2.getY().toByteArray());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }

        byte[] cipherBytes = out.toByteArray();
        return Base64.getEncoder().encodeToString(cipherBytes);

    }

    /**
     * Entschlüsselt per El-Gamal
     * @param text
     * @param privateKey
     * @param p
     * @param curve
     * @return
     */
    public static String decrypt(String text,
                                 BigInteger privateKey,
                                 BigInteger p,
                                 FiniteFieldEllipticCurve curve) {

        System.out.println("ECC-ElGamal Entschlüsselung");

        int blocksize = calculateBlockSize(p);
        int tupleBytes = 2 * 2 * blocksize; // c1(x,y) + c2(x,y)

        byte[] cipherBytes = Base64.getDecoder().decode(text);
        ByteArrayInputStream in = new ByteArrayInputStream(cipherBytes);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        byte[] buf = new byte[tupleBytes];
        while (in.available() >= tupleBytes) {

            in.read(buf, 0, tupleBytes);

            // c1
            BigInteger c1x = new BigInteger(1, slice(buf, 0, blocksize));
            BigInteger c1y = new BigInteger(1, slice(buf, blocksize, blocksize));
            ECPoint c1 = new FiniteFieldECPoint(c1x, c1y).normalize(curve);

            // c1
            BigInteger c2x = new BigInteger(1, slice(buf, 2 * blocksize, blocksize));
            BigInteger c2y = new BigInteger(1, slice(buf, 3 * blocksize, blocksize));
            ECPoint c2 = new FiniteFieldECPoint(c2x, c2y).normalize(curve);

            // M = c2 - x * c1
            ECPoint xc1 = c1.multiply(privateKey, curve);
            BigInteger invY = p.subtract(xc1.getY()).mod(p);
            ECPoint negXc1 = new FiniteFieldECPoint(xc1.getX(), invY).normalize(curve);
            ECPoint M = c2.add(negXc1, curve).normalize(curve);

            // m1, m2 aus Punkt-Koordinaten


        }

        //

    }

    /**
     * Berechnet die Blockgröße für die Primzahl p
     * Dadurch wird sichergestellt, dass m1, m2 < p
     * @param p
     * @return
     */
    private static int calculateBlockSize(BigInteger p) {

        int bitLength = p.bitLength();
        int maxBytes = (bitLength - 16) / 8;    // Hierdurch erhalten wir 2 Byte Sicherheit
        return Math.max(1, maxBytes);           // Hierdurch wird sichergestellt, dass mindestens 1 Byte zurückgegeben wird

    }

    private static byte[] slice(byte[] src, int offset, int len) {

        byte[] dst = new byte[len];
        System.arraycopy(src, offset, dst, 0, len);
        return dst;

    }


}
