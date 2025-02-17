package org.rsa;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class BlockChiffreTest {

    @Test
    void testZerlegeInBloecke_GenaueTeilung() {
        String text = "HELLOWORLD";
        int blockGroesse = 2;
        List<String> result = BlockChiffre.zerlegeInBloecke(text, blockGroesse);

        assertEquals(5, result.size());
        assertEquals("HE", result.get(0));
        assertEquals("LL", result.get(1));
        assertEquals("OW", result.get(2));
        assertEquals("OR", result.get(3));
        assertEquals("LD", result.get(4));
    }

    @Test
    void testZerlegeInBloecke_UngeradeLaenge() {
        String text = "HELLO";
        int blockGroesse = 3;
        List<String> result = BlockChiffre.zerlegeInBloecke(text, blockGroesse);

        assertEquals(2, result.size());
        assertEquals("HEL", result.get(0));
        assertEquals("LO", result.get(1));
    }

    @Test
    void testZerlegeInBloecke_LeererText() {
        String text = "";
        int blockGroesse = 3;
        List<String> result = BlockChiffre.zerlegeInBloecke(text, blockGroesse);

        assertTrue(result.isEmpty());
    }

    @Test
    void testZerlegeInBloecke_BlockgrößeGrößerAlsText() {
        String text = "HI";
        int blockGroesse = 5;
        List<String> result = BlockChiffre.zerlegeInBloecke(text, blockGroesse);

        assertEquals(1, result.size());
        assertEquals("HI", result.get(0));
    }

    @Test
    void testZerlegeInBloecke_EinzelneBuchstaben() {
        String text = "ABCDE";
        int blockGroesse = 1;
        List<String> result = BlockChiffre.zerlegeInBloecke(text, blockGroesse);

        assertEquals(5, result.size());
        assertEquals("A", result.get(0));
        assertEquals("B", result.get(1));
        assertEquals("C", result.get(2));
        assertEquals("D", result.get(3));
        assertEquals("E", result.get(4));
    }
}
