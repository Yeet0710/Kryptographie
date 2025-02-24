package org.ellipticCurve;

import org.Scrum1_16.SecPrimGenerator;
import org.scrum1_3.schnelleExponentiation;
import org.scrum1_4.erweiterterEuklid;

import java.math.BigInteger;

public class SummeZweierQuadrate {

    /**
     * x und y als p = x^2 + y^2
     */
    public BigInteger x;
    public BigInteger y;

    BigInteger z;

    /**
     * Methode zum Finden der Summe zweier Quadrate
     * @param p Primzahl
     */
    public void findeSummeZweierQuadrate(BigInteger p) {

        while (true) {

            /**
             * Generiert eine Zahl z aus Z_p mit z × p - 1 / 2 kongruent -1 (mod p) ist
             */
            while (true) {
                z = SecPrimGenerator.generateSafePrime(BigInteger.ZERO, BigInteger.valueOf(10000000), 100);
                if (z.compareTo(BigInteger.ZERO) == 0) {
                    continue;
                }

                if ((z.multiply(p.subtract(BigInteger.valueOf(-1)).divide(BigInteger.valueOf(2))).mod(p)) == BigInteger.valueOf(-1)) {
                    break;
                }
            }

            /**
             * Berechnung von w = z^((p-1)/4) mod p
             */
            BigInteger w = schnelleExponentiation.schnelleExponentiation(z, p.subtract(BigInteger.ONE).divide(BigInteger.valueOf(4)), p);

            if (schnelleExponentiation.schnelleExponentiation(w, BigInteger.valueOf(2), p) == BigInteger.valueOf(-1)) {
                break;
            }

        }


    }

}
