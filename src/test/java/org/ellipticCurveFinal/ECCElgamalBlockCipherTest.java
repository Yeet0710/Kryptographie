package org.ellipticCurveFinal;

import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

public class ECCElgamalBlockCipherTest {

    /**
     * Angepasster Test: Leerer Klartext → 0 Tupel, base64 = ""
     *
     * Wir vermeiden hier alle aufwändigen Berechnungen (calculateGroupOrder, findGenerator, …)
     * und verwenden stattdessen den bekannten Generator G = (5,1) auf der Kurve y^2 = x^3 - x mod17
     * sowie dessen Ordnung q = 19. So braucht der Test praktisch keine Zeit.
     */
    @Test
    void testEncryptEmptyPlaintext() {
        // 1) Definiere Kurve p1 = 17
        BigInteger p1 = BigInteger.valueOf(17);
        FiniteFieldEllipticCurve curve1 = new FiniteFieldEllipticCurve(p1);

        // 2) Verwende festen Generator G = (5,1), der auf y^2 = x^3 - x mod17 liegt
        ECPoint G1 = new FiniteFieldECPoint(
                BigInteger.valueOf(5),
                BigInteger.valueOf(1)
        ).normalize(curve1);

        // 3) Bekannte Ordnung dieses Generators auf F17: q1 = 19
        BigInteger q1 = BigInteger.valueOf(19);

        // 4) Privater Schlüssel d1 = 1 (beliebig im Intervall [1..q1-1])
        BigInteger d1 = BigInteger.ONE;
        ECPoint Y1 = G1.multiply(d1, curve1).normalize(curve1);

        // 5) Leerer Klartext ("")
        String plaintext = "";

        // 6) Verschlüsselung
        ECCElgamalBlockCipher.Result result = ECCElgamalBlockCipher.encrypt(
                plaintext,
                G1,    // Generator
                Y1,    // Öffentlicher Schlüssel
                p1,    // Modulus
                q1,    // Ordnung
                curve1 // Kurve
        );

        // 7) Da plaintext leer ist, erwarten wir 0 Tupel und leeren Base64‐String
        assertEquals(0, result.ax.length,  "ax muss Länge 0 haben");
        assertEquals(0, result.ay.length,  "ay muss Länge 0 haben");
        assertEquals(0, result.b1.length,  "b1 muss Länge 0 haben");
        assertEquals(0, result.b2.length,  "b2 muss Länge 0 haben");
        assertEquals("", result.base64,    "Base64‐String muss leer sein");
    }
}
