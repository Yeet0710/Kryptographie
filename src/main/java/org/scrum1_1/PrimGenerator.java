package org.scrum1_1;
import java.math.*;
import java.security.*;

import static org.scrum1_2.PrimTester.istPrimzahl;

public class PrimGenerator {
    private static final SecureRandom random = new SecureRandom();
    /**
     * Generiert eine zufällige Primzahl im Bereich [a, b] unter Verwendung des Miller-Rabin-Tests.
     * @param a Untere Schranke (inklusive)
     * @param b Obere Schranke (inklusive)
     * @param mrIterations Anzahl der Miller-Rabin-Iterationen
     * @return Eine zufällige Primzahl im gegebenen Bereich
     */
    public static BigInteger generateRandomPrime(BigInteger a, BigInteger b, int mrIterations) {
        // Kontrolle, dass a ≤ b
        if (a.compareTo(b) > 0) {
            throw new IllegalArgumentException("Die untere Schranke muss kleiner als die obere Schranke sein.");
        }
        // Kontrolle, dass a ≥ 0
        if (a.compareTo(BigInteger.ZERO) < 0) {
            throw new IllegalArgumentException("Die untere Schranke muss größer oder gleich 0 sein.");
        }

        long startTime = System.currentTimeMillis();  // Zeitmessung starten
        BigInteger primeCandidate;
        int attempts = 0;
        while (true) {
            attempts++;
            primeCandidate = getRandomBigInteger(a, b);         // erzeugt zufällige ungerade Zahl
            if (istPrimzahl(primeCandidate, mrIterations)) {    // prüft, ob diese Zahl eine Primzahl ist
                long duration = System.currentTimeMillis() - startTime;
                System.out.println("Primzahl gefunden: " + primeCandidate);
                System.out.println("Anzahl Versuche: " + attempts);
                System.out.println("Benötigte Zeit: " + duration + " ms");
                return primeCandidate;
            }
        }
    }


    /**
     * Generiert eine zufällige ungerade Zahl im Bereich [a, b].
     */
    private static BigInteger getRandomBigInteger(BigInteger a, BigInteger b) {
        BigInteger range = b.subtract(a).add(BigInteger.ONE); // ermittelt die differenz b - a = range
        BigInteger randomBigInt;
        do {
            randomBigInt = new BigInteger(range.bitLength(), random).mod(range).add(a); // erzeugt Zahl in range und addiert a
        } while (randomBigInt.mod(BigInteger.TWO).equals(BigInteger.ZERO)); // Sicherstellen, dass die Zahl ungerade ist
        return randomBigInt;
    }

    public static void main(String[] args) {
        BigInteger lowerBound = new BigInteger("200000"); // Untere Grenze der Range
        BigInteger upperBound = new BigInteger("300000"); // Obere Grenze der Range
        int mrIterations = 20;  // Anzahl der Iterationen für den Miller-Rabin-Test

        BigInteger prime = generateRandomPrime(lowerBound, upperBound, mrIterations);
        System.out.println("Generierte Primzahl: " + prime);
    }
}
