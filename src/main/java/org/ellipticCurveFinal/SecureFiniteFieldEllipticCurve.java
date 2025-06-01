package org.ellipticCurveFinal;

import org.scrum1_3.schnelleExponentiation;

import java.math.BigInteger;
import java.security.SecureRandom;

public class SecureFiniteFieldEllipticCurve {

    private final FiniteFieldEllipticCurve curve;
    private final BigInteger q; // Untergruppenordnung: q = N/8

    /**
     * Konstruktor: Erzeugt eine sichere elliptische Kurve mit fester Form y² = x³ - x.
     *
     * @param bitLength             Bitlänge für die zu generierende Primzahl p.
     * @param millerRabinIterations Anzahl der Iterationen für den Miller-Rabin-Test.
     */
    public SecureFiniteFieldEllipticCurve(int bitLength, int millerRabinIterations) {
        SecureRandom random = new SecureRandom();
        BigInteger p;
        FiniteFieldEllipticCurve candidateCurve;
        BigInteger order;
        BigInteger qCandidate;

        while (true) {
            // Erzeuge eine Primzahl p der gewünschten Bitlänge, sodass p ≡ 5 mod 8 gilt.
            p = generatePrimeCongruentToFiveModEight(bitLength, millerRabinIterations, random);

            candidateCurve = new FiniteFieldEllipticCurve(p);

            // Berechne die Gruppenordnung N = p + 1 - h.
            order = candidateCurve.calculateGroupOrder();

            // Prüfe, ob N durch 8 teilbar ist.
            if (!order.mod(BigInteger.valueOf(8)).equals(BigInteger.ZERO)) {
                continue;
            }

            qCandidate = order.divide(BigInteger.valueOf(8));

            // Prüfe, ob qCandidate prim ist (Miller-Rabin-Test).
            if (qCandidate.isProbablePrime(millerRabinIterations)) {
                break;
            }
        }

        candidateCurve.setQ(qCandidate);
        this.curve = candidateCurve;
        this.q = qCandidate;
    }

    public FiniteFieldEllipticCurve getCurve() {
        return curve;
    }

    public BigInteger getQ() {
        return q;
    }

    /**
     * Erzeugt eine Primzahl p mit gegebener Bitlänge,
     * so dass p ≡ 5 mod 8 gilt,
     * indem zufällige Kandidaten per Miller–Rabin geprüft werden.
     *
     * @param bitLength Bitlänge von p
     * @param mrRounds  Anzahl der Miller–Rabin-Runden
     * @param rnd       SecureRandom-Instanz
     * @return Primzahl p ≡ 5 mod 8
     */
    public static BigInteger generatePrimeCongruentToFiveModEight(
            int bitLength,
            int mrRounds,
            SecureRandom rnd
    ) {
        BigInteger eight = BigInteger.valueOf(8);
        BigInteger targetMod = BigInteger.valueOf(5);

        while (true) {
            // Zufallskandidat mit höchstem Bit = 1
            BigInteger p = new BigInteger(bitLength, rnd).setBit(bitLength - 1);

            // Auf p ≡ 5 mod 8 korrigieren
            BigInteger mod8 = p.mod(eight);
            BigInteger adjust = targetMod.subtract(mod8);
            p = p.add(adjust);
            if (p.bitLength() != bitLength) {
                // Bitlänge verletzt → nächster Versuch
                continue;
            }

            // Miller–Rabin-Prüfung
            if (isProbablePrimeMR(p, mrRounds, rnd)) {
                return p;
            }
        }
    }

    /**
     * Miller–Rabin-Test: prüft, ob n vermutlich prim ist.
     * Verwendet eure schnelleExponentiation für a^d mod n.
     *
     * @param n          Ungerade Zahl > 2
     * @param iterations Anzahl der Test-Runden
     * @param rnd        SecureRandom-Instanz
     * @return true, falls n vermutlich prim
     */
    public static boolean isProbablePrimeMR(BigInteger n, int iterations, SecureRandom rnd) {
        if (n.compareTo(BigInteger.TWO) < 0) return false;
        if (n.equals(BigInteger.TWO) || n.equals(BigInteger.valueOf(3))) return true;
        if (n.mod(BigInteger.TWO).equals(BigInteger.ZERO)) return false;

        // Schreibe n-1 = 2^s * d mit d ungerade
        BigInteger d = n.subtract(BigInteger.ONE);
        int s = d.getLowestSetBit();
        d = d.shiftRight(s);

        for (int i = 0; i < iterations; i++) {
            // Zufällige Basis a ∈ [2, n-2]
            BigInteger a;
            do {
                a = new BigInteger(n.bitLength(), rnd);
            } while (a.compareTo(BigInteger.TWO) < 0 || a.compareTo(n.subtract(BigInteger.TWO)) > 0);

            // Erstes a^d mod n
            BigInteger x = schnelleExponentiation.schnelleExponentiation(a, d, n);
            if (x.equals(BigInteger.ONE) || x.equals(n.subtract(BigInteger.ONE))) {
                continue;
            }
            boolean passed = false;
            for (int r = 1; r < s; r++) {
                // Quadrieren: x = x^2 mod n
                x = schnelleExponentiation.schnelleExponentiation(x, BigInteger.TWO, n);
                if (x.equals(n.subtract(BigInteger.ONE))) {
                    passed = true;
                    break;
                }
            }
            if (!passed) {
                return false; // n ist zusammengesetzt
            }
        }
        return true; // vermutlich prim
    }

}