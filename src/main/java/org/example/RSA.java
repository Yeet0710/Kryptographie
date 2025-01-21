
package org.example;

import java.math.*;
import java.security.*;

public class RSA {
    private BigInteger n; // RSA-Verfahren
    private BigInteger e; // Öffnelicher Exponent
    private BigInteger d; // Privater Exponent
    private int bitLegth = 1024; // Standard-Bit-Länge

    public RSA(int prizahlGroesse) {
        SecureRandom random = new SecureRandom();

        // Generiere zwei unterschiedliche Primzahlen
        BigInteger p = generierePrimzahl(prizahlGroesse, random);
        BigInteger q = generierePrimzahl(prizahlGroesse, random);

        // Berechne n = p * q
        n = p.multiply(q);

        // Berechne phi(n) = (p-1) * (q-1)
        BigInteger phi = p.subtract(BigInteger.ONE).multiply(q.subtract(BigInteger.ONE));

        // Finde einen öffentlichen Exponenten e
        e = BigInteger.valueOf(65537); // Standardwert für e
        while (!phi.gcd(e).equals(BigInteger.ONE)) {
            e = e.add(BigInteger.ONE); // Nächstmöglichen Wert für e prüfen
        }

         // Berechne den privaten Schlüssel d
        d = e.modInverse(phi); // Privater Schlüssel
    }

    // Methode zur Primzahlgenerierung
    private BigInteger generierePrimzahl(int bits, SecureRandom random) {
        return BigInteger.probablePrime(bits, random);
    }

    public BigInteger getPublicKey() {
        return e;
    }

    public BigInteger getPrivateKey() {
        return d;
    }

    public BigInteger getModulus() {
        return n;
    }

    public static void main(String[] args) {
        int bitLength = 512; // Größe der Primzahlen
        RSA rsa = new RSA(bitLength);

        System.out.println("Modulus n: " + rsa.getModulus());
        System.out.println("Öffentlicher Schlüssel e: " + rsa.getPublicKey());
        System.out.println("Privater Schlüssel d: " + rsa.getPrivateKey());
    }

}
