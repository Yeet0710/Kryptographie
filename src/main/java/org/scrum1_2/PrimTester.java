package org.scrum1_2;

import java.math.BigInteger;
import java.security.SecureRandom;

public class PrimTester {

    // Ein global wiederverwendbarer SecureRandom
    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * Prüft, ob 'zahl' eine wahrscheinliche Primzahl ist,
     * indem Miller-Rabin ('iteration')-mal ausgeführt wird.
     * @param zahl      die zu testende Zahl (BigInteger)
     * @param iteration Anzahl der Wiederholungen des MRT
     * @return true, wenn 'zahl' wahrscheinlich prim ist; sonst false
     */
    public static boolean istPrimzahl(BigInteger zahl, int iteration) {
        if (iteration < 1) {
            throw new IllegalArgumentException("Die Anzahl der Iterationen muss ≥ 1 sein.");
        }
        // 0 und 1 sind keine Primzahlen
        if (zahl.compareTo(BigInteger.TWO) < 0) return false;
        // 2 und 3 sind Primzahlen
        if (zahl.equals(BigInteger.TWO) || zahl.equals(BigInteger.valueOf(3))) return true;
        // Gerade Zahlen (>2) sind keine Primzahlen
        if (zahl.mod(BigInteger.TWO).equals(BigInteger.ZERO)) return false;

        // Schnellcheck: kleine Primfaktoren (3, 5, 7)
        for (BigInteger p : new BigInteger[]{BigInteger.valueOf(3),
                BigInteger.valueOf(5),
                BigInteger.valueOf(7)}) {
            if (zahl.mod(p).equals(BigInteger.ZERO) && !zahl.equals(p)) {
                return false;
            }
        }

        // Zerlege (n - 1) = 2^r * d, wobei d ungerade
        BigInteger d = zahl.subtract(BigInteger.ONE);
        int r = 0;
        while (d.mod(BigInteger.TWO).equals(BigInteger.ZERO)) {
            d = d.divide(BigInteger.TWO);
            r++;
        }
        // Nun gilt: n - 1 = 2^r * d

        // Führe Miller-Rabin 'iteration'-mal durch
        for (int i = 0; i < iteration; i++) {
            if (!millerRabinTest(zahl, d, r)) {
                return false; // zusammengesetzt
            }
        }
        return true; // mit sehr hoher Wahrscheinlichkeit prim
    }

    /**
     * Ein einzelner Miller-Rabin-Testlauf mit vorzerlegtem d und gegebenem r.
     * @param zahl die zu testende Zahl (ungerade, >3)
     * @param d    ungerader Teil (n - 1 = 2^r * d)
     * @param r    Anzahl der Faktoren 2 in (n - 1)
     * @return true, wenn dieser Testlauf bestanden ist; false, wenn eindeutig zusammengesetzt
     */
    private static boolean millerRabinTest(BigInteger zahl, BigInteger d, int r) {
        // Wähle Basis a ∈ [2, n-2]
        BigInteger a = BigInteger.TWO
                .add(new BigInteger(zahl.bitLength(), RANDOM)
                        .mod(zahl.subtract(BigInteger.TWO)));

        // x = a^d mod n
        BigInteger x = a.modPow(d, zahl);
        // Wenn x ≡ 1 oder x ≡ -1 mod n: Testlauf bestanden
        if (x.equals(BigInteger.ONE) || x.equals(zahl.subtract(BigInteger.ONE))) {
            return true;
        }

        // Wiederhole Quadrieren (r-1)-mal: x ← x^2 mod n
        BigInteger dLok = d;
        for (int i = 1; i < r; i++) {
            x = x.modPow(BigInteger.TWO, zahl); // x = x^2 mod n
            dLok = dLok.multiply(BigInteger.TWO);

            // Wenn x ≡ 1, dann zusammengesetzt
            if (x.equals(BigInteger.ONE)) {
                return false;
            }
            // Wenn x ≡ -1, Testlauf bestanden
            if (x.equals(zahl.subtract(BigInteger.ONE))) {
                return true;
            }
        }

        // Nach r Schritten kein -1 gefunden → zusammengesetzt
        return false;
    }

    public static void main(String[] args) {
        BigInteger testZahl = new BigInteger("104729");
        int iteration = 20;

        if (istPrimzahl(testZahl, iteration)) {
            System.out.println(testZahl + " ist wahrscheinlich eine Primzahl.");
        } else {
            System.out.println(testZahl + " ist eine zusammengesetzte Zahl.");
        }
    }
}
