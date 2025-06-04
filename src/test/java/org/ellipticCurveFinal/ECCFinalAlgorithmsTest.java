package org.ellipticCurveFinal;

import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.security.SecureRandom;

import static org.junit.jupiter.api.Assertions.*;

class ECCFinalAlgorithmsTest {

    private static final BigInteger P = BigInteger.valueOf(29); // 29 \u2261 5 mod 8
    private static final BigInteger PRIVATE_KEY = BigInteger.TWO;
    private static final String MESSAGE = "Hallo ECC";

    private FiniteFieldEllipticCurve setupCurve(BigInteger p) {
        FiniteFieldEllipticCurve curve = new FiniteFieldEllipticCurve(p);
        BigInteger order = curve.calculateGroupOrder();
        BigInteger q = order.divide(BigInteger.valueOf(8));
        curve.setQ(q);
        return curve;
    }

    private FiniteFieldEllipticCurve setupCurve() {
        return setupCurve(P);
    }

    @Test
    void testCalculateGroupOrder() {
        BigInteger[] primes = {
                BigInteger.valueOf(29),
                BigInteger.valueOf(37),
                BigInteger.valueOf(53),
                BigInteger.valueOf(61),
                BigInteger.valueOf(101)
        };
        BigInteger[] expected = {
                BigInteger.valueOf(40),
                BigInteger.valueOf(40),
                BigInteger.valueOf(40),
                BigInteger.valueOf(72),
                BigInteger.valueOf(104)
        };

        for (int i = 0; i < primes.length; i++) {
            FiniteFieldEllipticCurve c = new FiniteFieldEllipticCurve(primes[i]);
            assertEquals(expected[i], c.calculateGroupOrder(),
                    "group order for p=" + primes[i]);
        }
    }

    @Test
    void testEncryptDecrypt() {
        FiniteFieldEllipticCurve curve = setupCurve();
        BigInteger q = curve.getQ();
        ECPoint g = curve.findGenerator(q);
        ECPoint publicKey = g.multiply(PRIVATE_KEY, curve).normalize(curve);

        ECCElgamalBlockCipher.Result r = ECCElgamalBlockCipher.encrypt(
                MESSAGE,
                g,
                publicKey,
                P,
                q,
                curve
        );
        String decrypted = ECCElgamalBlockCipher.decrypt(r, PRIVATE_KEY, P, curve);
        assertEquals(MESSAGE, decrypted);

        // zusätzlicher Test mit größerer Primzahl, um Paddingfälle abzudecken
        BigInteger bigP = BigInteger.valueOf(269); // >= 256 → chunkSize > 0
        FiniteFieldEllipticCurve bigCurve = setupCurve(bigP);
        BigInteger bigQ = bigCurve.getQ();
        ECPoint g2 = bigCurve.findGenerator(bigQ);
        ECPoint pub2 = g2.multiply(PRIVATE_KEY, bigCurve).normalize(bigCurve);
        String msg2 = "A" + '\0' + "B"; // enthält Nullbyte

        ECCElgamalBlockCipher.Result r2 = ECCElgamalBlockCipher.encrypt(
                msg2,
                g2,
                pub2,
                bigP,
                bigQ,
                bigCurve
        );
        String dec2 = ECCElgamalBlockCipher.decrypt(r2, PRIVATE_KEY, bigP, bigCurve);
        assertEquals(msg2, dec2);
    }

    @Test
    void testSignAndVerify() {
        FiniteFieldEllipticCurve curve = setupCurve();
        BigInteger q = curve.getQ();
        ECPoint g = curve.findGenerator(q);
        ECPoint publicKey = g.multiply(PRIVATE_KEY, curve).normalize(curve);

        ECCSignature.Signature sig = ECCSignature.sign(MESSAGE, PRIVATE_KEY, q, g, curve);
        assertTrue(ECCSignature.verify(MESSAGE, sig, publicKey, q, g, curve));
        assertFalse(ECCSignature.verify("falsch", sig, publicKey, q, g, curve));

        // weitere Durchführung mit größerem p
        BigInteger bigP = BigInteger.valueOf(269);
        FiniteFieldEllipticCurve bigCurve = setupCurve(bigP);
        BigInteger bigQ = bigCurve.getQ();
        ECPoint g2 = bigCurve.findGenerator(bigQ);
        ECPoint pub2 = g2.multiply(PRIVATE_KEY, bigCurve).normalize(bigCurve);
        ECCSignature.Signature sig2 = ECCSignature.sign(MESSAGE, PRIVATE_KEY, bigQ, g2, bigCurve);
        assertTrue(ECCSignature.verify(MESSAGE, sig2, pub2, bigQ, g2, bigCurve));
        // manipulierte Signatur sollte fehlschlagen
        ECCSignature.Signature tampered = new ECCSignature.Signature(sig2.r.add(BigInteger.ONE), sig2.s);
        assertFalse(ECCSignature.verify(MESSAGE, tampered, pub2, bigQ, g2, bigCurve));
    }

    @Test
    void testIsProbablePrimeMR() {
        SecureRandom rnd = new SecureRandom();
        BigInteger[] primes = {
                BigInteger.valueOf(29),
                BigInteger.valueOf(37),
                BigInteger.valueOf(101)
        };
        for (BigInteger p : primes) {
            assertTrue(SecureFiniteFieldEllipticCurve.isProbablePrimeMR(p, 10, rnd),
                    "Prime check failed for " + p);
        }

        BigInteger[] composites = {
                BigInteger.valueOf(35),
                BigInteger.valueOf(561), // Carmichael-Zahl
                BigInteger.valueOf(1105)
        };
        for (BigInteger c : composites) {
            assertFalse(SecureFiniteFieldEllipticCurve.isProbablePrimeMR(c, 10, rnd),
                    "Composite not detected for " + c);
        }
    }
}
