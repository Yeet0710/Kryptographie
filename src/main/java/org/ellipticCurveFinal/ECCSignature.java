package org.ellipticCurveFinal;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * Klasse für das Elliptic Curve Digital Signature Algorithm (ECDSA) nach Algorithmus 3.5 (Seite 73).
 */
public class ECCSignature {

    /**
     * Container für eine Signatur (r, s).
     */
    public static class Signature {
        public final BigInteger r;
        public final BigInteger s;

        public Signature(BigInteger r, BigInteger s) {
            this.r = r;
            this.s = s;
        }

        @Override
        public String toString() {
            return "(" + r.toString() + ", " + s.toString() + ")";
        }
    }

    /**
     * Signiert eine Nachricht M mit privatem Schlüssel x, Parameter q, Generator g und Kurve.
     * Entsprechend K2-Algorithmus 3.5
     *
     * @param message     Die zu signierende Nachricht (als UTF-8-String).
     * @param privateKey  Privater Schlüssel x ∈ ℤ_q.
     * @param q           Ordnung der Untergruppe H (BigInteger).
     * @param generator   Generatorpunkt g ∈ H ⊆ E(ℤ_p).
     * @param curve       Die zugehörige FiniteFieldEllipticCurve.
     * @return Signature-Objekt mit (r, s).
     */
    public static Signature sign(String message,
                                 BigInteger privateKey,
                                 BigInteger q,
                                 ECPoint generator,
                                 FiniteFieldEllipticCurve curve) {
        SecureRandom rnd = new SecureRandom();
        BigInteger r, s = null, k;
        BigInteger h;

        try {
            // 1) Hashwert h = SHA-1(M) mod q
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] hBytes = md.digest(message.getBytes(StandardCharsets.UTF_8));
            h = new BigInteger(1, hBytes).mod(q);
            System.out.println("DEBUG: sign() -> h = " + h);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-1 nicht verfügbar", e);
        }

        do {
            // 2) Zufällige Zahl k ∈ ℤ_q\{0}
            do {
                k = new BigInteger(q.bitLength(), rnd);
            } while (k.compareTo(BigInteger.ZERO) <= 0 || k.compareTo(q) >= 0);

            // 3) (u, v) = k·g
            ECPoint point = generator.multiply(k, curve).normalize(curve);
            BigInteger u = point.getX();
            r = u.mod(q);
            if (r.equals(BigInteger.ZERO)) {
                continue; // Wenn r == 0, neu wählen
            }

            // 4) k^{-1} mod q
            BigInteger kInv = k.modInverse(q);

            // 5) s = (h + x·r)·k^{-1} mod q
            s = (h.add(privateKey.multiply(r))).multiply(kInv).mod(q);
            // Wenn s == 0, erneut k wählen
        } while (s.equals(BigInteger.ZERO));

        System.out.println("Signieren: " + s + ", " + r);

        return new Signature(r, s);
    }

    /**
     * Verifiziert eine ECDSA-Signatur (r, s) zur Nachricht M mit öffentlichem Schlüssel y, Ordnung q, Generator g und Kurve.
     * Entsprechend K2-Algorithmus 3.5
     *
     * @param message     Die signierte Nachricht (als UTF-8-String).
     * @param sig         Die Signatur (r, s).
     * @param publicKey   Öffentlicher Schlüssel y = x·g.
     * @param q           Ordnung der Untergruppe H (BigInteger).
     * @param generator   Generatorpunkt g ∈ H ⊆ E(ℤ_p).
     * @param curve       Die zugehörige FiniteFieldEllipticCurve.
     * @return true, falls Verifikation erfolgreich (u' ≡ r mod q), sonst false.
     */
    public static boolean verify(String message,
                                 Signature sig,
                                 ECPoint publicKey,
                                 BigInteger q,
                                 ECPoint generator,
                                 FiniteFieldEllipticCurve curve) {
        BigInteger r = sig.r;
        BigInteger s = sig.s;

        // 1) Prüfen, ob 1 ≤ r, s ≤ q−1
        if (r.compareTo(BigInteger.ONE) < 0 || r.compareTo(q.subtract(BigInteger.ONE)) > 0) {
            return false;
        }
        if (s.compareTo(BigInteger.ONE) < 0 || s.compareTo(q.subtract(BigInteger.ONE)) > 0) {
            return false;
        }

        BigInteger h;
        try {
            // 2) Hashwert h = SHA-1(M) mod q
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] hBytes = md.digest(message.getBytes(StandardCharsets.UTF_8));
            h = new BigInteger(1, hBytes).mod(q);
            System.out.println("DEBUG: verify() -> h = " + h);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-1 nicht verfügbar", e);
        }

        // 3) w = s^{-1} mod q
        BigInteger w = s.modInverse(q);

        // 4) u1 = h·w mod q, u2 = r·w mod q
        BigInteger u1 = h.multiply(w).mod(q);
        BigInteger u2 = r.multiply(w).mod(q);

        // 5) (u', v') = u1·g + u2·y
        ECPoint p1 = generator.multiply(u1, curve).normalize(curve);
        ECPoint p2 = publicKey.multiply(u2, curve).normalize(curve);
        ECPoint sum = p1.add(p2, curve).normalize(curve);

        // Wenn Summe Punkt im Unendlichen ist, kann keine gültige r berechnet werden
        if (sum instanceof InfinitePoint) {
            return false;
        }

        // 6) u' = sum.getX() mod q
        BigInteger uPrime = sum.getX().mod(q);

        System.out.println("Verifizieren: " + uPrime + ", " + r);
        System.out.println(uPrime.equals(r));

        // 7) Verifikation: u' ≡ r mod q
        return uPrime.equals(r);
    }
}
