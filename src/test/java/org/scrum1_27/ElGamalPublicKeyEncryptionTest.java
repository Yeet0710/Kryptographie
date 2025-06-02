package org.scrum1_27;

import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.security.SecureRandom;

import static org.junit.jupiter.api.Assertions.*;

public class ElGamalPublicKeyEncryptionTest {

    /**
     * Testet generateKeys(p,g):
     * - wähle p = 17 (Primzahl), g = 3 (Primitive Root mod 17).
     * - Es gilt: y = g^x mod p.
     */
    @Test
    void testGenerateKeys() {
        BigInteger p = BigInteger.valueOf(17);
        BigInteger g = BigInteger.valueOf(3);

        BigInteger[] keys = ElGamalPublicKeyEncryption.generateKeys(p, g);
        assertNotNull(keys);
        assertEquals(2, keys.length);

        BigInteger x = keys[0];   // privater Schlüssel
        BigInteger y = keys[1];   // öffentlicher Schlüssel

        assertTrue(x.compareTo(BigInteger.ZERO) > 0 && x.compareTo(p) < 0,
                "x muss im Bereich [1..p-1] liegen");
        // Prüfe, dass y == g^x mod p gilt
        BigInteger expectedY = g.modPow(x, p);
        assertEquals(expectedY, y, "y muss gleich g^x mod p sein");
    }

    /**
     * Testet encrypt(...) und decrypt(...) auf Lücke:
     * - wähle p = 17, g = 3, generiere Keys.
     * - wähle M = 5 (< p), rufe encrypt(M,p,g,y) → (a,b).
     * - rufe decrypt(a,b,p,x) → erhält M zurück.
     */
    @Test
    void testEncryptAndDecrypt() {
        BigInteger p = BigInteger.valueOf(17);
        BigInteger g = BigInteger.valueOf(3);

        // 1) Keys generieren
        BigInteger[] keys = ElGamalPublicKeyEncryption.generateKeys(p, g);
        BigInteger x = keys[0];
        BigInteger y = keys[1];

        // 2) Eine kleine Nachricht M < p
        BigInteger M = BigInteger.valueOf(5);

        // 3) Verschlüssele M
        BigInteger[] cipher = ElGamalPublicKeyEncryption.encrypt(M, p, g, y);
        assertNotNull(cipher);
        assertEquals(2, cipher.length);
        BigInteger a = cipher[0];
        BigInteger b = cipher[1];

        // 4) Entschlüssele wieder
        BigInteger decrypted = ElGamalPublicKeyEncryption.decrypt(a, b, p, x);
        assertEquals(M, decrypted, "Der entschlüsselte Wert muss der Originalnachricht entsprechen");
    }
}
