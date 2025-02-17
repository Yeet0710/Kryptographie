package org.rsa;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class erweiterterEuklidTest {

    @Test
    void testErweiterterGGT_GemeinsamerTeilerVorhanden() {
        int a = 252, b = 105;
        int[] result = erweiterterEuklid.erweiterereGGT(a, b);

        assertEquals(21, result[0]);  // ggT von 252 und 105 ist 21
        assertEquals(1, (a * result[1] + b * result[2]) / result[0]); // Testet die Gleichung: ax + by = ggT
    }

    @Test
    void testErweiterterGGT_Primzahlen() {
        int a = 13, b = 7;
        int[] result = erweiterterEuklid.erweiterereGGT(a, b);

        assertEquals(1, result[0]); // ggT von Primzahlen ist 1
        assertEquals(1, (a * result[1] + b * result[2])); // Testet die Gleichung: ax + by = 1
    }

    @Test
    void testErweiterterGGT_GleicheZahlen() {
        int a = 42, b = 42;
        int[] result = erweiterterEuklid.erweiterereGGT(a, b);

        assertEquals(42, result[0]); // ggT von 42 und 42 ist 42
        assertEquals(1, (a * result[1] + b * result[2]) / result[0]); // ax + by = ggT
    }

    @Test
    void testErweiterterGGT_EinsUndZahl() {
        int a = 1, b = 77;
        int[] result = erweiterterEuklid.erweiterereGGT(a, b);

        assertEquals(1, result[0]); // ggT von 1 und jeder Zahl ist 1
        assertEquals(1, (a * result[1] + b * result[2])); // Testet die Gleichung: ax + by = 1
    }

    @Test
    void testErweiterterGGT_ZahlUndEins() {
        int a = 77, b = 1;
        int[] result = erweiterterEuklid.erweiterereGGT(a, b);

        assertEquals(1, result[0]); // ggT von 77 und 1 ist 1
        assertEquals(1, (a * result[1] + b * result[2])); // Testet die Gleichung: ax + by = 1
    }
}
