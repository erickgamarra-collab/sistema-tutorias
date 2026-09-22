package edu.uees.refactor.service;

import edu.uees.refactor.domain.Reserva;

/** Coordina el caso de uso sin asumir las tarifas ni las salidas externas. */
public class ServicioReservas {
    private final CalculadoraTarifa calculadoraTarifa = new CalculadoraTarifa();
    private final RegistroReservaConsola registroReserva = new RegistroReservaConsola();
    private final NotificacionReservaConsola notificacionReserva = new NotificacionReservaConsola();

    public double procesar(Reserva r, int horasAnticipacion) {
        if (r == null) {
            return 0;
        }
        if (r.getCorreo() == null || !r.getCorreo().contains("@")) {
            return 0;
        }
        if (!r.getPeriodo().esValido()) {
            return 0;
        }
        if (horasAnticipacion < 2) {
            return 0;
        }

        double total = calculadoraTarifa.calcularTotal(r);

        registroReserva.guardar(r);
        notificacionReserva.enviar(r);
        r.confirmar();
        return total;
    }
}
