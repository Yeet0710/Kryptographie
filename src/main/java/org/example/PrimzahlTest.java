package org.example;
import java.math.*;
import java.security.*;

public class PrimzahlTest {

    public static boolean istPrimzahl(BigInteger zahl, int genauigkeit) {
        if (zahl.compareTo(BigInteger.TWO) < 0) return false;
        if (zahl.equals(BigInteger.TWO) || zahl.equals(BigInteger.valueOf(3))) return true;

        BigInteger d = zahl.subtract(BigInteger.ONE);
        int r = 0;

        while (d.mod(BigInteger.TWO).equals(BigInteger.ZERO)) {
            d = d.divide(BigInteger.TWO);
            r++;
        }

        for (int i = 0; i < genauigkeit; i++) {
            if (!millerRabinTest(zahl, d)) {
                return false;
            }
        }
        return true;
    }

    private static boolean millerRabinTest(BigInteger zahl, BigInteger d) {
        SecureRandom random = new SecureRandom();
        BigInteger a = BigInteger.TWO.add(new BigInteger(zahl.bitLength(), random).mod(zahl.subtract(BigInteger.TWO)));
        BigInteger x = a.modPow(d, zahl);

        if (x.equals(BigInteger.ONE) || x.equals(zahl.subtract(BigInteger.ONE))) return true;

        while (!d.equals(zahl.subtract(BigInteger.ONE))) {
            x = x.modPow(BigInteger.TWO, zahl);
            d = d.multiply(BigInteger.TWO);

            if (x.equals(BigInteger.ONE)) return false;
            if (x.equals(zahl.subtract(BigInteger.ONE))) return true;
        }
        return false;
    }

}
