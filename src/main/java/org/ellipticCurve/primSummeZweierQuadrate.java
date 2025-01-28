package org.ellipticCurve;

import org.rsa.PrimzahlTest;
import org.rsa.euklidischerAlgorithmus;
import org.rsa.schnelleExponentiation;
import org.rsa.erweiterterEuklid;

import java.lang.reflect.Array;
import java.math.BigInteger;
import java.security.SecureRandom;

public class primSummeZweierQuadrate {

    public int[] summeZweierQudrate(BigInteger p) {

        int x;
        int y;

        //Erstellt eine zufällige Zahl z
        SecureRandom random = new SecureRandom();
        BigInteger z = new BigInteger(p.bitLength(), random);

        //Prüft ob z eine Quadratische Restklasse modulo p ist
        while(schnelleExponentiation.modularesPotenzieren(z.longValue(), (p.subtract(BigInteger.valueOf(1))).divide(BigInteger.valueOf(2)).longValue(),p.longValue()) != -1) {
            z = new BigInteger(p.bitLength(), random);
        }

        //Berechnet w = z^((p+1)/4) mod p
        BigInteger w = BigInteger.valueOf(schnelleExponentiation.modularesPotenzieren(z.longValue(), (p.subtract(BigInteger.valueOf(1))).divide(BigInteger.valueOf(4)).longValue(),p.longValue()));

        //Berechnet ggT(w,p)
        int[] ggT = erweiterterEuklid.erweiterereGGT(w.intValue(),p.intValue());

        //Wähle x und y so, dass x^2 + y^2 = p
        if (ggT[1] % 2 == 0) {
            x = ggT[1];
            y = ggT[2];
        } else {
            x = ggT[2];
            y = ggT[1];
        }
        return new int[]{x,y};
    }


}
