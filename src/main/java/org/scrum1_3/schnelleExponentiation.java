package org.scrum1_3;

import java.math.BigInteger;
public class schnelleExponentiation {

    /**
     * schnelle Exponentiation mit Modulo
     *  (um den privaten Schlüssel d zu berechnen)
     * @param basis = Basis
     * @param exponent = Exponent
     * @param modulus = Modulo
     * @return  (base^exponent) mod modulus
     */
    public static BigInteger schnelleExponentiation (BigInteger basis, BigInteger exponent, BigInteger modulus) {
        if(modulus.equals(BigInteger.ONE)) return BigInteger.ZERO;
        if(exponent.equals(BigInteger.ZERO)) return BigInteger.ONE;

        BigInteger result = BigInteger.ONE;
        basis = basis.mod(modulus);

        while (exponent.compareTo(BigInteger.ZERO) > 0) {
            // falls das aktuelle Exponent-Bit gesetzt ist (ungerade Zahl)
            if(exponent.mod(BigInteger.TWO).equals(BigInteger.ONE)) {
                result = result.multiply(basis).mod(modulus);
            }
            // shift right
            exponent = exponent.divide(BigInteger.TWO);
            // Basis quadriren und wieder Modulo rechnen
            basis = basis.multiply(basis).mod(modulus);
        }
        return result;
    }

    /*
    n = 6 :2 = 3 (Rest 0) , LSB = 0
    n = 13 Binär= 1101, LSB = 1
     */
    public static void main(String[] args){
        BigInteger basis = new BigInteger("123456789");
        BigInteger exponent = new BigInteger("987654321");
        BigInteger modulus = new BigInteger("1000000007");

        BigInteger result = schnelleExponentiation(basis, exponent, modulus);
        System.out.println("Ergebnis: " + result);

    }


}
