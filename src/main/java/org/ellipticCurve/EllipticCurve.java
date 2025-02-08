package org.ellipticCurve;

import java.math.BigInteger;

public class EllipticCurve {
    private final BigInteger a, b, p; // Parameter der Kurve

    public EllipticCurve(BigInteger a, BigInteger b, BigInteger p) {
        this.a = a;
        this.b = b;
        this.p = p;
    }

    // Klasse für einen Punkt auf der elliptischen Kurve
    public static class Point {
        BigInteger x, y;
        boolean isInfinity;

        public Point(BigInteger x, BigInteger y) {
            this.x = x;
            this.y = y;
            this.isInfinity = false;
        }

        public static Point infinity() {
            Point inf = new Point(BigInteger.ZERO, BigInteger.ZERO);
            inf.isInfinity = true;
            return inf;
        }

        public boolean isInfinity() {
            return isInfinity;
        }

        @Override
        public String toString() {
            return isInfinity ? "∞" : "(" + x + ", " + y + ")";
        }
    }

    // Punktaddition: P + Q
    public Point add(Point P, Point Q) {
        if (P.isInfinity) return Q;
        if (Q.isInfinity) return P;

        if (P.x.equals(Q.x) && P.y.equals(Q.y.negate().mod(p))) {
            return Point.infinity();
        }

        BigInteger lambda;
        if (P.x.equals(Q.x) && P.y.equals(Q.y)) {
            // Punktverdopplung (Tangentenverfahren)
            BigInteger numerator = P.x.pow(2).multiply(BigInteger.valueOf(3)).add(a);
            BigInteger denominator = P.y.multiply(BigInteger.TWO).modInverse(p);
            lambda = numerator.multiply(denominator).mod(p);
        } else {
            // Punktaddition (Sehnenmethode)
            BigInteger numerator = Q.y.subtract(P.y).mod(p);
            BigInteger denominator = Q.x.subtract(P.x).modInverse(p);
            lambda = numerator.multiply(denominator).mod(p);
        }

        BigInteger x3 = lambda.pow(2).subtract(P.x).subtract(Q.x).mod(p);
        BigInteger y3 = lambda.multiply(P.x.subtract(x3)).subtract(P.y).mod(p);
        return new Point(x3, y3);
    }

    // Skalare Multiplikation k * P durch Montgomery ladder optimiert
    public Point multiply(Point P, BigInteger k) {
        if (k.signum() == 0) {
            return Point.infinity();
        }

        Point R0 = Point.infinity(); // R0 = 0P (Unendlich-Punkt)
        Point R1 = P;                // R1 = 1P

        int bitLength = k.bitLength();
        for (int i = bitLength - 1; i >= 0; i--) {
            if (k.testBit(i)) {
                R0 = add(R0, R1); // R0 = R0 + R1
                R1 = add(R1, R1); // R1 = 2 * R1
            } else {
                R1 = add(R0, R1); // R1 = R0 + R1
                R0 = add(R0, R0); // R0 = 2 * R0
            }
        }

        return R0;
    }


    // Testmethode
    public static void main(String[] args) {
        BigInteger a = BigInteger.valueOf(2);
        BigInteger b = BigInteger.valueOf(3);
        BigInteger p = BigInteger.valueOf(97);  // Primzahlfeld Z_97

        EllipticCurve curve = new EllipticCurve(a, b, p);
        Point P = new Point(BigInteger.valueOf(3), BigInteger.valueOf(6));

        // Skalare Multiplikation 20 * P
        BigInteger k = BigInteger.valueOf(20);
        Point result = curve.multiply(P, k);

        System.out.println("Ergebnis von " + k + " * P: " + result);
    }
}
