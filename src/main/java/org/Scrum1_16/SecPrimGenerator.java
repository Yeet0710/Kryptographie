package org.Scrum1_16;
import java.math.BigInteger;
import java.security.SecureRandom;

public class SecPrimGenerator {
    private static final SecureRandom random = new SecureRandom();

    // Generiert eine sichere Primzahl p = 2q + 1
    public static BigInteger generateSafePrime(int bitLength) {
        while (true) {
            BigInteger q = BigInteger.probablePrime(bitLength - 1, random);
            BigInteger p = q.multiply(BigInteger.TWO).add(BigInteger.ONE);

            if (p.isProbablePrime(20)) { // Miller-Rabin-Test mit 20 Iterationen
                return p;
            }
        }
    }

    // Findet eine Primitivwurzel g in Z_p
    public static BigInteger findPrimitiveRoot(BigInteger p) {
        BigInteger q = p.subtract(BigInteger.ONE).divide(BigInteger.TWO);

        while (true) {
            BigInteger g = new BigInteger(p.bitLength(), random).mod(p);
            if (g.compareTo(BigInteger.TWO) < 0 || g.equals(p.subtract(BigInteger.ONE))) {
                continue;
            }

            // Überprüft, ob g eine Primitivwurzel ist
            if (!g.modPow(BigInteger.TWO, p).equals(BigInteger.ONE) &&
                    !g.modPow(q, p).equals(BigInteger.ONE)) {
                return g;
            }
        }
    }

    public static void main(String[] args) {
        int bitLength = 512; // Länge der Primzahl in Bits
        BigInteger p = generateSafePrime(bitLength);
        BigInteger g = findPrimitiveRoot(p);

        System.out.println("Sichere Primzahl p: " + p);
        System.out.println("Primitivwurzel g: " + g);
    }
}