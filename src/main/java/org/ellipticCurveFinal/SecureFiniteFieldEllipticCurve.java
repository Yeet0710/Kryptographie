package org.ellipticCurveFinal;

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
     * Generiert eine Primzahl p der angegebenen Bitlänge, die der Kongruenzbedingung p ≡ 5 mod 8 genügt.
     */
    private BigInteger generatePrimeCongruentToFiveModEight(int bitLength, int millerRabinIterations, SecureRandom random) {
        while (true) {
            BigInteger p = BigInteger.probablePrime(bitLength, random);
            // Korrigiere p, sodass p ≡ 5 mod 8.
            BigInteger mod8 = p.mod(BigInteger.valueOf(8));
            BigInteger adjustment = BigInteger.valueOf(5).subtract(mod8);
            p = p.add(adjustment);
            // Falls die Anpassung die Bitlänge überschreitet, justiere.
            if (p.bitLength() > bitLength) {
                p = p.subtract(BigInteger.valueOf(8));
            }
            if (p.isProbablePrime(millerRabinIterations)) {
                return p;
            }
        }
    }
}