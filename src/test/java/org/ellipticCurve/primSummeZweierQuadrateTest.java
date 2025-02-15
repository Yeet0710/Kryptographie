package org.ellipticCurve;

import org.junit.jupiter.api.Test;
import java.math.BigInteger;
import static org.junit.jupiter.api.Assertions.*;

class primSummeZweierQuadrateTest {

    // Beispielwerte für eine elliptische Kurve: y² = x³ + ax + b (mod p)
    BigInteger p = BigInteger.valueOf(17);
    BigInteger a = BigInteger.valueOf(2);
    BigInteger b = BigInteger.valueOf(2);
    primSummeZweierQuadrate ec = new primSummeZweierQuadrate(a, b, p);

    @Test
    void testPunktAddition() {
        primSummeZweierQuadrate.Punkt P = new primSummeZweierQuadrate.Punkt(BigInteger.valueOf(5), BigInteger.valueOf(1));
        primSummeZweierQuadrate.Punkt Q = new primSummeZweierQuadrate.Punkt(BigInteger.valueOf(6), BigInteger.valueOf(3));

        primSummeZweierQuadrate.Punkt result = ec.add(P, Q);
        assertNotNull(result);
        System.out.println("P + Q = (" + result.x + ", " + result.y + ")");
    }

    @Test
    void testPunktVerdopplung() {
        primSummeZweierQuadrate.Punkt P = new primSummeZweierQuadrate.Punkt(BigInteger.valueOf(5), BigInteger.valueOf(1));

        primSummeZweierQuadrate.Punkt result = ec.doubleP(P);
        assertNotNull(result);
        System.out.println("2P = (" + result.x + ", " + result.y + ")");
    }

    @Test
    void testSkalareMultiplikation() {
        primSummeZweierQuadrate.Punkt P = new primSummeZweierQuadrate.Punkt(BigInteger.valueOf(5), BigInteger.valueOf(1));

        primSummeZweierQuadrate.Punkt result = ec.multiply(P, BigInteger.valueOf(3));
        assertNotNull(result);
        System.out.println("3P = (" + result.x + ", " + result.y + ")");
    }

    @Test
    void testAdditionMitUnendlichemPunkt() {
        primSummeZweierQuadrate.Punkt P = new primSummeZweierQuadrate.Punkt(BigInteger.valueOf(5), BigInteger.valueOf(1));
        primSummeZweierQuadrate.Punkt O = primSummeZweierQuadrate.Punkt.infinity();

        primSummeZweierQuadrate.Punkt result = ec.add(P, O);
        assertEquals(P.x, result.x);
        assertEquals(P.y, result.y);
    }

    @Test
    void testSkalareMultiplikationMitNull() {
        primSummeZweierQuadrate.Punkt P = new primSummeZweierQuadrate.Punkt(BigInteger.valueOf(5), BigInteger.valueOf(1));

        primSummeZweierQuadrate.Punkt result = ec.multiply(P, BigInteger.ZERO);
        assertTrue(result.isInfinity());
    }
}
