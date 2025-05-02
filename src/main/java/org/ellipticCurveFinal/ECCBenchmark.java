package org.ellipticCurveFinal;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

public class ECCBenchmark {
    public static void main(String[] args) {
        final int iterations = 100;
        byte[] testData;

        // Lade Testdaten aus Ressourcen-Datei "eingabe"
        try (InputStream is = ECCBenchmark.class.getClassLoader().getResourceAsStream("eingabe")) {
            if (is == null) {
                System.err.println("Ressource 'eingabe' nicht gefunden im Klassenpfad.");
                return;
            }
            testData = is.readAllBytes();
        } catch (IOException e) {
            System.err.println("Fehler beim Lesen der Ressource 'eingabe': " + e.getMessage());
            return;
        }

        List<Long> genTimes = new ArrayList<>();
        List<Long> encTimes = new ArrayList<>();
        List<Long> decTimes = new ArrayList<>();
        List<Long> totalTimes = new ArrayList<>();

        // 1. Curve und Generator
        int bitLength = 1024;  // anpassbar
        int mrIterations = 20;
        SecureFiniteFieldEllipticCurve secureCurve = new SecureFiniteFieldEllipticCurve(bitLength, mrIterations);
        FiniteFieldEllipticCurve curve = secureCurve.getCurve();
        BigInteger q = secureCurve.getQ();
        ECPoint G = curve.findGenerator(q);

        // 2. Schlüsselgenerierung
        SecureRandom random = new SecureRandom();
        BigInteger d;
        do {
            d = new BigInteger(q.bitLength(), random);
        } while (d.compareTo(BigInteger.ONE) < 0 || d.compareTo(q) >= 0);
        ECPoint Q = G.multiply(d, curve);

        // 3. Benchmark-Schleife
        for (int i = 0; i < iterations; i++) {
            long startIter = System.currentTimeMillis();
            // 3a. Generator-Findung
            long startGen = System.currentTimeMillis();
            curve.findGenerator(q);
            long endGen = System.currentTimeMillis();
            genTimes.add(endGen - startGen);

            // 3b. Verschlüsselung
            long startEnc = System.currentTimeMillis();
            BigInteger k;
            do {
                k = new BigInteger(q.bitLength(), random);
            } while (k.compareTo(BigInteger.ONE) < 0 || k.compareTo(q) >= 0);
            ECPoint R = G.multiply(k, curve);
            ECPoint sharedEnc = Q.multiply(k, curve);
            byte[] keyEnc = sharedEnc.getX().toByteArray();
            byte[] cipher = xorBytes(testData, keyEnc);
            long endEnc = System.currentTimeMillis();
            encTimes.add(endEnc - startEnc);

            // 3c. Entschlüsselung
            long startDec = System.currentTimeMillis();
            ECPoint sharedDec = R.multiply(d, curve);
            byte[] keyDec = sharedDec.getX().toByteArray();
            byte[] plain = xorBytes(cipher, keyDec);
            long endDec = System.currentTimeMillis();
            decTimes.add(endDec - startDec);

            long endIter = System.currentTimeMillis();
            totalTimes.add(endIter - startIter);
        }

        // 4. Statistik
        double avgGen = genTimes.stream().mapToLong(Long::longValue).average().orElse(0);
        double avgEnc = encTimes.stream().mapToLong(Long::longValue).average().orElse(0);
        double avgDec = decTimes.stream().mapToLong(Long::longValue).average().orElse(0);
        double avgTotal = totalTimes.stream().mapToLong(Long::longValue).average().orElse(0);

        double stdGen = Math.sqrt(genTimes.stream().mapToDouble(t -> Math.pow(t - avgGen, 2)).average().orElse(0));
        double stdEnc = Math.sqrt(encTimes.stream().mapToDouble(t -> Math.pow(t - avgEnc, 2)).average().orElse(0));
        double stdDec = Math.sqrt(decTimes.stream().mapToDouble(t -> Math.pow(t - avgDec, 2)).average().orElse(0));
        double stdTotal = Math.sqrt(totalTimes.stream().mapToDouble(t -> Math.pow(t - avgTotal, 2)).average().orElse(0));

        // 5. Ausgabe
        System.out.println("******** ECC Benchmark ********");
        System.out.println("Iterationen           : " + iterations);
        System.out.printf("Generator-Findung     : %.2f ms (Std.Dev: %.2f ms)\n", avgGen, stdGen);
        System.out.printf("Verschlüsselung       : %.2f ms (Std.Dev: %.2f ms)\n", avgEnc, stdEnc);
        System.out.printf("Entschlüsselung       : %.2f ms (Std.Dev: %.2f ms)\n", avgDec, stdDec);
        System.out.printf("Komplette Durchführung : %.2f ms (Std.Dev: %.2f ms)\n", avgTotal, stdTotal);
    }

    private static byte[] xorBytes(byte[] data, byte[] key) {
        byte[] out = new byte[data.length];
        for (int i = 0; i < data.length; i++) {
            out[i] = (byte) (data[i] ^ key[i % key.length]);
        }
        return out;
    }
}
