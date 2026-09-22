package edu.uees.refactor.service;

import edu.uees.refactor.domain.Reserva;

/** Extract Class: concentra la regla de precio, independiente del procesamiento. */
final class CalculadoraTarifa {
    private static final double TARIFA_BASE = 40;
    private static final double FACTOR_VIP = 0.85;

    double calcularTotal(Reserva reserva) {
        if ("VIP".equals(reserva.getTipo())) {
            return TARIFA_BASE * FACTOR_VIP;
        }
        return TARIFA_BASE;
    }
}
