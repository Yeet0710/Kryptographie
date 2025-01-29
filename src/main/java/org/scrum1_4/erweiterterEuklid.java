package org.scrum1_4;

import java.math.BigInteger;

public class erweiterterEuklid {


    public static BigInteger[] erweiterterEuklid (BigInteger a, BigInteger b){
        // falls b = 0, ist der ggT = a und x = 1, y = 0
        if(b.equals(BigInteger.ZERO)){
            return new BigInteger[]{a, BigInteger.ONE, BigInteger.ZERO};
        }

        // rekursive Berechnung
        BigInteger[] result = erweiterterEuklid(b, a.mod(b));
        BigInteger ggT = result[0];
        BigInteger x1 = result[1];
        BigInteger y1 = result[2];

        BigInteger x = y1;
        BigInteger y = x1.subtract(a.divide(b).multiply(y1));

        return new BigInteger[]{ggT, x, y};

    }


    public static void main(String[] args) {
        BigInteger a = new BigInteger("120");
        BigInteger b = new BigInteger("23");

        // Führe den erweiterten euklidischen Algorithmus aus
        BigInteger[] result = erweiterterEuklid(a, b);

        // Ausgabe des Ergebnisses
        System.out.println("ggT: " + result[0]);   // Der ggT von a und b
        System.out.println("x: " + result[1]);    // Bezout-Koeffizient x
        System.out.println("y: " + result[2]);    // Bezout-Koeffizient y

        // Validierung der Bezout-Identität
        BigInteger bezoutCheck = a.multiply(result[1]).add(b.multiply(result[2]));
        System.out.println(a + " * " + result[1] + " + " + result[2] + " * " + b);
        System.out.println("Validierung der Bezout-Identität: " + bezoutCheck);
    }

    /*
    Bezout-Gleichung: 120 * (-9) + 23 * 47 = 1  ////  ax + by = ggT(a,b)
     */
}
