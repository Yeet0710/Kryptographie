package org.ellipticCurve;

import org.scrum1_16.SecPrimGenerator;
import org.scrum1_3.schnelleExponentiation;

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
                z = SecPrimGenerator.generateSafePrime(BigInteger.valueOf(256), 100);
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

//    public int[] summeZweierQudrate(BigInteger p) {
//
//        int x;
//        int y;
//
//        //Erstellt eine zufällige Zahl z
//        SecureRandom random = new SecureRandom();
//        BigInteger z = new BigInteger(p.bitLength(), random);
//
//        //Prüft ob z eine Quadratische Restklasse modulo p ist
//        while(schnelleExponentiation.modularesPotenzieren(z.longValue(), (p.subtract(BigInteger.valueOf(1))).divide(BigInteger.valueOf(2)).longValue(),p.longValue()) != -1) {
//
//
//            z = new BigInteger(p.bitLength(), random);
//
//
//        }
//
//        //Berechnet w = z^((p+1)/4) mod p
//        BigInteger w = BigInteger.valueOf(schnelleExponentiation.modularesPotenzieren(z.longValue(), (p.subtract(BigInteger.valueOf(1))).divide(BigInteger.valueOf(4)).longValue(),p.longValue()));
//
//        //Berechnet ggT(w,p)
//        int[] ggT = erweiterterEuklid.erweiterereGGT(w.intValue(),p.intValue());
//
//        //Wähle x und y so, dass x^2 + y^2 = p
//        if (ggT[1] % 2 == 0) {
//            x = ggT[1];
//            y = ggT[2];
//        } else {
//            x = ggT[2];
//            y = ggT[1];
//        }
//
//        return new int[]{x,y};
//
//    }

}
