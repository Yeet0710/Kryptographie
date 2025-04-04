package org.ellipticCurveFinal;

import java.math.BigInteger;

public abstract class ECPoint {

    /**
     * Liefert den x-Koordinatenwert (bei Unendlichkeitspunkt wird eine Exception geworfen).
     */
    public abstract BigInteger getX();

    /**
     * Liefert den y-Koordinatenwert (bei Unendlichkeitspunkt wird eine Exception geworfen).
     */
    public abstract BigInteger getY();

    /**
     * Normalisiert den Punkt: Reduziert die Koordinaten modulo p und überprüft, ob der
     * normalisierte Punkt auf der Kurve liegt. Ist dies nicht der Fall, wird der Unendlichkeitspunkt zurückgegeben.
     */
    public abstract ECPoint normalize(FiniteFieldEllipticCurve curve);

    /**
     * Addiert diesen Punkt mit einem anderen Punkt auf derselben Kurve.
     */
    public abstract ECPoint add(ECPoint other, FiniteFieldEllipticCurve curve);

    /**
     * Verdoppelt den aktuellen Punkt auf der Kurve.
     */
    public abstract ECPoint doublePoint(FiniteFieldEllipticCurve curve);

    /**
     * Skalar-Multiplikation (Double-and-Add-Algorithmus).
     */
    public ECPoint multiply(BigInteger scalar, FiniteFieldEllipticCurve curve) {
        ECPoint result = InfinitePoint.getInstance();
        ECPoint temp = this;
        for (int i = 0; i < scalar.bitLength(); i++) {
            if (scalar.testBit(i)) {
                result = result.add(temp, curve);
            }
            temp = temp.doublePoint(curve);
        }
        return result;
    }
}
