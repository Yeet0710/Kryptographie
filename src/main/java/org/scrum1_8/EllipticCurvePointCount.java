package org.scrum1_8;
import java.math.BigInteger;
import java.security.SecureRandom;

public class EllipticCurvePointCount {

    // Methode zur Berechnung der Legendre-Symbol (quadratische Reste)
    /**
     * Berechtung des Legendre-Symbols [n/p]
     * @param n eine ganze Zahl
     * @param p sichere Primzahl
     * @return Eine zufällige Primzahl im gegebenen Bereich
     */
    private static int legendreSymbol(BigInteger n, BigInteger p) {
        BigInteger result = n.modPow(p.subtract(BigInteger.ONE).divide(BigInteger.TWO), p);
        if (result.equals(BigInteger.ZERO)) return 0;  // Kein Rest
        if (result.equals(BigInteger.ONE)) return 1;  // Quadratischer Rest
        return -1; // Kein quadratischer Rest
    }

    /**
     * Berechtung die Anzahl der Punkte auf E(Z_p)
     * @param p sichere Primzahl
     * @param a Element aus Z_p
     * @param b Element aus Z_p
     * @return Eine zufällige Primzahl im gegebenen Bereich
     */
    public static BigInteger countPoints(BigInteger p, BigInteger a, BigInteger b) {
        BigInteger count = BigInteger.ONE; // Start mit 1 für den Punkt O (Unendlichkeitspunkt)

        for (BigInteger x = BigInteger.ZERO; x.compareTo(p) < 0; x = x.add(BigInteger.ONE)) {
            BigInteger rhs = x.pow(3).add(a.multiply(x)).add(b).mod(p); // x^3 + ax + b mod p
            int legendre = legendreSymbol(rhs, p);

            // Prüfe, ob rhs ein quadratischer Rest ist
            if (rhs.equals(BigInteger.ZERO)) {
                count = count.add(BigInteger.ONE); // y = 0 als einzige Lösung
            } else if (legendreSymbol(rhs, p) == 1) {
                count = count.add(BigInteger.TWO); // Zwei Lösungen für y
            }
        }

        return count;
    }

    public static void main(String[] args) {
        BigInteger p = new BigInteger("23"); // Eine sichere Primzahl
        BigInteger a = new BigInteger("1");
        BigInteger b = new BigInteger("1");

        BigInteger N = countPoints(p, a, b);
        System.out.println("Gruppenordnung N von E(Z_p): " + N);
    }
}