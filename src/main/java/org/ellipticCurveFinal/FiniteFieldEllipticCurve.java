package org.ellipticCurveFinal;

import org.scrum1_3.schnelleExponentiation;
import org.scrum1_4.erweiterterEuklid;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

public class FiniteFieldEllipticCurve {

    // Feste Kurvenparameter: y^2 = x^3 - x
    private final BigInteger a = BigInteger.valueOf(-1); // a = -1
    private final BigInteger b = BigInteger.ZERO;          // b = 0
    private final BigInteger p;  // Primzahl des endlichen Körpers
    private BigInteger q;        // Untergruppenordnung (q = N/8, sofern gültig)

    public FiniteFieldEllipticCurve(BigInteger p) {
        this.p = p;
    }

    public BigInteger getA() {
        // Für Rechnungen mod p kann a auch als p-1 genutzt werden.
        return a.mod(p);
    }

    public BigInteger getB() {
        return b;
    }

    public BigInteger getP() {
        return p;
    }

    public BigInteger getQ() {
        return q;
    }

    public void setQ(BigInteger q) {
        this.q = q;
    }

    /**
     * Prüft, ob der gegebene Punkt die Kurvengleichung y² = x³ - x (mod p) erfüllt.
     */
    public boolean isValidPoint(ECPoint point) {
        if (point instanceof InfinitePoint) {
            return true;
        }
        BigInteger x = point.getX();
        BigInteger y = point.getY();
        BigInteger left = y.modPow(BigInteger.valueOf(2), p);
        BigInteger right = (x.modPow(BigInteger.valueOf(3), p)
                .subtract(x)).mod(p);
        return left.equals(right);
    }

    /**
     * Findet einen Generator per Kofaktor-Methode
     * -------------------------------------------
     * Wählt einen zufälligen Kurvenpunkt aus,
     * multipliziert ihn einmal mit dem festen Faktor (8)
     * und prüft nur, ob das Ergebnis nicht der Punkt im Unendlichen ist.
     * Damit entfällt das bisherige aufwändige Testen mehrerer Vielfacher.
     *
     * @param q Die Ordnung der gesuchten Untergruppe.
     * @return Ein Generator der Untergruppe ermittelt durch einmalige Kofaktor-Multiplikation.
     */
    public ECPoint findGenerator(BigInteger q) {
        SecureRandom random = new SecureRandom();
        BigInteger legExp = p.subtract(BigInteger.ONE).divide(BigInteger.valueOf(2));
        BigInteger eight = BigInteger.valueOf(8);

        while (true) {
            // Zufälliges x in F_p
            BigInteger x = new BigInteger(p.bitLength(), random).mod(p);
            // r = x^3 - x mod p
            BigInteger r = x.modPow(BigInteger.valueOf(3), p).subtract(x).mod(p);
            // Legendre-Symbol test: r^((p-1)/2) mod p == 1
            if (!r.modPow(legExp, p).equals(BigInteger.ONE)) continue;
            // Quadratwurzel für p ≡ 5 mod 8: y = r^((p+3)/8) mod p
            BigInteger exp = p.add(BigInteger.valueOf(3)).divide(BigInteger.valueOf(8));
            BigInteger y = r.modPow(exp, p);
            // Falls nötig, nochmals prüfen
            if (!y.modPow(BigInteger.valueOf(2), p).equals(r)) continue;

            ECPoint g0 = new FiniteFieldECPoint(x, y).normalize(this);
            if (!isValidPoint(g0)) continue;

            // Kofaktor-Multiplikation
            ECPoint g = g0.multiply(eight, this);
            // Rückgabe, sobald g != O
            if (!(g instanceof InfinitePoint)) {
                return g;
            }
        }
    }






    /**
     * Berechnet alle Punkte der Kurve (nur für kleine p praktikabel).
     */
    public List<ECPoint> calculateAllPoints() {
        List<ECPoint> points = new ArrayList<>();
        for (BigInteger x = BigInteger.ZERO; x.compareTo(p) < 0; x = x.add(BigInteger.ONE)) {
            BigInteger rhs = (x.modPow(BigInteger.valueOf(3), p).subtract(x)).mod(p);
            for (BigInteger y = BigInteger.ZERO; y.compareTo(p) < 0; y = y.add(BigInteger.ONE)) {
                if (y.modPow(BigInteger.valueOf(2), p).equals(rhs)) {
                    points.add(new FiniteFieldECPoint(x, y));
                }
            }
        }
        points.add(InfinitePoint.getInstance());
        return points;
    }

    /**
     * Berechnet die Gruppenordnung N = p + 1 - h gemäß der Durchführungsverordnung.
     * Dabei wird p als Summe zweier Quadrate dargestellt: p = x² + y².
     * h wird anhand der Kongruenzklassen von x und y modulo 4 bestimmt.
     * (Hinweis: Diese naive Darstellung funktioniert nur für kleine p.)
     */
    public BigInteger calculateGroupOrder() {
        SumOfSquares rep = SumOfSquares.represent(p);
        BigInteger x = rep.x;
        BigInteger y = rep.y;

        BigInteger h;
        if (x.mod(BigInteger.valueOf(4)).equals(BigInteger.ZERO)) {
            if (y.mod(BigInteger.valueOf(4)).equals(BigInteger.valueOf(3))) {
                h = y.multiply(BigInteger.valueOf(-2));
            } else {
                h = y.multiply(BigInteger.valueOf(2));
            }
        } else {
            if (y.mod(BigInteger.valueOf(4)).equals(BigInteger.valueOf(3))) {
                h = y.multiply(BigInteger.valueOf(2));
            } else {
                h = y.multiply(BigInteger.valueOf(-2));
            }
        }
        return p.add(BigInteger.ONE).subtract(h);
    }


        public static class SumOfSquares {
            public final BigInteger x;
            public final BigInteger y;

            public SumOfSquares(BigInteger x, BigInteger y) {
                this.x = x;
                this.y = y;
            }

            /**
             * Stellt eine Primzahl p ≡ 1 mod 4 (und somit auch p ≡ 5 mod 8) als Summe zweier Quadrate p = x² + y² dar.
             * Es wird Cornacchias Algorithmus verwendet, der auch bei großen p effizient arbeitet.
             * Diese Methode gibt konsolenbasierte Statusmeldungen aus.
             *
             * Schritt 1: Bestimme effizient ein t mit t² ≡ -1 mod p über einen probabilistischen Ansatz.
             * Schritt 2: Führe den modifizierten euklidischen Algorithmus durch, solange r1² > p gilt.
             * Schritt 3: Ermittle y aus y² = p - r1² und gebe das Paar (x,y) zurück (sortiert, sodass x ≤ y).
             *
             * @param p eine Primzahl, für die p ≡ 1 mod 4 gilt
             * @return SumOfSquares-Instanz mit x und y, sodass p = x² + y²
             */
            public static SumOfSquares represent(BigInteger p) {
                System.out.println("[Cornacchia] Starte Darstellung von p als Summe zweier Quadrate.");
                if (!p.mod(BigInteger.valueOf(4)).equals(BigInteger.ONE)) {
                    throw new IllegalArgumentException("p muss ≡ 1 mod 4 sein.");
                }

                // Schritt 1: Suche effizient ein t mit t² ≡ -1 mod p
                BigInteger t = findSqrtOfMinusOne(p);

                // Schritt 2: Anwenden des euklidischen Algorithmus
                BigInteger r0 = p;
                BigInteger r1 = t;
                System.out.println("[Cornacchia] Starte euklidischen Algorithmus: r0 = p, r1 = t = " + t);
                int step = 0;
                while (r1.multiply(r1).compareTo(p) > 0) {
                    BigInteger r2 = r0.mod(r1);
                    r0 = r1;
                    r1 = r2;
                    step++;
                    if (step % 5 == 0) {
                        System.out.println("[Cornacchia] Schritt " + step + ": r0 = " + r0 + ", r1 = " + r1);
                    }
                }
                System.out.println("[Cornacchia] Euklidischer Algorithmus beendet: r1 = " + r1);

                // Schritt 3: Berechne y² = p - r1² und bestimme y
                BigInteger x = r1;
                BigInteger ySquared = p.subtract(x.multiply(x));
                BigInteger y = sqrtFloor(ySquared);
                if (!y.multiply(y).equals(ySquared)) {
                    throw new IllegalStateException("Darstellung von p als Summe zweier Quadrate nicht möglich (kein perfektes Quadrat bei y²).");
                }
                if (x.compareTo(y) > 0) {
                    // Tausche, sodass x ≤ y
                    BigInteger temp = x;
                    x = y;
                    y = temp;
                }
                System.out.println("[Cornacchia] Erfolgreiche Darstellung: p = " + p + " = " + x + "^2 + " + y + "^2");
                return new SumOfSquares(x, y);
            }

            /**
             * Effiziente Suche nach t, sodass t² ≡ -1 mod p, mithilfe eines probabilistischen Ansatzes.
             * Hierbei wird ein zufälliges a gewählt, und dann t = a^((p-1)/4) mod p getestet.
             * Es werden Statusmeldungen ausgegeben.
             *
             * @param p die Primzahl, für die p ≡ 1 mod 4 gilt
             * @return t mit t² ≡ -1 mod p
             */
            private static BigInteger findSqrtOfMinusOne(BigInteger p) {
                System.out.println("[Cornacchia] Suche effizientes t mit t² ≡ -1 mod p...");
                SecureRandom random = new SecureRandom();
                BigInteger pMinus1 = p.subtract(BigInteger.ONE);
                BigInteger half = pMinus1.shiftRight(1);      // (p-1)/2
                BigInteger quarter = half.shiftRight(1);        // (p-1)/4
                int attempt = 0;
                while (true) {
                    attempt++;
                    BigInteger a = new BigInteger(p.bitLength(), random).mod(p);
                    if (a.compareTo(BigInteger.ONE) <= 0) continue;
                    // Prüfe: a^((p-1)/2) mod p
                    BigInteger check = a.modPow(half, p);
                    if (check.equals(pMinus1)) { // d.h. a^( (p-1)/2 ) ≡ -1 mod p
                        BigInteger t = a.modPow(quarter, p);
                        if (t.modPow(BigInteger.TWO, p).equals(pMinus1)) {
                            System.out.println("[Cornacchia] t gefunden nach " + attempt + " Versuchen: t = " + t);
                            return t;
                        }
                    }
                    if (attempt % 1000 == 0) {
                        System.out.println("[Cornacchia] " + attempt + " Versuche bisher...");
                    }
                }
            }

            /**
             * Berechnet die ganzzahlige Quadratwurzel von n mittels Newton-Verfahren (abgerundet).
             * Diese Methode liefert den größten ganzzahligen Wert x, sodass x² ≤ n.
             *
             * @param n die zu berechnende Zahl (n ≥ 0)
             * @return sqrtFloor(n)
             */
            private static BigInteger sqrtFloor(BigInteger n) {
                if (n.compareTo(BigInteger.ZERO) < 0) {
                    throw new ArithmeticException("sqrtFloor: negative Eingabe");
                }
                if (n.equals(BigInteger.ZERO) || n.equals(BigInteger.ONE)) {
                    return n;
                }
                BigInteger two = BigInteger.valueOf(2);
                BigInteger x = BigInteger.ONE.shiftLeft(n.bitLength() / 2);
                while (true) {
                    BigInteger y = x.add(n.divide(x)).divide(two);
                    if (y.equals(x) || y.equals(x.subtract(BigInteger.ONE))) {
                        return y;
                    }
                    x = y;
                }
            }
        }



    /**
         * Naive Methode zur Berechnung der Quadratwurzel eines BigInteger.
         * Liefert null, falls n kein perfektes Quadrat ist.
         */
        private static BigInteger bigIntSquareRoot(BigInteger n) {
            BigInteger candidate = BigInteger.ONE;
            while (candidate.multiply(candidate).compareTo(n) <= 0) {
                if (candidate.multiply(candidate).equals(n)) {
                    return candidate;
                }
                candidate = candidate.add(BigInteger.ONE);
            }
            return null;
        }
    }