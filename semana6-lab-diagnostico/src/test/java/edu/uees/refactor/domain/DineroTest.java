package edu.uees.refactor.domain;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DineroTest {
    @Test
    void dineroNegativoLanzaExcepcion() {
        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class, () -> new Dinero(-1));
        assertEquals("El dinero no puede ser negativo", error.getMessage());
    }
}
