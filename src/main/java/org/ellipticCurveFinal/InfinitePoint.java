package org.ellipticCurveFinal;

import java.math.BigInteger;

public class InfinitePoint extends ECPoint {

    private static final InfinitePoint instance = new InfinitePoint();

    private InfinitePoint() { }

    public static InfinitePoint getInstance() {
        return instance;
    }

    @Override
    public BigInteger getX() {
        throw new UnsupportedOperationException("Infinite point hat keine x-Koordinate.");
    }

    @Override
    public BigInteger getY() {
        throw new UnsupportedOperationException("Infinite point hat keine y-Koordinate.");
    }

    @Override
    public ECPoint normalize(FiniteFieldEllipticCurve curve) {
        return this;
    }

    @Override
    public ECPoint add(ECPoint other, FiniteFieldEllipticCurve curve) {
        return other;
    }

    @Override
    public ECPoint doublePoint(FiniteFieldEllipticCurve curve) {
        return this;
    }

    @Override
    public ECPoint multiply(BigInteger scalar, FiniteFieldEllipticCurve curve) {
        return this;
    }

    @Override
    public String toString() {
        return "InfinitePoint";
    }
}