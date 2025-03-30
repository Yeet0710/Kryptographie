package org.scrum1_6;

import org.scrum1_1.PrimGenerator;
import org.scrum1_3.schnelleExponentiation;
import org.scrum1_4.erweiterterEuklid;

import java.io.*;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;


public class RSAUtils2047 {
    private static final SecureRandom random = new SecureRandom();

    // Schlüsseldateien für Alice
    private static final String E_FILE_ALICE = "rsa2047_e.txt";
    private static final String N_FILE_ALICE = "rsa2047_n.txt";
    private static final String D_FILE_ALICE = "rsa2047_d.txt";

    private static BigInteger eAlice, nAlice, dAlice;


    // Schlüsselgenerierung für Alice
    public static void generateAndSaveKeys(String eFile, String nFile, String dFile, int bitLength) throws IOException {
        BigInteger lowerBound = schnelleExponentiation.schnelleExponentiation(BigInteger.TWO, BigInteger.valueOf(bitLength / 2 - 1), BigInteger.valueOf(2).pow(bitLength));
        BigInteger upperBound = schnelleExponentiation.schnelleExponentiation(BigInteger.TWO, BigInteger.valueOf(bitLength / 2), BigInteger.valueOf(2).pow(bitLength));

        BigInteger p = PrimGenerator.generateRandomPrime(lowerBound, upperBound, 20);
        BigInteger q = PrimGenerator.generateRandomPrime(lowerBound, upperBound, 20);

        BigInteger n = p.multiply(q);
        BigInteger phi = (p.subtract(BigInteger.ONE)).multiply(q.subtract(BigInteger.ONE));

        BigInteger e;
        do {
            e = new BigInteger(bitLength / 2, random);
        } while (!erweiterterEuklid.sindTeilerfremd(phi, e));

        BigInteger d = erweiterterEuklid.erweiterterEuklid(e, phi)[1].mod(phi).add(phi).mod(phi);

        saveKeysToFile(e, n, d, eFile, nFile, dFile);
    }

    // Speichert Schlüssel in Datei
    private static void saveKeysToFile(BigInteger e, BigInteger n, BigInteger d, String eFile, String nFile, String dFile) throws IOException {
        try (PrintWriter outE = new PrintWriter(new FileWriter(eFile));
             PrintWriter outN = new PrintWriter(new FileWriter(nFile));
             PrintWriter outD = new PrintWriter(new FileWriter(dFile))) {
            outE.println(e);
            outN.println(n);
            outD.println(d);
        }
    }

    // Lädt Alice's Schlüssel aus Dateien
    public static void loadKeysFromFiles() throws IOException {
        eAlice = loadKey(E_FILE_ALICE);
        nAlice = loadKey(N_FILE_ALICE);
        dAlice = loadKey(D_FILE_ALICE);
    }

    private static BigInteger loadKey(String filename) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            return new BigInteger(reader.readLine());
        }
    }

    // Getter für Alice
    public static BigInteger getAlicePublicKey() {
        return eAlice;
    }

    public static BigInteger getAliceModulus() {
        return nAlice;
    }

    public static BigInteger getAlicePrivateKey() {
        return dAlice;
    }


    // Hashfunktion für Signaturen
    private static BigInteger hashMessage(String message) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] digest = md.digest(message.getBytes(StandardCharsets.UTF_8));
        return new BigInteger(1, digest);
    }

    // Signieren einer Nachricht mit Alice's Schlüssel
    public static BigInteger sign(String message) throws NoSuchAlgorithmException {
        BigInteger hash = hashMessage(message);
        return schnelleExponentiation.schnelleExponentiation(hash, dAlice, nAlice);
    }

    // Verifikation mit Alice PubKey
    public static boolean verify(String message, BigInteger signature) throws NoSuchAlgorithmException {
        BigInteger hash = hashMessage(message);
        BigInteger decryptedHash = schnelleExponentiation.schnelleExponentiation(signature, eAlice, nAlice);
        return hash.equals(decryptedHash);
    }

    public static void main(String[] args) {
        try {
            File eFileAlice = new File(E_FILE_ALICE);
            File nFileAlice = new File(N_FILE_ALICE);
            File dFileAlice = new File(D_FILE_ALICE);


            // Schlüssel für Alice generieren, falls nicht vorhanden
            if (!eFileAlice.exists() || !nFileAlice.exists() || !dFileAlice.exists()) {
                System.out.println("Alice's Schlüssel nicht gefunden. Generiere neue Schlüssel...");
                generateAndSaveKeys(E_FILE_ALICE, N_FILE_ALICE, D_FILE_ALICE, 2047);
            }

            // Schlüssel laden
            loadKeysFromFiles();

            // Schlüssel ausgeben
            System.out.println("Alice's Öffentlicher Schlüssel (e): " + getAlicePublicKey());
            System.out.println("Alice's Modulus (n): " + getAliceModulus());
            System.out.println("Alice's Privater Schlüssel (d): " + getAlicePrivateKey());
            System.out.println("----------");

            // Test Signatur & Verifikation
            String message = "Hallo RSA";
            BigInteger signature = sign(message);
            System.out.println("\nSignatur: " + signature);

            boolean isValid = verify(message, signature);
            System.out.println("Verifizierung: " + isValid);
        } catch (Exception e) {
            System.out.println("Fehler: " + e.getMessage());
        }
    }
}
