package org.ellipticCurveFinal;

import java.math.BigInteger;

public class FiniteFieldECPoint extends ECPoint {

    private final BigInteger x;
    private final BigInteger y;

    public FiniteFieldECPoint(BigInteger x, BigInteger y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public BigInteger getX() {
        return x;
    }

    @Override
    public BigInteger getY() {
        return y;
    }

    @Override
    public ECPoint normalize(FiniteFieldEllipticCurve curve) {
        BigInteger normX = x.mod(curve.getP());
        BigInteger normY = y.mod(curve.getP());
        ECPoint normalized = new FiniteFieldECPoint(normX, normY);
        return (curve.isValidPoint(normalized)) ? normalized : InfinitePoint.getInstance();
    }

    @Override
    public ECPoint add(ECPoint other, FiniteFieldEllipticCurve curve) {
        if (other instanceof InfinitePoint) {
            return this;
        }
        // Falls beide Punkte invers zueinander sind: y1 = -y2 (mod p) → Ergebnis ist der Unendlichkeitspunkt.
        if (this.x.equals(other.getX()) &&
                this.y.equals(other.getY().negate().mod(curve.getP()))) {
            return InfinitePoint.getInstance();
        }
        // Falls die Punkte identisch sind, wird die Punktverdopplung angewandt.
        if (this.x.equals(other.getX()) && this.y.equals(other.getY())) {
            return this.doublePoint(curve);
        }
        BigInteger p = curve.getP();
        BigInteger lambdaNumerator = other.getY().subtract(this.y).mod(p);
        BigInteger lambdaDenom = other.getX().subtract(this.x).mod(p);
        BigInteger lambda = lambdaNumerator.multiply(lambdaDenom.modInverse(p)).mod(p);

        BigInteger newX = lambda.modPow(BigInteger.valueOf(2), p)
                .subtract(this.x).subtract(other.getX()).mod(p);
        BigInteger newY = lambda.multiply(this.x.subtract(newX))
                .subtract(this.y).mod(p);

        return new FiniteFieldECPoint(newX, newY).normalize(curve);
    }

    @Override
    public ECPoint doublePoint(FiniteFieldEllipticCurve curve) {
        BigInteger p = curve.getP();
        // Formel: λ = (3x² + a) / (2y) mod p, wobei a = -1.
        BigInteger numerator = (this.x.modPow(BigInteger.valueOf(2), p).multiply(BigInteger.valueOf(3))
                .add(curve.getA())).mod(p);
        BigInteger denominator = (this.y.multiply(BigInteger.valueOf(2))).mod(p);
        BigInteger lambda = numerator.multiply(denominator.modInverse(p)).mod(p);

        BigInteger newX = lambda.modPow(BigInteger.valueOf(2), p)
                .subtract(this.x.multiply(BigInteger.valueOf(2))).mod(p);
        BigInteger newY = lambda.multiply(this.x.subtract(newX))
                .subtract(this.y).mod(p);

        return new FiniteFieldECPoint(newX, newY).normalize(curve);
    }

    @Override
    public String toString() {
        return "FiniteFieldECPoint {x = " + x + ", y = " + y + "}";
    }
}
