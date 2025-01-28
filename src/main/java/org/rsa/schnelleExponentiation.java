package org.rsa;

public class schnelleExponentiation {
    public static long modularesPotenzieren(long basis, long exponent, long modul) {
        long ergebnis = 1;
        basis = basis % modul; // Basis modulo nehmen 
        while(exponent > 0) {
            if((exponent & 1) == 1) { //Prüfen, ob das niedrigste Bit 1 ist
                ergebnis = (ergebnis * basis) % modul;
            }
            basis = (basis * basis) % modul; //Basis quadrieren
            exponent >>= 1; // Exponent halbieren
        }
        return ergebnis;
    }
    
    public static void main (String[] args) {
        long basis = 3, exponent = 13, modul = 100;
        System.out.println("Ergebnis: " + modularesPotenzieren(basis, exponent, modul));
    }

    
}
