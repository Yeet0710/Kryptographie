package org.ellipticCurveFinal;

import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.security.SecureRandom;

import static org.junit.jupiter.api.Assertions.*;

public class SecureFiniteFieldEllipticCurveTest {

    /**
     * Testet, ob generatePrimeCongruentToFiveModEight(...) tatsächlich eine Primzahl p produziert
     * mit p ≡ 5 (mod 8). Und ob isProbablePrimeMR(...) bei diesem p den Primstatus bestätigt.
     */
    @Test
    void testGeneratePrimeCongruentToFiveModEightAndProbablePrime() {
        SecureRandom rnd = new SecureRandom();
        int bitLength = 8;  // klein, damit der Test schnell bleibt
        int mrRounds = 5;   // Miller-Rabin-Runden

        BigInteger p = SecureFiniteFieldEllipticCurve.generatePrimeCongruentToFiveModEight(
                bitLength, mrRounds, rnd);

        // 1) Prüfe: p mod 8 == 5
        assertEquals(BigInteger.valueOf(5), p.mod(BigInteger.valueOf(8)),
                "p ≡ 5 (mod 8) muss gelten");

        // 2) Prüfe: p ist vermutlich prim
        assertTrue(SecureFiniteFieldEllipticCurve.isProbablePrimeMR(p, mrRounds, rnd),
                "p sollte als wahrscheinlich prim erkannt werden");
    }

    /**
     * Testet den Konstruktor von SecureFiniteFieldEllipticCurve:
     * - Erzeugt eine Kurve mit y^2 = x^3 - x (mod p) und setzt q = N/8 (N=calculateGroupOrder()).
     * - Wir überprüfen, dass N durch 8 teilbar ist und getQ() tatsächlich N/8 liefert.
     */
    @Test
    void testSecureFiniteFieldEllipticCurveConstructorSetsQCorrectly() {
        int bitLength = 9;    // etwas größer, Gruppe aber dennoch überschaubar
        int mrRounds = 5;
        SecureFiniteFieldEllipticCurve secureCurve = new SecureFiniteFieldEllipticCurve(bitLength, mrRounds);

        // 1) Hole die zugrundeliegende Kurve und Q
        FiniteFieldEllipticCurve curve = secureCurve.getCurve();
        BigInteger q = secureCurve.getQ();
        BigInteger p = curve.getP();

        // 2) Berechne Gruppenordnung N = calculateGroupOrder()
        BigInteger N = curve.calculateGroupOrder();

        // Prüfe, dass N durch 8 teilbar ist
        assertEquals(BigInteger.ZERO, N.mod(BigInteger.valueOf(8)),
                "N (Gruppenordnung) muss durch 8 teilbar sein");

        // Prüfe, dass getQ() == N/8
        assertEquals(N.divide(BigInteger.valueOf(8)), q,
                "q muss gleich N/8 sein");

        // 3) Stichprobe: Teste, dass findGenerator(q) einen gültigen Punkt liefert
        ECPoint g = curve.findGenerator(q);
        assertNotNull(g, "Ein Generator sollte gefunden werden");
        assertTrue(curve.isValidPoint(g), "Generator muss ein gültiger Punkt sein");
        assertFalse(g instanceof InfinitePoint, "Generator darf nicht ∞ sein");
    }
}
