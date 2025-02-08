package org.ellipticCurve;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.math.BigInteger;

public class EllipticCurveTest {
    private EllipticCurve curve;
    private EllipticCurve.Point P;
    private BigInteger p = BigInteger.valueOf(97); // Primzahl für das endliche Feld

    @BeforeEach
    void setUp() {
        BigInteger a = BigInteger.valueOf(2);
        BigInteger b = BigInteger.valueOf(3);
        curve = new EllipticCurve(a, b, p);
        P = new EllipticCurve.Point(BigInteger.valueOf(3), BigInteger.valueOf(6));
    }

    @Test
    void testPunktAddition() {
        EllipticCurve.Point Q = new EllipticCurve.Point(BigInteger.valueOf(10), BigInteger.valueOf(20));
        EllipticCurve.Point result = curve.add(P, Q);
        assertNotNull(result, "Das Ergebnis sollte nicht null sein.");
    }

    @Test
    void testPunktVerdopplung() {
        EllipticCurve.Point result = curve.add(P, P);
        assertNotNull(result, "Das Ergebnis sollte nicht null sein.");
    }

    @Test
    void testSkalareMultiplikation() {
        BigInteger k = BigInteger.valueOf(5);
        EllipticCurve.Point result = curve.multiply(P, k);
        assertNotNull(result, "Das Ergebnis sollte nicht null sein.");
    }

    @Test
    void testNullstelleErgebnis() {
        EllipticCurve.Point result = curve.multiply(P, BigInteger.ZERO);
        assertTrue(result.isInfinity(), "0 * P sollte den Unendlich-Punkt ergeben.");
    }

    @Test
    void testAdditionMitUnendlichkeitspunkt() {
        EllipticCurve.Point inf = EllipticCurve.Point.infinity();
        EllipticCurve.Point result = curve.add(P, inf);
        assertEquals(P.x, result.x, "Addition mit Unendlich-Punkt sollte P ergeben.");
        assertEquals(P.y, result.y, "Addition mit Unendlich-Punkt sollte P ergeben.");
    }
}
