package org.rsa;

import org.junit.jupiter.api.Test;
import java.math.BigInteger;
import static org.junit.jupiter.api.Assertions.*;

class PrimzahlTestTest {

    @Test
    void testIstPrimzahl_KleinePrimzahlen() {
        assertTrue(PrimzahlTest.istPrimzahl(BigInteger.valueOf(2), 10));
        assertTrue(PrimzahlTest.istPrimzahl(BigInteger.valueOf(3), 10));
        assertTrue(PrimzahlTest.istPrimzahl(BigInteger.valueOf(5), 10));
        assertTrue(PrimzahlTest.istPrimzahl(BigInteger.valueOf(7), 10));
    }

    @Test
    void testIstPrimzahl_KleineZusammengesetzteZahlen() {
        assertFalse(PrimzahlTest.istPrimzahl(BigInteger.valueOf(1), 10));
        assertFalse(PrimzahlTest.istPrimzahl(BigInteger.valueOf(4), 10));
        assertFalse(PrimzahlTest.istPrimzahl(BigInteger.valueOf(6), 10));
        assertFalse(PrimzahlTest.istPrimzahl(BigInteger.valueOf(9), 10));
    }

    @Test
    void testIstPrimzahl_GroßePrimzahlen() {
        assertTrue(PrimzahlTest.istPrimzahl(new BigInteger("101"), 10));
        assertTrue(PrimzahlTest.istPrimzahl(new BigInteger("6733"), 10));
        assertTrue(PrimzahlTest.istPrimzahl(new BigInteger("104729"), 10)); // 10000. Primzahl
    }

    @Test
    void testIstPrimzahl_GroßeZusammengesetzteZahlen() {
        assertFalse(PrimzahlTest.istPrimzahl(new BigInteger("100"), 10));
        assertFalse(PrimzahlTest.istPrimzahl(new BigInteger("9999"), 10));
        assertFalse(PrimzahlTest.istPrimzahl(new BigInteger("123456789"), 10));
    }
}
