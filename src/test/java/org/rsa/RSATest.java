package org.rsa;

import org.junit.jupiter.api.Test;
import java.math.BigInteger;
import static org.junit.jupiter.api.Assertions.*;

class RSATest {

    @Test
    void testRSAKeyGeneration() {
        int bitLength = 512;
        RSA rsa = new RSA(bitLength);

        BigInteger n = rsa.getModulus();
        BigInteger e = rsa.getPublicKey();
        BigInteger d = rsa.getPrivateKey();

        assertNotNull(n);
        assertNotNull(e);
        assertNotNull(d);

        // Überprüfung, ob der öffentliche Exponent e eine häufig genutzte Zahl ist
        assertTrue(e.equals(BigInteger.valueOf(65537)), "Öffentlicher Schlüssel e sollte 65537 sein");

        // Testet, ob d korrekt das multiplikative Inverse von e modulo phi(n) ist
        BigInteger phi = n.subtract(BigInteger.ONE);
        assertEquals(BigInteger.ONE, e.multiply(d).mod(phi));
    }

    @Test
    void testPrimeGeneration() {
        RSA rsa = new RSA(512);

        BigInteger prime = rsa.generatePrime(512);
        assertTrue(PrimzahlTest.istPrimzahl(prime, 100));
    }
}
