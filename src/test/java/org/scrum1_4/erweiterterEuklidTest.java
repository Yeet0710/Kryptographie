package org.scrum1_4;

import org.junit.jupiter.api.Test;
import java.math.BigInteger;
import static org.junit.jupiter.api.Assertions.*;

class ErweiterterEuklidTest {

    @Test
    void testGGT() {
        assertEquals(BigInteger.valueOf(6), erweiterterEuklid.ggt(BigInteger.valueOf(48), BigInteger.valueOf(18)), "ggT(48,18) sollte 6 sein.");
        assertEquals(BigInteger.ONE, erweiterterEuklid.ggt(BigInteger.valueOf(101), BigInteger.valueOf(10)), "ggT(101,10) sollte 1 sein.");
        assertEquals(BigInteger.valueOf(17), erweiterterEuklid.ggt(BigInteger.valueOf(51), BigInteger.valueOf(34)), "ggT(51,34) sollte 17 sein.");
    }

    @Test
    void testSindTeilerfremd() {
        assertTrue(erweiterterEuklid.sindTeilerfremd(BigInteger.valueOf(13), BigInteger.valueOf(4)), "13 und 4 sind teilerfremd.");
        assertFalse(erweiterterEuklid.sindTeilerfremd(BigInteger.valueOf(24), BigInteger.valueOf(6)), "24 und 6 sind nicht teilerfremd.");
    }

    @Test
    void testErweiterterEuklid() {
        BigInteger a = new BigInteger("120");
        BigInteger b = new BigInteger("23");
        BigInteger[] result = erweiterterEuklid.erweiterterEuklid(a, b);

        BigInteger ggT = result[0];
        BigInteger x = result[1];
        BigInteger y = result[2];

        assertEquals(BigInteger.ONE, ggT, "ggT(120,23) sollte 1 sein.");

        // Überprüfung der Bezout-Identität: a * x + b * y = ggT(a, b)
        BigInteger bezoutCheck = a.multiply(x).add(b.multiply(y));
        assertEquals(ggT, bezoutCheck, "Bezout-Identität sollte erfüllt sein.");
    }

    @Test
    void testErweiterterEuklidMitGroßenZahlen() {
        BigInteger a = new BigInteger("987654321987654321");
        BigInteger b = new BigInteger("123456789123456789");
        BigInteger[] result = erweiterterEuklid.erweiterterEuklid(a, b);

        BigInteger ggT = result[0];
        BigInteger x = result[1];
        BigInteger y = result[2];

        assertNotNull(result, "Das Ergebnis sollte nicht null sein.");
        assertEquals(a.multiply(x).add(b.multiply(y)), ggT, "Bezout-Identität sollte für große Zahlen erfüllt sein.");
    }
}
