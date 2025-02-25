package org.Scrum1_16;
import org.scrum1_1.PrimGenerator;
import org.scrum1_2.PrimTester;

import java.math.BigInteger;
import java.security.SecureRandom;

public class SecPrimGenerator {
    private static final SecureRandom random = new SecureRandom();

    /**
     * Generiert eine zufällige Primzahl im Bereich [a, b] unter Verwendung des Miller-Rabin-Tests.
     * @param bitLength Länge der Primzahl in Bit
     * @param mrIterations Anzahl der Miller-Rabin-Iterationen
     * @return Eine zufällige Primzahl im gegebenen Bereich
     */
    public static BigInteger generateSafePrime(BigInteger bitLength, int mrIterations) {
        BigInteger lowerBound = BigInteger.ONE.shiftLeft(bitLength.subtract(BigInteger.ONE).intValue());  // 2^(bitLength - 1)
        BigInteger upperBound = lowerBound.shiftLeft(1).subtract(BigInteger.ONE);  // 2^bitLength - 1

        while (true) {
            BigInteger q = PrimGenerator.generateRandomPrime(lowerBound, upperBound, mrIterations); // Generiert Primzahl q
            BigInteger p = q.multiply(BigInteger.TWO).add(BigInteger.ONE); // Erzeugt p = 2q + 1


            if (PrimTester.istPrimzahl(p, mrIterations)) { // Miller-Rabin-Test mit 20 Iterationen für p
                return p;
            }
        }
    }

    // Findet eine Primitivwurzel g in Z_p
    public static BigInteger findPrimitiveRoot(BigInteger p) {
        BigInteger q = p.subtract(BigInteger.ONE).divide(BigInteger.TWO); // q = (p-1)/2

        while (true) {
            BigInteger g = new BigInteger(p.bitLength(), random).mod(p);
            if (g.compareTo(BigInteger.TWO) < 0 || g.equals(p.subtract(BigInteger.ONE))) {
                continue;
            }

            // Prüfe, ob g eine gültige Primitivwurzel ist
            if (!g.modPow(q, p).equals(BigInteger.ONE) &&  // g^q ≠ 1 mod p
                    !g.modPow(BigInteger.TWO, p).equals(BigInteger.ONE)) {  // g^2 ≠ 1 mod p
                return g;
            }
        }
    }

    public static void main(String[] args) {

        BigInteger bitLength = BigInteger.valueOf(256);  // Bit-Länge der Primzahl
        int mrIterations = 20;  // Anzahl der Iterationen für den Miller-Rabin-Test

        BigInteger p = generateSafePrime(bitLength, mrIterations);
        BigInteger g = findPrimitiveRoot(p);

        System.out.println("Sichere Primzahl p: " + p);
        System.out.println("Primitivwurzel g: " + g);
    }
}