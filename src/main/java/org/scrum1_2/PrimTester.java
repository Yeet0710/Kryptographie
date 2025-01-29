package org.scrum1_2;

import java.math.*;
import java.security.*;

public class PrimTester {

    /**
     * prüft, ob die Zahl eine wahrscheinliche Primzahl ist, indem MRT mehrfach durchgeführt wird
     * @param zahl = die zu testende Zahl
     * @param iteration = Anzahl der Iterationen des MRTs
     * @return true = wahrscheinlich eine Primzahl, sonst false
     */
    public static boolean istPrimzahl(BigInteger zahl, int iteration) {
        // kleiner als 2 != Primzahl
        if (zahl.compareTo(BigInteger.TWO) < 0) return false;
        // 2 || 3 = Primzahl
        if (zahl.equals(BigInteger.TWO) || zahl.equals(BigInteger.valueOf(3))) return true;

        // zerlege (n - 1) in der Form n - 1 = 2^r * d, bis d ungerade ist
        BigInteger d = zahl.subtract(BigInteger.ONE);
        int r = 0;

        // bestimme die größte Potenz von 2, die (n - 1) teilt
        while (d.mod(BigInteger.TWO).equals(BigInteger.ZERO)) {
            d = d.divide(BigInteger.TWO);
            r++;
        }

        // wiederhole den MRT mit verschiedenen Basen
        for (int i = 0; i < iteration; i++) {
            if (!millerRabinTest(zahl, d)) { // = zusammengesetzte Zahl
                return false;
            }
        }
        return true;
    }

    /**
     * führt einzelnen MRT für eine gegebene Basis durch
     * @param zahl = die zu testende Zahl
     * @param d = ungerade Zahl, die aus der Zerlegung von (n-1) stammt: n - 1 = 2^r * d
     * @return  true = wenn die Zahl wahrscheinlich eine Primzahl ist, sonst false
     */
    private static boolean millerRabinTest(BigInteger zahl, BigInteger d) {
        SecureRandom random = new SecureRandom();

        // zufällige Basis a im Bereich [2, n-2]
        BigInteger a = BigInteger.TWO.add(new BigInteger(zahl.bitLength(), random).mod(zahl.subtract(BigInteger.TWO)));
        // berechne x = a^d mod zahl
        BigInteger x = a.modPow(d, zahl);

        // falls x ≡ 1 (mod zahl) oder x ≡ -1 (mod zahl), besteht die Zahl diesen Testlauf
        if (x.equals(BigInteger.ONE) || x.equals(zahl.subtract(BigInteger.ONE))) return true;

        // quadrieren von x bis d = n-1
        while (!d.equals(zahl.subtract(BigInteger.ONE))) {
            x = x.modPow(BigInteger.TWO, zahl); // x = x^2 mod zahl
            d = d.multiply(BigInteger.TWO); // d verdoppeln, nächstes Potenzieren

            // falls x ≡ 1 (mod zahl), ist die Zahl zusammengesetzt
            if (x.equals(BigInteger.ONE)) return false;
            // falls x ≡ -1 (mod zahl), dann gut
            if (x.equals(zahl.subtract(BigInteger.ONE))) return true;
        }
        return false;
    }


    public static void main (String[] args) {
        BigInteger testZahl = new BigInteger("104729");
        int iteration = 20;

        if(istPrimzahl(testZahl, iteration)) {
            System.out.println(testZahl + " ist wahrscheinlich eine Prmzahl.");
        } else {
            System.out.println(testZahl + " ist eine zusammengesetzte Zahl.");
        }
    }

}
