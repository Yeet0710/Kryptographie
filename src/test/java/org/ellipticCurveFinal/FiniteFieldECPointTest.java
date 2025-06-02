package org.ellipticCurveFinal;

import org.junit.jupiter.api.Test;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.*;

public class FiniteFieldECPointTest {

    /**
     * Testet normalize(...):
     * - Ein Punkt mit Koordinaten außerhalb [0..p−1] wird modulo p reduziert.
     * - Wenn das Ergebnis nicht auf der Kurve liegt, gibt normalize() ∞ zurück.
     */
    @Test
    void testNormalizeAndValidity() {
        BigInteger p = BigInteger.valueOf(17);
        FiniteFieldEllipticCurve curve = new FiniteFieldEllipticCurve(p);

        // Beispiel: Punkt (18, 18). Modulo 17 entspricht das (1, 1).
        // (1,1) ist nicht auf der Kurve y^2 = x^3 - x mod17, denn: 1^2=1 ; 1^3 - 1 = 0 ⇒ 1≠0
        // Daher liefert normalize(...) den Unendlich-Punkt.
        ECPoint P = new FiniteFieldECPoint(BigInteger.valueOf(18), BigInteger.valueOf(18));
        ECPoint norm = P.normalize(curve);
        assertTrue(norm instanceof InfinitePoint, "normalize muss ∞ liefern, wenn (1,1) nicht gültig ist");

        // Ein gültiger Punkt, z.B. (5,1). normalize sollte wieder (5,1) liefern.
        ECPoint valid = new FiniteFieldECPoint(BigInteger.valueOf(5), BigInteger.valueOf(1));
        ECPoint validNorm = valid.normalize(curve);
        assertFalse(validNorm instanceof InfinitePoint, "(5,1) ist gültig und darf nicht auf ∞ normalisiert werden");
        assertEquals(BigInteger.valueOf(5), validNorm.getX(), "X-Koordinate muss erhalten bleiben");
        assertEquals(BigInteger.valueOf(1), validNorm.getY(), "Y-Koordinate muss erhalten bleiben");
    }

    /**
     * Testet add(...) und doublePoint(...):
     * - Wir wählen P1=(0,0) und P2=(1,0) auf y^2 = x^3 - x mod17.
     * - P1 + P2 = (16, 0), wie man durch Vergleich von Hand nachprüft.
     * - doublePoint((0,0)) liefert ∞, da y=0.
     */
    @Test
    void testAddAndDoublePoint() {
        BigInteger p = BigInteger.valueOf(17);
        FiniteFieldEllipticCurve curve = new FiniteFieldEllipticCurve(p);

        ECPoint P1 = new FiniteFieldECPoint(BigInteger.ZERO, BigInteger.ZERO).normalize(curve);
        ECPoint P2 = new FiniteFieldECPoint(BigInteger.ONE, BigInteger.ZERO).normalize(curve);

        // Prüfe, dass beide Punkte gültig sind
        assertTrue(curve.isValidPoint(P1), "(0,0) sollte gültig sein");
        assertTrue(curve.isValidPoint(P2), "(1,0) sollte gültig sein");

        // P1 + P2
        ECPoint sum = P1.add(P2, curve).normalize(curve);
        // Erwartung: (16, 0)
        assertFalse(sum instanceof InfinitePoint, "Summe darf nicht ∞ sein");
        assertEquals(BigInteger.valueOf(16), sum.getX(), "Summe X-Koordinate falsch");
        assertEquals(BigInteger.ZERO, sum.getY(), "Summe Y-Koordinate falsch");

        // doublePoint(P1) = doublePoint((0,0)) = ∞, da y=0 ⇒ 2*P1 = ∞
        ECPoint dbl = P1.doublePoint(curve);
        assertTrue(dbl instanceof InfinitePoint, "DoublePoint((0,0)) muss ∞ sein");
    }
}
