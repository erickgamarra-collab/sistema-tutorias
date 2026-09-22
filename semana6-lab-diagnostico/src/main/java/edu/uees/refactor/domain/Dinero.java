package edu.uees.refactor.domain;

/** Ejemplo didáctico aislado: no se utiliza en ServicioReservas. */
public record Dinero(double valor) {
    public Dinero {
        if (valor < 0) {
            throw new IllegalArgumentException("El dinero no puede ser negativo");
        }
    }
}
