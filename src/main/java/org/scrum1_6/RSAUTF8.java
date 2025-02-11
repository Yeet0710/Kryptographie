package org.scrum1_6;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.scrum1_1.PrimGenerator;
import org.scrum1_3.schnelleExponentiation;
import org.scrum1_4.erweiterterEuklid;

public class RSAUTF8 {

    private BigInteger n, d, e; // Öffentlicher Modulus, privater Schlüssel und öffentlicher Exponent
    private BigInteger friendPubKey;
    private BigInteger friendModulus;
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
        System.out.println("----");
        BigInteger q = PrimGenerator.generateRandomPrime(lowerBound, upperBound, 20);
        System.out.println("q: "+ q);
        System.out.println("----");

        //n = new BigInteger("17919713328749531059679579059882654804055347423360948013871804491276390638725758869030759538024079438476111768651186675130277679621240389166571792891651228901025530035633166530257946180427862022390348215341370677446604393430523135058041112294047225443184297512135984454380609476551106950776493691294184621176914639973921349707044879942807789811786823252993497079137691499583110801566326703151788262775887888379529921057376951487536882809196486192456918844901276494977670646758089009067085974810877203637063938984538814039150840526597446511971652509821941698265541097749579146402395266584458935555359548932868085620347");
        n = p.multiply(q); // Berechnung des Modulus
        System.out.println("n: " + n);
        System.out.println("----");

        BigInteger phi = (p.subtract(BigInteger.ONE)).multiply(q.subtract(BigInteger.ONE)); // Berechnung von phi(n)
        //e = new BigInteger("5951684041794240124863372592554006095815175854659371013200566668128986705537928204991827804499766322600714383315668853023868404565117704147694540025263243");
        //e = new BigInteger("65537");

        // Generiere e teilerfremd zu φ(n)
        do {
            e = new BigInteger(bitLength / 2, random);
        } while (!erweiterterEuklid.sindTeilerfremd(phi, e));

        // privater Schlüssel d
        d = erweiterterEuklid.erweiterterEuklid(e, phi)[1].mod(phi).add(phi).mod(phi); // Berechnung des Chiffrats
        System.out.println("phi: " + phi);
        System.out.println("----");
        System.out.println("e: " + e);
        System.out.println("----");
        System.out.println("d: " + d);
        System.out.println("----");
    }

    //  Text in mehrere BigInteger-Blöcke umwandeln**
    public List<BigInteger> textToBigIntegerBlocks(String text, BigInteger n) {
        List<BigInteger> blocks = new ArrayList<>();
        int BLOCK_SIZE = (int) Math.floor(n.bitLength() / 8) - 1; // Blockgröße in Byte

        for (int i = 0; i < text.length(); i += BLOCK_SIZE) {
            String block = text.substring(i, Math.min(i + BLOCK_SIZE, text.length()));
            byte[] blockBytes = block.getBytes(StandardCharsets.UTF_8);
            blocks.add(new BigInteger(1, blockBytes));
        }
        return blocks;
    }

    // **BigInteger-Blöcke zurück in den ursprünglichen Text umwandeln**
    public String bigIntegerBlocksToText(List<BigInteger> blocks) {
        StringBuilder text = new StringBuilder();
        for (BigInteger block : blocks) {
            text.append(new String(block.toByteArray(), StandardCharsets.UTF_8));
        }
        return text.toString();
    }
    // Blockweise Verschlüsselung**
    public List<BigInteger> encrypt(String message, BigInteger friendPubKey, BigInteger friendModulus) {
        List<BigInteger> blocks = textToBigIntegerBlocks(message, friendModulus);
        List<BigInteger> encryptedBlocks = new ArrayList<>();

        for (BigInteger block : blocks) {
            encryptedBlocks.add(schnelleExponentiation.schnelleExponentiation(block, friendPubKey, friendModulus));
        }
        return encryptedBlocks;
    }
/*
    public BigInteger encrypt (String message, BigInteger friendPubKey, BigInteger friendModulus) {
        byte[] bytes = message.getBytes(StandardCharsets.UTF_8);
        BigInteger plainText = new BigInteger(1, bytes);
        //Verschlüsserung mit dem pubKey und Modulus der anderen Person
        return schnelleExponentiation.schnelleExponentiation(plainText, friendPubKey, friendModulus);
    }

 */

    // Blockweise Entschlüsselung**
    public String decrypt(List<BigInteger> encryptedBlocks) {
        List<BigInteger> decryptedBlocks = new ArrayList<>();

        for (BigInteger block : encryptedBlocks) {
            decryptedBlocks.add(schnelleExponentiation.schnelleExponentiation(block, d, n));
        }

        return bigIntegerBlocksToText(decryptedBlocks);
    }
/*
    public String decrypt(BigInteger ciphertext) {
        BigInteger plaintext = schnelleExponentiation.schnelleExponentiation(ciphertext, d, n);
        byte[] bytes = plaintext.toByteArray();
        return new String(bytes, StandardCharsets.UTF_8);
    }

 */

    public BigInteger getPublicKey(){
        return e;
    }

    public BigInteger getModulus(){
        return n;
    }
    public BigInteger getPrivateKey(){return d;}
    // Methode zum Setzen des öffentlichen Schlüssels des Kommunikationspartners
    public void setPublicKey(BigInteger pubKey, BigInteger modulus) {
        this.friendPubKey = pubKey;
        this.friendModulus = modulus;
        System.out.println("Öffentlicher Schlüssel des Empfängers gesetzt!");
        System.out.println(" ");
    }
    public BigInteger getFriendPubKey(){
        if(friendPubKey == null) {
            System.out.println("Public Key von Bob wurde noch nicht gesetzt.");
            return BigInteger.ZERO;
        }
        return friendPubKey;
    }

    public BigInteger getFriendModulus() {
        if(friendModulus == null) {
            System.out.println("Modulus von Bob wurde noch nicht gesetzt.");
        }
        return friendModulus;
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        RSAUTF8 aliceRSA = new RSAUTF8(1024);
        RSAUTF8 bobRSA = new RSAUTF8(1024);


        // Alice pubKey und n ausgeben
        System.out.println("Eigener öffentlicher Schlüssel (e): " + aliceRSA.getPublicKey());
        System.out.println("Eigener Modulus (n): " + aliceRSA.getModulus());
        System.out.println("Eigener privater Schlüssel (d): " + aliceRSA.getPrivateKey());


        // Eingabe des pubKeys des Kommunikationspartners
        System.out.println("\nGib den öffentlichen Schlüssel des Empfängers ein:");
        BigInteger friendPubKey = new BigInteger(scanner.nextLine());

        System.out.println("Gib den Modulus des Empfängers ein:");
        BigInteger friendModulus = new BigInteger(scanner.nextLine());

        bobRSA.setPublicKey(friendPubKey,friendModulus);
        // Bob pubKey und n ausgeben
        System.out.println("Bob's öffentlicher Schlüssel (e): " + bobRSA.getFriendPubKey());
        System.out.println("Bob's Modulus (n): " + bobRSA.getFriendModulus());
        System.out.println("Bob's privater Schlüssel (d): " + bobRSA.getPrivateKey());

        // Nachricht eingeben
        System.out.println("\nGebe die Nachricht ein:");
        String message = scanner.nextLine();

        System.out.println("\nOriginal Nachricht: " + message);

        // Verschlüsselung
        long startEnc = System.nanoTime();
        List<BigInteger> encrypted = aliceRSA.encrypt(message,bobRSA.getPublicKey(),bobRSA.getModulus());
        long endEnc = System.nanoTime();

        long encTime = (endEnc - startEnc) / 1_000_000; // Millisekunden
        System.out.println("\nVerschlüsselte Blöcke:");
        for (BigInteger block : encrypted) {
            System.out.println(block);
        }
        System.out.println("\nVerschlüsselung dauerte: " + encTime + " ms");


        long startDec = System.nanoTime();
        String decrypted = bobRSA.decrypt(encrypted);
        long endDec = System.nanoTime();

        long decTime = (endDec - startDec) / 1_000_000; // Millisekunden
        System.out.println("Entschlüsselte Nachricht: " + decrypted);
        System.out.println("Entschlüsselung dauerte: " + decTime + " ms");
    }

}
