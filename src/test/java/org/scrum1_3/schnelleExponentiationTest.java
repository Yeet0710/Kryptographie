package org.scrum1_3;

import org.junit.jupiter.api.Test;
import java.math.BigInteger;
import java.security.SecureRandom;

import static org.junit.jupiter.api.Assertions.*;

class SchnelleExponentiationTest {

    @Test
    void testKleineWerte() {
        assertEquals(BigInteger.valueOf(4),
                schnelleExponentiation.schnelleExponentiation(BigInteger.valueOf(2), BigInteger.valueOf(2), BigInteger.valueOf(5)),
                "2^2 mod 5 sollte 4 sein.");

        assertEquals(BigInteger.ONE,
                schnelleExponentiation.schnelleExponentiation(BigInteger.valueOf(3), BigInteger.ZERO, BigInteger.valueOf(7)),
                "Jede Zahl hoch 0 mod irgendetwas sollte 1 sein.");
    }

    @Test
    void testGroßeZahlen() {
        BigInteger basis = new BigInteger("123456789");
        BigInteger exponent = new BigInteger("987654321");
        BigInteger modulus = new BigInteger("1000000007");
        BigInteger expected = new BigInteger("652541198"); // Vorberechnetes Ergebnis

        assertEquals(expected, schnelleExponentiation.schnelleExponentiation(basis, exponent, modulus),
                "Das vorher berechnete Ergebnis sollte mit der Methode übereinstimmen.");
    }

    @Test
    void testModuloEins() {
        assertEquals(BigInteger.ZERO,
                schnelleExponentiation.schnelleExponentiation(BigInteger.TEN, BigInteger.TEN, BigInteger.ONE),
                "Jede Zahl mod 1 sollte 0 sein.");
    }

    @Test
    void testNegativeBasis() {
        BigInteger basis = new BigInteger("-3");
        BigInteger exponent = new BigInteger("3");
        BigInteger modulus = new BigInteger("7");

        assertEquals(BigInteger.valueOf(1),
                schnelleExponentiation.schnelleExponentiation(basis, exponent, modulus),
                "-3^3 mod 7 sollte 1 sein.");
    }

    @Test
    void testGroßePrimzahlen() {
        BigInteger basis = new BigInteger("987654321987654321");
        BigInteger exponent = new BigInteger("123456789123456789");
        BigInteger modulus = new BigInteger("100000000000000003");

        BigInteger result = schnelleExponentiation.schnelleExponentiation(basis, exponent, modulus);
        assertNotNull(result, "Das Ergebnis sollte berechnet werden.");
    }
    @Test
    void testSehrGroßeZahlen() {
        // Erzeugen sehr großer Zufallszahlen (2048 Bit)
        SecureRandom random = new SecureRandom();
        int bitLength = 2048;
        BigInteger basis = new BigInteger(bitLength, random);
        BigInteger exponent = new BigInteger(bitLength, random);
        // Sicherstellen, dass das Modul positiv und größer als 1 ist:
        BigInteger modulus = new BigInteger(bitLength, random).abs().add(BigInteger.TWO);

        BigInteger result = schnelleExponentiation.schnelleExponentiation(basis, exponent, modulus);

        // Überprüfen, ob das Ergebnis berechnet wurde und kleiner als das Modul ist
        assertNotNull(result, "Das Ergebnis sollte berechnet werden.");
        assertTrue(result.compareTo(modulus) < 0, "Das Ergebnis muss kleiner als das Modul sein.");
    }

}
