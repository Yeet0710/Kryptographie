package org.rsa;

import org.junit.jupiter.api.Test;
import java.math.BigInteger;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RSAAnwendungTest {

    @Test
    void testVerschluesselnUndEntschluesseln() {
        RSA rsa = new RSA(512);

        BigInteger publicKey = rsa.getPublicKey();
        BigInteger privateKey = rsa.getPrivateKey();
        BigInteger modulus = rsa.getModulus();

        String nachricht = "Hallo";
        BigInteger verschluesselt = RSAAnwendung.verschluesseln(nachricht, publicKey, modulus);
        String entschluesselt = RSAAnwendung.entschluesseln(verschluesselt, privateKey, modulus);

        assertEquals(nachricht, entschluesselt, "Entschlüsselter Text sollte mit dem Original übereinstimmen");
    }

    @Test
    void testBlockChiffreIntegration() {
        String text = "Dies ist ein Test.";
        List<String> bloecke = BlockChiffre.zerlegeInBloecke(text, 5);

        assertFalse(bloecke.isEmpty(), "Die Liste der Blöcke sollte nicht leer sein");
        assertTrue(bloecke.get(0).length() <= 5, "Die Blockgröße sollte maximal 5 Zeichen sein");
    }
}
