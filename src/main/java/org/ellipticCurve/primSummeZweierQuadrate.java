package org.ellipticCurve;

import java.math.BigInteger;

public class primSummeZweierQuadrate {

    private final BigInteger a;
    private final BigInteger b;
    private final BigInteger p;

    public primSummeZweierQuadrate(BigInteger a, BigInteger b, BigInteger p) {
        this.a = a;
        this.b = b;
        this.p = p;
    }

    public static class Punkt {
        BigInteger x, y;
        boolean unendlich; // Repräsentiert das neutrale Element O

        public Punkt(BigInteger x, BigInteger y) {
            this.x = x;
            this.y = y;
            this.unendlich = false;
        }

        public static Punkt infinity() {
            Punkt o = new Punkt(BigInteger.ZERO, BigInteger.ZERO);
            o.unendlich = true;
            return o;
        }

        public boolean isInfinity() {
            return this.unendlich;
        }
    }

    // Sehnen-Tangenten-Verfahren (Punktaddition)
    public Punkt add(Punkt P, Punkt Q) {
        if (P.isInfinity()) return Q;
        if (Q.isInfinity()) return P;

        if (P.x.equals(Q.x) && P.y.equals(Q.y)) {
            return doubleP(P);
        }

        if (P.x.equals(Q.x) && P.y.add(Q.y).mod(p).equals(BigInteger.ZERO)) {
            return Punkt.infinity();
        }

        // Sehnenformel (λ = (y2 - y1) / (x2 - x1) mod p)
        BigInteger lambda = Q.y.subtract(P.y)
                .multiply(Q.x.subtract(P.x).modInverse(p))
                .mod(p);

        BigInteger xr = lambda.multiply(lambda).subtract(P.x).subtract(Q.x).mod(p);
        BigInteger yr = lambda.multiply(P.x.subtract(xr)).subtract(P.y).mod(p);

        return new Punkt(xr, yr);
    }

    // Tangentenmethode (Punktverdopplung)
    public Punkt doubleP(Punkt P) {
        if (P.isInfinity()) return P;

        // Tangentenformel (λ = (3x^2 + a) / (2y) mod p)
        BigInteger lambda = P.x.pow(2).multiply(BigInteger.valueOf(3)).add(a)
                .multiply(P.y.multiply(BigInteger.TWO).modInverse(p))
                .mod(p);

        BigInteger xr = lambda.multiply(lambda).subtract(P.x.multiply(BigInteger.TWO)).mod(p);
        BigInteger yr = lambda.multiply(P.x.subtract(xr)).subtract(P.y).mod(p);

        return new Punkt(xr, yr);
    }

    // Schnelle Addition (Double-and-Add Methode)
    public Punkt multiply(Punkt P, BigInteger k) {
        Punkt result = Punkt.infinity();
        Punkt base = P;

        while (k.compareTo(BigInteger.ZERO) > 0) {
            if (k.testBit(0)) { // Prüft, ob das niedrigste Bit 1 ist
                result = add(result, base);
            }
            base = doubleP(base);
            k = k.shiftRight(1); // Entspricht Division durch 2
        }
        return result;
    }
}
