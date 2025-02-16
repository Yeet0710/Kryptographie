package org.scrum1_2;

import org.junit.jupiter.api.Test;
import java.math.BigInteger;
import static org.junit.jupiter.api.Assertions.*;

class PrimTesterTest {

    @Test
    void testIstPrimzahlMitEchterPrimzahl() {
        BigInteger prime = new BigInteger("104729"); // 10000. Primzahl
        assertTrue(PrimTester.istPrimzahl(prime, 20), prime + " sollte als Primzahl erkannt werden.");
    }

    @Test
    void testIstPrimzahlMitZusammengesetzterZahl() {
        BigInteger composite = new BigInteger("104730"); // 104729 + 1, also keine Primzahl
        assertFalse(PrimTester.istPrimzahl(composite, 20), composite + " sollte als zusammengesetzte Zahl erkannt werden.");
    }

    @Test
    void testIstPrimzahlMitKleinenPrimzahlen() {
        assertTrue(PrimTester.istPrimzahl(BigInteger.TWO, 10), "2 sollte als Primzahl erkannt werden.");
        assertTrue(PrimTester.istPrimzahl(BigInteger.valueOf(3), 10), "3 sollte als Primzahl erkannt werden.");
        assertTrue(PrimTester.istPrimzahl(BigInteger.valueOf(17), 10), "17 sollte als Primzahl erkannt werden.");
    }

    @Test
    void testIstPrimzahlMitKleinenZusammengesetztenZahlen() {
        assertFalse(PrimTester.istPrimzahl(BigInteger.ONE, 10), "1 ist keine Primzahl.");
        assertFalse(PrimTester.istPrimzahl(BigInteger.ZERO, 10), "0 ist keine Primzahl.");
        assertFalse(PrimTester.istPrimzahl(BigInteger.valueOf(-5), 10), "-5 ist keine Primzahl.");
        assertFalse(PrimTester.istPrimzahl(BigInteger.valueOf(4), 10), "4 ist keine Primzahl.");
    }

    @Test
    void testIstPrimzahlMitGroßenPrimzahlen() {
        BigInteger prime = new BigInteger("9999999967"); // Eine bekannte große Primzahl
        assertTrue(PrimTester.istPrimzahl(prime, 20), prime + " sollte als Primzahl erkannt werden.");
    }

    @Test
    void testIstPrimzahlMitGroßenZusammengesetztenZahlen() {
        BigInteger composite = new BigInteger("9999999998"); // Eine große gerade Zahl, keine Primzahl
        assertFalse(PrimTester.istPrimzahl(composite, 20), composite + " sollte als zusammengesetzte Zahl erkannt werden.");
    }
}
