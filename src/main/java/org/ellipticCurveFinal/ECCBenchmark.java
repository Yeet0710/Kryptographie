package org.ellipticCurveFinal;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

/**
 * Benchmark für ECC mit Block-Chiffrierung.
 */
public class ECCBenchmark {
    public static void main(String[] args) {
        final int iterations = 100;
        String testText;

        // 1. Testdaten laden
        try (InputStream is = ECCBenchmark.class.getClassLoader().getResourceAsStream("eingabe")) {
            if (is == null) {
                System.err.println("Ressource 'eingabe' nicht gefunden im Klassenpfad.");
                return;
            }
            byte[] data = is.readAllBytes();
            testText = new String(data, StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.err.println("Fehler beim Lesen der Ressource 'eingabe': " + e.getMessage());
            return;
        }

        List<Long> genTimes = new ArrayList<>();
        List<Long> encTimes = new ArrayList<>();
        List<Long> decTimes = new ArrayList<>();
        List<Long> totalTimes = new ArrayList<>();

        // 2. Kurve initialisieren
        int bitLength = 256;
        int mrIterations = 20;
        SecureFiniteFieldEllipticCurve secureCurve = new SecureFiniteFieldEllipticCurve(bitLength, mrIterations);
        FiniteFieldEllipticCurve curve = secureCurve.getCurve();
        BigInteger p = curve.getP();
        BigInteger q = secureCurve.getQ();
        ECPoint G = curve.findGenerator(q);

        // 3. Schlüsselpaar erzeugen
        SecureRandom random = new SecureRandom();
        BigInteger d;
        do {
            d = new BigInteger(q.bitLength(), random);
        } while (d.compareTo(BigInteger.ONE) < 0 || d.compareTo(q) >= 0);
        ECPoint Q = G.multiply(d, curve);

        // 4. Benchmark-Schleife
        for (int i = 0; i < iterations; i++) {
            long startIter = System.currentTimeMillis();

            // 4a. Generator-Findung
            long startGen = System.currentTimeMillis();
            curve.findGenerator(q);
            long endGen = System.currentTimeMillis();
            genTimes.add(endGen - startGen);

            // 4b. Verschlüsselung
            long startEnc = System.currentTimeMillis();
            ECCBlockCipher.CipherResult res = ECCBlockCipher.encrypt(
                    testText, G, Q, p, q, curve
            );
            long endEnc = System.currentTimeMillis();
            encTimes.add(endEnc - startEnc);

            // 4c. Entschlüsselung
            long startDec = System.currentTimeMillis();
            ECCBlockCipher.decrypt(
                    res.cipherText, res.Rx, res.Ry, d, p, curve
            );
            long endDec = System.currentTimeMillis();
            decTimes.add(endDec - startDec);

            long endIter = System.currentTimeMillis();
            totalTimes.add(endIter - startIter);
        }

        // 5. Statistik berechnen
        double avgGen = genTimes.stream().mapToLong(Long::longValue).average().orElse(0);
        double avgEnc = encTimes.stream().mapToLong(Long::longValue).average().orElse(0);
        double avgDec = decTimes.stream().mapToLong(Long::longValue).average().orElse(0);
        double avgTotal = totalTimes.stream().mapToLong(Long::longValue).average().orElse(0);
        double stdGen = Math.sqrt(genTimes.stream().mapToDouble(t -> Math.pow(t - avgGen, 2)).average().orElse(0));
        double stdEnc = Math.sqrt(encTimes.stream().mapToDouble(t -> Math.pow(t - avgEnc, 2)).average().orElse(0));
        double stdDec = Math.sqrt(decTimes.stream().mapToDouble(t -> Math.pow(t - avgDec, 2)).average().orElse(0));
        double stdTotal = Math.sqrt(totalTimes.stream().mapToDouble(t -> Math.pow(t - avgTotal, 2)).average().orElse(0));

        // 6. Ausgabe
        System.out.println("******** ECC Benchmark mit Block-Chiffrierung ********");
        System.out.println("Iterationen          : " + iterations);
        System.out.printf("Generator-Findung    : %.2f ms (Std.Dev: %.2f ms)\n", avgGen, stdGen);
        System.out.printf("Verschlüsselung      : %.2f ms (Std.Dev: %.2f ms)\n", avgEnc, stdEnc);
        System.out.printf("Entschlüsselung      : %.2f ms (Std.Dev: %.2f ms)\n", avgDec, stdDec);
        System.out.printf("Komplette Durchlauf   : %.2f ms (Std.Dev: %.2f ms)\n", avgTotal, stdTotal);
    }
}
