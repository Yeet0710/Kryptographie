package org.Scrum1_16;
import org.scrum1_1.PrimGenerator;
import org.scrum1_2.PrimTester;

import java.math.BigInteger;
import java.security.SecureRandom;

public class SecPrimGenerator {
    private static final SecureRandom random = new SecureRandom();

    /**
     * Generiert eine zufällige Primzahl im Bereich [a, b] unter Verwendung des Miller-Rabin-Tests.
     * @param a Untere Schranke (inklusive)
     * @param b Obere Schranke (inklusive)
     * @param mrIterations Anzahl der Miller-Rabin-Iterationen
     * @return Eine zufällige Primzahl im gegebenen Bereich
     */
    public static BigInteger generateSafePrime(BigInteger a, BigInteger b, int mrIterations) {
        while (true) {
            BigInteger q = PrimGenerator.generateRandomPrime(a, b, mrIterations); // Generiert Primzahl
            BigInteger p = q.multiply(BigInteger.TWO).add(BigInteger.ONE); // erzeugt p = 2q + 1

            if (PrimTester.istPrimzahl(p, mrIterations)) { // Miller-Rabin-Test mit 20 Iterationen für p
                return p;
            }
        }
    }

    // Findet eine Primitivwurzel g in Z_p
    public static BigInteger findPrimitiveRoot(BigInteger p) {
        BigInteger q = p.subtract(BigInteger.ONE).divide(BigInteger.TWO); // q = (p-1)/2

        // Kontrolle das g ein Element aus Z_p^*\{1,p−1} ist
        while (true) {
            BigInteger g = new BigInteger(p.bitLength(), random).mod(p);
            if (g.compareTo(BigInteger.TWO) < 0 || g.equals(p.subtract(BigInteger.ONE))) {
                continue;
            }

            // Überprüft, ob g eine Primitivwurzel ist
            if (!g.modPow(BigInteger.TWO, p).equals(BigInteger.ONE) && // g^2 !≡ 1 (mod p)
                    !g.modPow(q, p).equals(BigInteger.ONE)) { // g^q !≡ 1 (mod p)
                return g;
            }
        }
    }

    public static void main(String[] args) {

        BigInteger lowerBound = new BigInteger("200000"); // Untere Grenze der Range
        BigInteger upperBound = new BigInteger("300000"); // Obere Grenze der Range
        int mrIterations = 20;  // Anzahl der Iterationen für den Miller-Rabin-Test

        BigInteger p = generateSafePrime(lowerBound, upperBound, mrIterations);
        BigInteger g = findPrimitiveRoot(p);

        System.out.println("Sichere Primzahl p: " + p);
        System.out.println("Primitivwurzel g: " + g);
    }
}