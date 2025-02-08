package org.scrum1_6;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Scanner;

import org.scrum1_1.PrimGenerator;
import org.scrum1_3.schnelleExponentiation;
import org.scrum1_4.erweiterterEuklid;

public class RSAUTF8 {

    private BigInteger n, d, e; // Öffentlicher Modulus, privater Schlüssel und öffentlicher Exponent
    private static final SecureRandom random = new SecureRandom();

    public RSAUTF8(int bitLength) {
        // Bestimmung der unteren und oberen Grenze für die Primzahlgenerierung
        BigInteger lowerBound = schnelleExponentiation.schnelleExponentiation(BigInteger.TWO, BigInteger.valueOf(bitLength / 2-1), BigInteger.valueOf(2).pow(bitLength));
        System.out.println("lowerBound: " + lowerBound);
        BigInteger upperBound = schnelleExponentiation.schnelleExponentiation(BigInteger.TWO, BigInteger.valueOf(bitLength / 2), BigInteger.valueOf(2).pow(bitLength));
        System.out.println("upperBoud: " + upperBound);


        // Generierung zweier großer Primzahlen
        BigInteger p = PrimGenerator.generateRandomPrime(lowerBound, upperBound, 20);
        System.out.println("p: " + p);
        BigInteger q = PrimGenerator.generateRandomPrime(lowerBound, upperBound, 20);
        System.out.println("q: "+ q);

        n = p.multiply(q); // Berechnung des Modulus

        BigInteger phi = (p.subtract(BigInteger.ONE)).multiply(q.subtract(BigInteger.ONE)); // Berechnung von phi(n)
        e = BigInteger.valueOf(65537); // Standardwert für e
        d = erweiterterEuklid.erweiterterEuklid(e, phi)[1].mod(phi).add(phi).mod(phi); // Berechnung des Chiffrats
    }


    public BigInteger encrypt (String message) {
        byte[] bytes = message.getBytes(StandardCharsets.UTF_8);
        BigInteger plainText = new BigInteger(1, bytes);
        return schnelleExponentiation.schnelleExponentiation(plainText, e, n);
    }


    public String decrypt(BigInteger ciphertext) {
        BigInteger plaintext = schnelleExponentiation.schnelleExponentiation(ciphertext, d, n);
        byte[] bytes = plaintext.toByteArray();
        return new String(bytes, StandardCharsets.UTF_8);
    }

    public BigInteger getPublicKey(){
        return e;
    }

    public BigInteger getModulus(){
        return n;
    }

    public static void main(String[] args) {
        RSAUTF8 rsa = new RSAUTF8(1024);

        Scanner scanner = new Scanner(System.in);
        System.out.println("Gebe die Nachrticht ein: ");
        String message = scanner.nextLine();

        System.out.println("Original Nachricht: " + message);

        long startEnc = System.nanoTime();
        BigInteger encrypted = rsa.encrypt(message);
        long endEnc = System.nanoTime();
        long encTime = (endEnc - startEnc) / 1_000_000; // Millisekunden
        System.out.println("Verschlüsselte Nachricht:" + encrypted);
        System.out.println("Verschlüsselung dauerte: " + encTime + " ms");


        long startDec = System.nanoTime();
        String decrypted = rsa.decrypt(encrypted);
        long endDec = System.nanoTime();
        long decTime = (endDec - startDec) / 1_000_000; // Millisekunden
        System.out.println("Entschlüsselte Nachricht: " + decrypted);
        System.out.println("Entschlüsselung dauerte: " + decTime + " ms");
    }

}
