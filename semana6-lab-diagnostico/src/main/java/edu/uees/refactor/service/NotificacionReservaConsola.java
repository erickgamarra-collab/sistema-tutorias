package edu.uees.refactor.service;

import edu.uees.refactor.domain.Reserva;

/** Extract Class: conserva la simulación original de notificación. */
final class NotificacionReservaConsola {
    void enviar(Reserva reserva) {
        System.out.println("Correo enviado a " + reserva.getCorreo());
    }
}
