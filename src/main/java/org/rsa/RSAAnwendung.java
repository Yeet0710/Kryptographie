package org.rsa;
import java.math.*;
import java.util.ArrayList;
import java.util.List;

public class RSAAnwendung {
    public static BigInteger verschluesseln(String nachricht, BigInteger publicKey, BigInteger modulus) {
        return new BigInteger(nachricht.getBytes()).modPow(publicKey, modulus);
    }

    public static String entschluesseln(BigInteger chiffriert, BigInteger privateKey, BigInteger modulus) {
        BigInteger entschluesselt = chiffriert.modPow(privateKey, modulus);
        return new String(entschluesselt.toByteArray());
    }

    public static void main(String[] args) {
        // Schritt 1: RSA-Schlüsselerzeugung
        RSA rsa = new RSA(512);

        BigInteger publicKey = rsa.getPublicKey();
        BigInteger privateKey = rsa.getPrivateKey();
        BigInteger modulus = rsa.getModulus();

        System.out.println("Öffentlicher Schlüssel: " + publicKey);
        System.out.println("Privater Schlüssel: " + privateKey);
        System.out.println("Modul (n): " + modulus);

        // Schritt 2: Klartext eingeben und in Blöcke zerlegen
        String klartext = "Joey ist ein besonder cooler dude.";
        List<String> bloecke = BlockChiffre.zerlegeInBloecke(klartext, 10);
        System.out.println("Klartext-Blöcke: " + bloecke);

        // Schritt 3: Blöcke verschlüsseln
        List<BigInteger> verschluesselteBloecke = new ArrayList<>();
        for (String block : bloecke) {
            verschluesselteBloecke.add(verschluesseln(block, publicKey, modulus));
        }
        System.out.println("Verschlüsselte Blöcke: " + verschluesselteBloecke);

        // Schritt 4: Blöcke entschlüsseln
        List<String> entschluesselteBloecke = new ArrayList<>();
        for (BigInteger verschluesselt : verschluesselteBloecke) {
            entschluesselteBloecke.add(entschluesseln(verschluesselt, privateKey, modulus));
        }
        System.out.println("Entschlüsselte Blöcke: " + entschluesselteBloecke);

        // Ausgabe des zusammengesetzten Textes
        System.out.println("Wiederhergestellter Text: " + String.join("", entschluesselteBloecke));
    }

}
