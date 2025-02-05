package org.scrum1_1;


import java.math.BigInteger;
import java.security.SecureRandom;

public class PrimGenerator {

    // soll eine zuf&auml;llige Primzahl im Bereich [a, b] unter Verwendung MRT generieren


    private static final SecureRandom random = new SecureRandom();
     /**
         * Generiert eine zufällige Primzahl im Bereich [a, b] unter Verwendung des Miller-Rabin-Tests.
         *
         * @param a Untere Schranke (inklusive)
         * @param b Obere Schranke (inklusive)
         * @param millerRabinSteps Anzahl der Miller-Rabin-Prüfungen
         * @return Eine zufällige Primzahl im gegebenen Bereich
         */
        public static BigInteger generateRandomPrime(BigInteger a, BigInteger b, int millerRabinSteps) {
            if (a.compareTo(b) > 0) {
                throw new IllegalArgumentException("Die untere Schranke muss kleiner als die obere Schranke sein.");
            }
            if (a.compareTo(BigInteger.ZERO) < 0) {
                throw new IllegalArgumentException("Die untere Schranke muss größer oder gleich 0 sein.");
            }

            BigInteger primeCandidate;
            while (true) {
                primeCandidate = getRandomBigInteger(a, b); // Zufällige ungerade Zahl generieren
                if (isProbablePrime(primeCandidate, millerRabinSteps)) {
                    return primeCandidate;
                }
            }
        }

        /**
         * Generiert eine zufällige ungerade Zahl im Bereich [a, b].
         */
        private static BigInteger getRandomBigInteger(BigInteger a, BigInteger b) {
            BigInteger range = b.subtract(a).add(BigInteger.ONE);
            BigInteger randomBigInt;
            do {
                randomBigInt = new BigInteger(range.bitLength(), random).mod(range).add(a);
            } while (randomBigInt.mod(BigInteger.TWO).equals(BigInteger.ZERO)); // Sicherstellen, dass die Zahl ungerade ist
            return randomBigInt;
        }

        /**
         * Führt den Miller-Rabin-Primalitätstest durch.
         *
         * @param n Zahl, die getestet werden soll
         * @param k Anzahl der Iterationen (je mehr, desto sicherer)
         * @return true, wenn die Zahl wahrscheinlich eine Primzahl ist, sonst false
         */
        public static boolean isProbablePrime(BigInteger n, int k) {
            if (n.compareTo(BigInteger.TWO) < 0) return false;
            if (n.equals(BigInteger.TWO) || n.equals(BigInteger.valueOf(3))) return true;
            if (n.mod(BigInteger.TWO).equals(BigInteger.ZERO)) return false;

            BigInteger d = n.subtract(BigInteger.ONE);
            int s = 0;

            while (d.mod(BigInteger.TWO).equals(BigInteger.ZERO)) {
                d = d.divide(BigInteger.TWO);
                s++;
            }

            for (int i = 0; i < k; i++) {
                BigInteger a = getRandomWitness(n);
                BigInteger x = a.modPow(d, n);

                if (x.equals(BigInteger.ONE) || x.equals(n.subtract(BigInteger.ONE))) {
                    continue;
                }

                boolean isComposite = true;
                for (int r = 0; r < s - 1; r++) {
                    x = x.modPow(BigInteger.TWO, n);
                    if (x.equals(n.subtract(BigInteger.ONE))) {
                        isComposite = false;
                        break;
                    }
                }
                if (isComposite) return false;
            }
            return true;
        }

        /**
         * Wählt eine zufällige Basis für den Miller-Rabin-Test.
         */
        private static BigInteger getRandomWitness(BigInteger n) {
            return new BigInteger(n.bitLength(), random).mod(n.subtract(BigInteger.TWO)).add(BigInteger.TWO);
        }

        public static void main(String[] args) {
            BigInteger lowerBound = new BigInteger("200000");
            BigInteger upperBound = new BigInteger("300000");
            int millerRabinIterations = 20;

            BigInteger prime = generateRandomPrime(lowerBound, upperBound, millerRabinIterations);
            System.out.println("Generierte Primzahl: " + prime);
        }
    }

