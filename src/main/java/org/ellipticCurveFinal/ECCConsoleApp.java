package org.ellipticCurveFinal;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Scanner;

public class ECCConsoleApp {

    public static void main(String[] args) {
        try {
            // 1. Erzeuge eine sichere elliptische Kurve gemäß der Durchführungsverordnung.
            // Hierbei wird p generiert, sodass p ≡ 5 mod 8 gilt, und q = N/8 (prim) berechnet.
            int bitLength = 16; // Bitlänge von p (anpassbar, für Testzwecke)
            int millerRabinIterations = 20;
            SecureFiniteFieldEllipticCurve secureCurve = new SecureFiniteFieldEllipticCurve(bitLength, millerRabinIterations);
            FiniteFieldEllipticCurve curve = secureCurve.getCurve();
            System.out.println("Generierte Kurve: Z_p mit p = " + curve.getP());
            System.out.println("Berechnete Untergruppenordnung q = " + secureCurve.getQ());

            // 2. Wähle einen Basispunkt G aus der Kurve (ersten gefundenen gültigen Punkt, nicht unendlich).
            ECPoint G = curve.findGenerator();
            System.out.println("Basispunkt G: " + G);

            // 3. Schlüsselgenerierung:
            // Erzeuge einen privaten Schlüssel d (zufällig in [1, q-1]) und berechne den öffentlichen Schlüssel Q = d * G.
            SecureRandom random = new SecureRandom();
            BigInteger q = secureCurve.getQ();
            BigInteger d;
            do {
                d = new BigInteger(q.bitLength(), random);
            } while (d.compareTo(BigInteger.ONE) < 0 || d.compareTo(q) >= 0);
            ECPoint Q = G.multiply(d, curve);
            System.out.println("Private Key d: " + d);
            System.out.println("Public Key Q: " + Q);

            // 4. Einlesen der Nachricht: Benutzer kann wählen, ob der Text aus einer Datei eingelesen werden soll
            Scanner scanner = new Scanner(System.in);
            String plaintext = "";
            System.out.println("Soll der zu verschlüsselnde Text aus einer Datei eingelesen werden? (j/n)");
            String choice = scanner.nextLine();
            if (choice.trim().equalsIgnoreCase("j")) {
                System.out.println("Gib den Dateipfad ein:");
                String filePath = scanner.nextLine();
                plaintext = readFileAsString(filePath);
            } else {
                System.out.println("Gib die Nachricht zum Verschlüsseln ein:");
                plaintext = scanner.nextLine();
            }
            byte[] plaintextBytes = plaintext.getBytes("UTF-8");

            Long startTimeV = System.currentTimeMillis();
            // 5. Erzeuge einen ephemeral Schlüssel k (zufällig in [1, q-1]).
            BigInteger k;
            do {
                k = new BigInteger(q.bitLength(), random);
            } while (k.compareTo(BigInteger.ONE) < 0 || k.compareTo(q) >= 0);
            // Berechne R = k * G und den gemeinsamen Geheimwert: S = k * Q.
            ECPoint R = G.multiply(k, curve);
            ECPoint sharedSecretEnc = Q.multiply(k, curve); // k * Q
            BigInteger sharedSecretX = sharedSecretEnc.getX();
            // Verwende die x-Koordinate des gemeinsamen Geheimnisses als symmetrischen Schlüssel (in Byteform).
            byte[] keyBytes = sharedSecretX.toByteArray();
            // Verschlüssele via einfacher XOR-Verschlüsselung.
            byte[] ciphertextBytes = xorBytes(plaintextBytes, keyBytes);
            System.out.println("Ciphertext (hex): " + bytesToHex(ciphertextBytes));
            System.out.println("Ephemeral public value R: " + R);
            Long endTimeV = System.currentTimeMillis();

            // 6. Entschlüsselung:
            // Empfänger berechnet S' = d * R (sollte identisch mit S = k * Q sein).
            Long startTimeE = System.currentTimeMillis();
            ECPoint sharedSecretDec = R.multiply(d, curve);
            BigInteger sharedSecretXDec = sharedSecretDec.getX();
            byte[] keyBytesDec = sharedSecretXDec.toByteArray();
            byte[] decryptedBytes = xorBytes(ciphertextBytes, keyBytesDec);
            String decryptedMessage = new String(decryptedBytes, "UTF-8");
            System.out.println("Decrypted message: " + decryptedMessage);
            Long endTimeE = System.currentTimeMillis();
            System.out.println("Verschlüsselungszeit: " + (endTimeV - startTimeV) + " ms");
            System.out.println("Entschlüsselungszeit: " + (endTimeE - startTimeE) + " ms");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Liest den Inhalt einer Datei (inklusive Zeilenumbrüche) und gibt ihn als String zurück.
     */
    private static String readFileAsString(String filePath) throws IOException {
        StringBuilder contentBuilder = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String sCurrentLine;
            while ((sCurrentLine = br.readLine()) != null) {
                contentBuilder.append(sCurrentLine).append(System.lineSeparator());
            }
        }
        return contentBuilder.toString();
    }

    /**
     * Hilfsmethode: XOR-Verschlüsselung/Entschlüsselung.
     * Wiederholt den Schlüssel, falls notwendig.
     */
    private static byte[] xorBytes(byte[] data, byte[] key) {
        byte[] output = new byte[data.length];
        for (int i = 0; i < data.length; i++) {
            output[i] = (byte) (data[i] ^ key[i % key.length]);
        }
        return output;
    }

    /**
     * Hilfsmethode: Wandelt ein Byte-Array in eine Hexadezimaldarstellung um.
     */
    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }
}
