package edu.uees.refactor.domain;

import java.time.LocalDateTime;

/**
 * Value Object: agrupa inicio/fin y su regla de validez. No rechaza fechas
 * inválidas al construirse: el contrato heredado exige que procesar retorne 0.
 */
public record PeriodoReserva(LocalDateTime inicio, LocalDateTime fin) {
    public boolean esValido() {
        return inicio != null && fin != null && fin.isAfter(inicio);
    }
}
