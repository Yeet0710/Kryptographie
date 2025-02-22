package org.scrum1_8;
import org.scrum1_2.PrimTester;
import java.math.BigInteger;
import java.security.SecureRandom;

public class EllipticCurveOrderCalculator {

    private static final SecureRandom secureRandom = new SecureRandom();

    // Berechnet die Gruppenordnung N gemäß Korollar 3.1
    public static BigInteger computeGroupOrder(BigInteger p) {
        if (!PrimTester.istPrimzahl(p, 20)) {
            throw new IllegalStateException("Eingegebene Zahl(" + p + ") ist keine Primzahl");
        }
        int[] xy = findXYRepresentation(p);
        int x = xy[0];
        int y = xy[1];

        // Berechnung von h gemäß Korollar 3.1
        int h;
        if ((x % 4 == 0 && y % 4 == 3) ||           // x ≡ 0 (mod 4) & y ≡ 3 (mod 4)
            (x % 4 == 2 && y % 4 == 1)) {           // x ≡ 2 (mod 4) & y ≡ 1 (mod 4)
            h = -2 * y;                             // => h = -2 * y
        } else if ((x % 4 == 0 && y % 4 == 1) ||    // x ≡ 0 (mod 4) & y ≡ 1 (mod 4)
                   (x % 4 == 2 && y % 4 == 3)) {    // x ≡ 2 (mod 4) & y ≡ 3 (mod 4)
            h = 2 * y;                              // => h = 2 * y
        } else {
            throw new IllegalStateException("Unerwartete Werte für x und y");
        }

        return p.add(BigInteger.ONE).subtract(BigInteger.valueOf(h));
    }

    // Findet x, y mit p = x^2 + y^2, wobei y ungerade ist und x > 0, y > 0
    /**
     * Generiert zufällige x und y mit p = x^2 + y^2, wobei y ungerade ist und x > 0, y > 0
     * @param p Primzahl für die die Darstellung als Summe zweier Quadratzahlen gesucht wird
     * @return Die generierten x und y
     */
    private static int[] findXYRepresentation(BigInteger p) {
        int x, y;
        while (true) {
            x = new BigInteger(p.bitLength(), secureRandom).mod(p).intValue(); // Generiert zufälliges x (mod p)
            int ySquared = p.intValue() - (x * x); // y^2 = p - x^2
            y = (int) Math.sqrt(ySquared);
            if (y * y == ySquared && y % 2 == 1 && x > 0 && y > 0) { // y * y = y^2 , y ist ungerade & x , y > 0
                return new int[]{x, y};
            }
        }
    }

    public static void main(String[] args) {
        BigInteger p = new BigInteger("23"); // Beispielprimzahl

        BigInteger order = computeGroupOrder(p);
        System.out.println("Gruppenordnung N: " + order);
    }
}
