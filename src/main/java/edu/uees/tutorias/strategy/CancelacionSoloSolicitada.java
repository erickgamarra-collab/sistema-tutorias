package edu.uees.tutorias.strategy;

import edu.uees.tutorias.domain.EstadoReserva;
import edu.uees.tutorias.domain.Reserva;

/** Política más restrictiva: solo cancela solicitudes aún no confirmadas. */
public final class CancelacionSoloSolicitada implements PoliticaCancelacion {
    @Override
    public boolean puedeCancelar(Reserva reserva) {
        return reserva.getEstado() == EstadoReserva.SOLICITADA
                || reserva.getEstado() == EstadoReserva.REPROGRAMADA;
    }

    @Override
    public String descripcion() {
        return "Cancelación solo antes de confirmar";
    }
}
