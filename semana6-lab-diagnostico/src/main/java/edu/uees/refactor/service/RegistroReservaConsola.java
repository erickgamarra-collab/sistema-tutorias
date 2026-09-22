package edu.uees.refactor.service;

import edu.uees.refactor.domain.Reserva;

/** Extract Class: conserva la simulación original de persistencia. */
final class RegistroReservaConsola {
    void guardar(Reserva reserva) {
        System.out.println("Guardando reserva " + reserva.getId());
    }
}
