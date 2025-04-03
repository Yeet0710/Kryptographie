package org.ellipticCurve;

import org.scrum1_16.SecPrimGenerator;
import org.scrum1_2.PrimTester;
import org.scrum1_8.EllipticCurveOrderCalculator;

import java.math.BigInteger;
import java.security.SecureRandom;

public class ECCTry {

    public static String EccEncrypt(String Message) {

        BigInteger N;

        while (PrimTester.istPrimzahl(N.divide(BigInteger.valueOf(8)), 20) = false) {
            BigInteger p = SecPrimGenerator.generateSafePrime(BigInteger.valueOf(256), 20); // Generiert sichere Primzahl

            // als summe zweier quadrate darstellen: TODO

            N = EllipticCurveOrderCalculator.computeGroupOrder(p);
            System.out.println("Gruppenordnung N= " + N);
        }

        BigInteger r;

        while (BigInteger.modPow(r, (p - 1) / 2, p)) {
            SecureRandom x = new SecureRandom();
            BigInteger r = Math.pow(x, 3) - x;

        }

        // Berechnung von (p-1)/4 und (p+3)/8
        BigInteger exp1 = p.subtract(BigInteger.ONE).divide(BigInteger.valueOf(4));  // (p-1)/4
        BigInteger exp2 = p.add(BigInteger.valueOf(3)).divide(BigInteger.valueOf(8)); // (p+3)/8

        // Prüfen, ob r^((p-1)/4) mod p ≡ 1 oder -1 ist
        BigInteger check = r.modPow(exp1, p);

        BigInteger y;
        if (check.equals(BigInteger.ONE)) {
            // Fall 1: r^((p-1)/4) ≡ 1 (mod p)
            y = r.modPow(exp2, p);
            System.out.println("Fall 1: y = " + y);
        } else if (check.equals(p.subtract(BigInteger.ONE))) {
            // Fall 2: r^((p-1)/4) ≡ -1 (mod p), was gleichbedeutend mit p-1 ist (weil mod p)
            BigInteger factor = p.add(BigInteger.ONE).divide(BigInteger.TWO); // (p+1)/2
            BigInteger fourR = r.multiply(BigInteger.valueOf(4)).mod(p); // 4r mod p
            y = factor.multiply(fourR.modPow(exp2, p)).mod(p); // ((p+1)/2) * (4r)^((p+3)/8) mod p
            System.out.println("Fall 2: y = " + y);
        }



        return Message;
    }
}
