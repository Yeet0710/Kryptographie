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


public class RSAUtils {
    private static final SecureRandom random = new SecureRandom();
    private static final int DEFAULT_MR_ITERATIONS = 20;

    // Schlüsseldateien für Alice
    private static final String E_FILE_ALICE = "rsa_e.txt";
    private static final String N_FILE_ALICE = "rsa_n.txt";
    private static final String D_FILE_ALICE = "rsa_d.txt";

    // Schlüsseldateien für Bob
    private static final String E_FILE_BOB = "bob_e.txt";
    private static final String N_FILE_BOB = "bob_n.txt";
    private static final String D_FILE_BOB = "bob_d.txt";

    private static BigInteger eAlice, nAlice, dAlice;
    private static BigInteger eBob, nBob, dBob;

    /**
     * Generiert und speichert RSA-Schlüssel (e, n, d) in Dateien.
     * Misst dabei die Zeit, die für die Primzahlerzeugung benötigt wird.
     * @param eFile      Dateiname für e
     * @param nFile      Dateiname für n
     * @param dFile      Dateiname für d
     * @param bitLength  gewünschte Bitlänge (z.B. 1024, 2048)
     * @return           Array {n, ZeitPrimP, ZeitPrimQ, GesamtzeitMillis}
     * @throws IOException
     */
    public static void generateAndSaveKeys(String eFile, String nFile, String dFile, int bitLength) throws IOException {
        BigInteger lowerBound = schnelleExponentiation.schnelleExponentiation(BigInteger.TWO, BigInteger.valueOf(bitLength / 2 - 1), BigInteger.valueOf(2).pow(bitLength));
        BigInteger upperBound = schnelleExponentiation.schnelleExponentiation(BigInteger.TWO, BigInteger.valueOf(bitLength / 2), BigInteger.valueOf(2).pow(bitLength));

        System.out.println("---- Starte RSA-Schlüsselgenerierung (" + bitLength + " Bit) ----");

        // 1) p erzeugen (mit Zeitmessung)
        System.out.print("Erzeuge p (Primzahl) ... ");
        long startP = System.currentTimeMillis();
        BigInteger p = PrimGenerator.generateRandomPrime(lowerBound, upperBound, DEFAULT_MR_ITERATIONS);
        long timeP = System.currentTimeMillis() - startP;
        System.out.println("fertig (" + timeP + " ms). p hat Bitlänge = " + p.bitLength());

        // 2) q erzeugen (mit Zeitmessung)
        System.out.print("Erzeuge q (Primzahl) ... ");
        long startQ = System.currentTimeMillis();
        BigInteger q = PrimGenerator.generateRandomPrime(lowerBound, upperBound, DEFAULT_MR_ITERATIONS);
        long timeQ = System.currentTimeMillis() - startQ;
        System.out.println("fertig (" + timeQ + " ms). q hat Bitlänge = " + q.bitLength());

        // 3) n und phi(n) berechnen
        BigInteger n = p.multiply(q);
        BigInteger phi = (p.subtract(BigInteger.ONE)).multiply(q.subtract(BigInteger.ONE));

        System.out.println("Berechne n = p * q; n hat Bitlänge = " + n.bitLength());
        System.out.println("Berechne φ(n) = (p-1)*(q-1)");

        // 4) e wählen (z.B. zufällig mit (bitLength/2) Bit Länge), solange gcd(e, phi) != 1
        System.out.print("Wähle e, sodass gcd(e, φ(n)) = 1 ... ");
        BigInteger e;
        do {
            e = new BigInteger(bitLength / 2, random);
        } while (!erweiterterEuklid.sindTeilerfremd(phi, e));

        // 5) d = e^(-1) mod φ(n) berechnen
        BigInteger d = erweiterterEuklid.erweiterterEuklid(e, phi)[1].mod(phi).add(phi).mod(phi);

        // 6) Schlüssel in Dateien speichern
        saveKeysToFile(e, n, d, eFile, nFile, dFile);
        System.out.println("Schlüssel gespeichert in Dateien: ");
        System.out.println("  " + eFile + ", " + nFile + ", " + dFile);
        System.out.println("---- Schlüsselgenerierung beendet ----\n");
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

    // Lädt Alice's und Bob's Schlüssel aus Dateien
    public static void loadKeysFromFiles() throws IOException {
        eAlice = loadKey(E_FILE_ALICE);
        nAlice = loadKey(N_FILE_ALICE);
        dAlice = loadKey(D_FILE_ALICE);

        eBob = loadKey(E_FILE_BOB);
        nBob = loadKey(N_FILE_BOB);
        dBob = loadKey(D_FILE_BOB);
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

    // Getter für Bob
    public static BigInteger getBobPublicKey() {
        return eBob;
    }

    public static BigInteger getBobModulus() {
        return nBob;
    }

    public static BigInteger getBobPrivateKey() {
        return dBob;
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
        BigInteger decryptedHash = schnelleExponentiation.schnelleExponentiation(signature, eBob, nBob);
        return hash.equals(decryptedHash);
    }

    public static void main(String[] args) {
        try {
            File eFileAlice = new File(E_FILE_ALICE);
            File nFileAlice = new File(N_FILE_ALICE);
            File dFileAlice = new File(D_FILE_ALICE);

            File eFileBob = new File(E_FILE_BOB);
            File nFileBob = new File(N_FILE_BOB);
            File dFileBob = new File(D_FILE_BOB);

            // Schlüssel für Alice generieren, falls nicht vorhanden
            if (!eFileAlice.exists() || !nFileAlice.exists() || !dFileAlice.exists()) {
                System.out.println("Alice's Schlüssel nicht gefunden. Generiere neue Schlüssel...");
                generateAndSaveKeys(E_FILE_ALICE, N_FILE_ALICE, D_FILE_ALICE, 1024);
            }

            // Schlüssel für Bob generieren, falls nicht vorhanden
            if (!eFileBob.exists() || !nFileBob.exists() || !dFileBob.exists()) {
                System.out.println("Bob's Schlüssel nicht gefunden. Generiere neue Schlüssel...");
                generateAndSaveKeys(E_FILE_BOB, N_FILE_BOB, D_FILE_BOB, 1024);
            }

            // Schlüssel laden
            loadKeysFromFiles();

            // Schlüssel ausgeben
            System.out.println("Alice's Öffentlicher Schlüssel (e): " + getAlicePublicKey());
            System.out.println("Alice's Modulus (n): " + getAliceModulus());
            System.out.println("Alice's Privater Schlüssel (d): " + getAlicePrivateKey());
            System.out.println("----------");

            System.out.println("Bob's Öffentlicher Schlüssel (e): " + getBobPublicKey());
            System.out.println("Bob's Modulus (n): " + getBobModulus());
            System.out.println("Bob's Privater Schlüssel (d): " + getBobPrivateKey());

            // Beispiel: Signatur und Verifikation
            String message = "abcd";
            System.out.println("\n== Test-Signatur & Verifikation ==");
            System.out.println("Nachricht: \"" + message + "\"");

            long startSign = System.currentTimeMillis();
            BigInteger signature = sign(message);
            long timeSign = System.currentTimeMillis() - startSign;
            System.out.println("Signatur: " + signature);
            System.out.println("Signatur‐Dauer: " + timeSign + " ms");

            long startVer = System.currentTimeMillis();
            boolean isValid = verify(message, signature);
            long timeVer = System.currentTimeMillis() - startVer;
            System.out.println("Verifikation erfolgreich? " + isValid);
            System.out.println("Verifikations‐Dauer: " + timeVer + " ms");
        } catch (Exception e) {
            System.out.println("Fehler: " + e.getMessage());
        }
    }
}
