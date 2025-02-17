package org.scrum1_6;

import org.scrum1_1.PrimGenerator;
import org.scrum1_3.schnelleExponentiation;
import org.scrum1_4.erweiterterEuklid;

import java.io.*;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

public class RSAUtils3404 {
    private static final SecureRandom random = new SecureRandom();
    private static final String E_FILE = "rsa3404_e.txt";
    private static final String N_FILE = "rsa3404_n.txt";
    private static final String D_FILE = "rsa3404_d.txt";

    private static BigInteger e;
    private static BigInteger n;
    private static BigInteger d;

    public static void generateAndSaveKeys(int bitLength) throws IOException {

        // Bestimmung der unteren und oberen Grenze für die Primzahlgenerierung
        BigInteger lowerBound = schnelleExponentiation.schnelleExponentiation(BigInteger.TWO, BigInteger.valueOf(bitLength / 2 - 1), BigInteger.valueOf(2).pow(bitLength));
        System.out.println("lowerBound: " + lowerBound);
        BigInteger upperBound = schnelleExponentiation.schnelleExponentiation(BigInteger.TWO, BigInteger.valueOf(bitLength / 2), BigInteger.valueOf(2).pow(bitLength));
        System.out.println("upperBoud: " + upperBound);

        // Generierung zweier großer Primzahlen
        BigInteger p = PrimGenerator.generateRandomPrime(lowerBound, upperBound, 20);
        System.out.println("p: " + p);
        System.out.println("----");
        BigInteger q = PrimGenerator.generateRandomPrime(lowerBound, upperBound, 20);
        System.out.println("q: " + q);
        System.out.println("----");

        //n = new BigInteger("17919713328749531059679579059882654804055347423360948013871804491276390638725758869030759538024079438476111768651186675130277679621240389166571792891651228901025530035633166530257946180427862022390348215341370677446604393430523135058041112294047225443184297512135984454380609476551106950776493691294184621176914639973921349707044879942807789811786823252993497079137691499583110801566326703151788262775887888379529921057376951487536882809196486192456918844901276494977670646758089009067085974810877203637063938984538814039150840526597446511971652509821941698265541097749579146402395266584458935555359548932868085620347");
        n = p.multiply(q); // Berechnung des Modulus
        System.out.println("n: " + n);
        System.out.println("----");

        BigInteger phi = (p.subtract(BigInteger.ONE)).multiply(q.subtract(BigInteger.ONE)); // Berechnung von phi(n)

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

        saveKeysToFile();
    }

    private static void saveKeysToFile() throws IOException {
        try (PrintWriter outE = new PrintWriter(new FileWriter(E_FILE));
             PrintWriter outN = new PrintWriter(new FileWriter(N_FILE));
             PrintWriter outD = new PrintWriter(new FileWriter(D_FILE))) {
            outE.println(e);
            outN.println(n);
            outD.println(d);
        }
    }

    public static List<BigInteger> textToBigIntegerBlocks(String text, BigInteger n) {
        List<BigInteger> blocks = new ArrayList<>();
        int BLOCK_SIZE = (n.bitLength() / 8) - 1;
        for (int i = 0; i < text.length(); i += BLOCK_SIZE) {
            String block = text.substring(i, Math.min(i + BLOCK_SIZE, text.length()));
            byte[] blockBytes = block.getBytes(StandardCharsets.UTF_8);
            blocks.add(new BigInteger(1, blockBytes));
        }
        return blocks;
    }

    public static void loadKeysFromFiles() throws IOException {
        try (BufferedReader brE = new BufferedReader(new FileReader(E_FILE));
             BufferedReader brN = new BufferedReader(new FileReader(N_FILE));
             BufferedReader brD = new BufferedReader(new FileReader(D_FILE))) {
            e = new BigInteger(brE.readLine());
            n = new BigInteger(brN.readLine());
            d = new BigInteger(brD.readLine());
        }
    }

    public static BigInteger getPublicKey() {
        return e;
    }

    public static BigInteger getModulus() {
        return n;
    }

    public static BigInteger getPrivateKey() {
        return d;
    }

    public static void main(String[] args) {
        try {
            File eFile = new File(E_FILE);
            File nFile = new File(N_FILE);
            File dFile = new File(D_FILE);

            if (!eFile.exists() || !nFile.exists() || !dFile.exists()) {
                System.out.println("Schlüsseldateien nicht gefunden. Generiere neue Schlüssel...");
                generateAndSaveKeys(3404);
            } else {
                System.out.println("Lade vorhandene Schlüssel...");
                loadKeysFromFiles();
            }
            // Schlüssel ausgeben
            System.out.println("Öffentlicher Schlüssel (e): " + getPublicKey());
            System.out.println("Modulus (n): " + getModulus());
            System.out.println("Privater Schlüssel (d): " + getPrivateKey());
        } catch (Exception e){
            System.out.println("Fehler: " + e.getMessage());
        }
    }
}