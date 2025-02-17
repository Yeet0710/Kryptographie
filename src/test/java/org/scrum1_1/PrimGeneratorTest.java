package org.scrum1_1;

import org.junit.jupiter.api.Test;
import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.*;

class PrimGeneratorTest {

    @Test
    void testGenerateRandomPrimeWithinBounds() {
        BigInteger lowerBound = new BigInteger("200000");
        BigInteger upperBound = new BigInteger("300000");
        int mrIterations = 20;

        BigInteger prime = PrimGenerator.generateRandomPrime(lowerBound, upperBound, mrIterations);

        assertNotNull(prime, "Die generierte Primzahl sollte nicht null sein.");
        assertTrue(prime.compareTo(lowerBound) >= 0 && prime.compareTo(upperBound) <= 0,
                "Die Primzahl muss innerhalb der angegebenen Grenzen liegen.");
        assertTrue(prime.isProbablePrime(10), "Die Zahl sollte mit hoher Wahrscheinlichkeit eine Primzahl sein.");
    }

    @Test
    void testGenerateRandomPrimeThrowsExceptionForInvalidBounds() {
        BigInteger lowerBound = new BigInteger("500000");
        BigInteger upperBound = new BigInteger("200000");
        int mrIterations = 20;

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                PrimGenerator.generateRandomPrime(lowerBound, upperBound, mrIterations));
        assertEquals("Die untere Schranke muss kleiner als die obere Schranke sein.", exception.getMessage());
    }

    @Test
    void testGenerateRandomPrimeThrowsExceptionForNegativeLowerBound() {
        BigInteger lowerBound = new BigInteger("-1");
        BigInteger upperBound = new BigInteger("100");
        int mrIterations = 20;

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                PrimGenerator.generateRandomPrime(lowerBound, upperBound, mrIterations));
        assertEquals("Die untere Schranke muss größer oder gleich 0 sein.", exception.getMessage());
    }

    @Test
    void testGetRandomBigIntegerWithinBounds() {
        BigInteger lowerBound = new BigInteger("1000");
        BigInteger upperBound = new BigInteger("2000");

        for (int i = 0; i < 100; i++) {  // Mehrfach testen, um Zufallswerte zu überprüfen
            BigInteger randomBigInt = PrimGenerator.generateRandomPrime(lowerBound, upperBound, 10);
            assertNotNull(randomBigInt);
            assertTrue(randomBigInt.compareTo(lowerBound) >= 0 && randomBigInt.compareTo(upperBound) <= 0,
                    "Die generierte Zahl muss innerhalb der Grenzen liegen.");
            assertEquals(BigInteger.ONE, randomBigInt.mod(BigInteger.TWO), "Die generierte Zahl sollte ungerade sein.");
        }
    }
}
