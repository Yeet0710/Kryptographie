package org.scrum1_27;
import org.Scrum1_16.SecPrimGenerator;
import org.scrum1_3.schnelleExponentiation;

import java.math.BigInteger;
import java.security.SecureRandom;

public class ElGamalPublicKeyEncryption {
    private static final SecureRandom random = new SecureRandom();

    /**
     * Algorithmus 2.3: Generiert das Schlüsselpaar für ElGamal.
     * @param p Die sichere Primzahl
     * @param g Die Primitivwurzel
     * @return Ein Array mit {geheimer Schlüssel x, öffentlicher Schlüssel y}
     */
    public static BigInteger[] generateKeys(BigInteger p, BigInteger g) {
        long startTime = System.nanoTime();

        BigInteger x = new BigInteger(p.bitLength() - 1, random); // Privater Schlüssel x
        BigInteger y = g.modPow(x, p); // Öffentlicher Schlüssel y

        long endTime = System.nanoTime();
        System.out.println("Schlüsselerzeugung dauerte: " + (endTime - startTime) / 1e6 + " ms");

        return new BigInteger[]{x, y};
    }

    /**
     * Verschlüsselt eine Nachricht M mit ElGamal.
     * @param M Die Nachricht als Zahl
     * @param p Die Primzahl p
     * @param g Die Primitivwurzel g
     * @param y Öffentlicher Schlüssel
     * @return Das Chiffrat {a, b}
     */
    public static BigInteger[] encrypt(BigInteger M, BigInteger p, BigInteger g, BigInteger y) {
        long startTime = System.nanoTime();

        BigInteger k;
        do {
            k = new BigInteger(p.bitLength() - 1, random);
        } while (k.compareTo(BigInteger.ZERO) <= 0 || k.compareTo(p.subtract(BigInteger.TWO)) >= 0);

        BigInteger a = schnelleExponentiation.schnelleExponentiation(g, k, p);
        BigInteger b = M.multiply(schnelleExponentiation.schnelleExponentiation(y, k, p)).mod(p);

        long endTime = System.nanoTime();
        System.out.println("Verschlüsselung dauerte: " + (endTime - startTime) / 1e6 + " ms");

        return new BigInteger[]{a, b};
    }

    /**
     * Entschlüsselt das Chiffrat mit dem geheimen Schlüssel x.
     * @param a Der erste Teil des Chiffrats
     * @param b Der zweite Teil des Chiffrats
     * @param p Die Primzahl p
     * @param x Der private Schlüssel
     * @return Die entschlüsselte Nachricht
     */
    public static BigInteger decrypt(BigInteger a, BigInteger b, BigInteger p, BigInteger x) {
        long startTime = System.nanoTime();

        // Berechnung von a^x mod p
        BigInteger z = schnelleExponentiation.schnelleExponentiation(a, x, p);

        // Berechnung des modularen Inversen von z mod p
        BigInteger zInverse = z.modInverse(p);

        // Berechnung der entschlüsselten Nachricht M
        BigInteger M = b.multiply(zInverse).mod(p);

        long endTime = System.nanoTime();
        System.out.println("Entschlüsselung dauerte: " + (endTime - startTime) / 1e6 + " ms");

        return M;
    }

    public static void main(String[] args) {
        BigInteger lowerBound = new BigInteger("2000000000"); // Untere Grenze
        BigInteger upperBound = new BigInteger("3000000000"); // Obere Grenze
        int mrIterations = 20;  // Miller-Rabin-Test Iterationen

        BigInteger p = SecPrimGenerator.generateSafePrime(lowerBound, upperBound, mrIterations); // Generiert sichere Primzahl
        BigInteger g = SecPrimGenerator.findPrimitiveRoot(p); // Berechnet Primitivwurzel von p

        BigInteger[] keys = generateKeys(p, g);
        BigInteger x = keys[0]; // Privater Schlüssel
        BigInteger y = keys[1]; // Öffentlicher Schlüssel

        BigInteger M = new BigInteger("123456789"); // Beispiel-Nachricht

        /**
          Message muss aktuell kleiner sein als die untere Schranke,
          um zu garantieren, dass sie verschlüsselt werden kann.
         */

        System.out.println("Öffentlicher Schlüssel: (p=" + p + ", g=" + g + ", y=" + y + ")");
        System.out.println("Privater Schlüssel: x=" + x);
        System.out.println("Ursprüngliche Nachricht: M=" + M);

        BigInteger[] cipher = encrypt(M, p, g, y);
        System.out.println("Chiffrat: a=" + cipher[0] + ", b=" + cipher[1]);

        BigInteger decrypted = decrypt(cipher[0], cipher[1], p, x);
        System.out.println("Entschlüsselte Nachricht: M=" + decrypted);
    }
}
