package org.rsa;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SchnelleExponentiationTest {

    @Test
    void testModularesPotenzieren() {
        assertEquals(9, schnelleExponentiation.modularesPotenzieren(3, 2, 10));
        assertEquals(3, schnelleExponentiation.modularesPotenzieren(3, 1, 10));
        assertEquals(1, schnelleExponentiation.modularesPotenzieren(3, 0, 10));
        assertEquals(81, schnelleExponentiation.modularesPotenzieren(3, 4, 100));
        assertEquals(27, schnelleExponentiation.modularesPotenzieren(3, 3, 100));
    }

    @Test
    void testModularesPotenzierenMitGroßenZahlen() {
        assertEquals(16, schnelleExponentiation.modularesPotenzieren(2, 30, 1000));
        assertEquals(32, schnelleExponentiation.modularesPotenzieren(2, 5, 100));
        assertEquals(6, schnelleExponentiation.modularesPotenzieren(5, 3, 11));
    }
}
