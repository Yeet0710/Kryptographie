package org.ellipticCurveFinal;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Scanner;

/**
 * ECCConsoleApp: Demonstriert Verschlüsselung und Entschlüsselung.
 */
public class ECCConsoleApp {

    public static void main(String[] args) {
        try {
            // 1. Kurve und Schlüssel generieren
            int bitLength = 256;
            int millerRabinIterations = 20;
            SecureFiniteFieldEllipticCurve secureCurve =
                    new SecureFiniteFieldEllipticCurve(bitLength, millerRabinIterations);
            FiniteFieldEllipticCurve curve = secureCurve.getCurve();
            BigInteger p = curve.getP();
            BigInteger q = secureCurve.getQ();

            ECPoint G = curve.findGenerator(q);
            // privater Schlüssel d
            SecureRandom random = new SecureRandom();
            BigInteger d;
            do {
                d = new BigInteger(q.bitLength(), random);
            } while (d.compareTo(BigInteger.ONE) < 0 || d.compareTo(q) >= 0);
            ECPoint Q = G.multiply(d, curve);

            // 2. Nachricht einlesen
            Scanner scanner = new Scanner(System.in);
            System.out.println("Gib die Nachricht ein oder Datei lesen? (j/n)");
            String choice = scanner.nextLine().trim();
            String plaintext;
            if (choice.equalsIgnoreCase("j")) {
                System.out.println("Dateipfad:");
                plaintext = readFileAsString(scanner.nextLine().trim());
            } else {
                System.out.println("Nachricht:\n");
                plaintext = scanner.nextLine();
            }

            // 3. Verschlüsselung
            long startEnc = System.currentTimeMillis();
            ECCBlockCipher.CipherResult encrypted = ECCBlockCipher.encrypt(
                    plaintext, G, Q, p, q, curve
            );
            long endEnc = System.currentTimeMillis();
            System.out.println("--- Verschlüsselt ---");
            System.out.println("Ciphertext: " + encrypted.cipherText);
            System.out.println("Q.x: " + Q.getX());
            System.out.println("Q.y: " + Q.getY());
            System.out.println("p: " + p);
            System.out.println("G.x: " + G.getX());
            System.out.println("G.Y: " + G.getY());
            System.out.println("Verschlüsselungszeit: " + (endEnc - startEnc) + " ms");

            // 4. Entschlüsselung
            long startDec = System.currentTimeMillis();
            String decrypted = ECCBlockCipher.decrypt(
                    encrypted.cipherText,
                    encrypted.Rx,
                    encrypted.Ry,
                    d,
                    p,
                    curve
            );
            long endDec = System.currentTimeMillis();
            System.out.println("--- Entschlüsselt ---");
            System.out.println(decrypted);
            System.out.println("Entschlüsselungszeit: " + (endDec - startDec) + " ms");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Liest Dateiinhalt als UTF-8-String.
     */
    private static String readFileAsString(String filePath) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append(System.lineSeparator());
            }
        }
        return sb.toString();
    }
}
