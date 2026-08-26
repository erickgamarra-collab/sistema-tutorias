package edu.uees.tutorias.service;

import edu.uees.tutorias.domain.Estudiante;
import edu.uees.tutorias.domain.HorarioDisponible;
import edu.uees.tutorias.domain.Reserva;
import edu.uees.tutorias.notification.Notificador;
import edu.uees.tutorias.repository.ReservaRepository;

import java.util.Objects;
import java.util.UUID;

/**
 * Caso de uso que coordina reservas, persistencia y notificación mediante abstracciones.
 */
public final class ServicioReservas {
    private final ReservaRepository reservaRepository;
    private final Notificador notificador;

    public ServicioReservas(ReservaRepository reservaRepository, Notificador notificador) {
        this.reservaRepository = Objects.requireNonNull(reservaRepository, "El repositorio es obligatorio");
        this.notificador = Objects.requireNonNull(notificador, "El notificador es obligatorio");
    }

    public Reserva solicitarTutoria(Estudiante estudiante, HorarioDisponible horario) {
        Objects.requireNonNull(estudiante, "El estudiante es obligatorio");
        Objects.requireNonNull(horario, "El horario es obligatorio");

        if (!horario.estaDisponible()) {
            throw new IllegalStateException("No es posible reservar un horario ocupado");
        }

        horario.reservar();
        Reserva reserva = new Reserva(estudiante, horario);
        reservaRepository.guardar(reserva);

        notificarParticipantes(reserva, "Nueva tutoría solicitada. Estado: " + reserva.getEstado());
        return reserva;
    }

    public Reserva confirmarReserva(UUID reservaId) {
        Reserva reserva = obtenerReserva(reservaId);
        reserva.confirmar();
        reservaRepository.guardar(reserva);
        notificarParticipantes(reserva, "Tutoría confirmada.");
        return reserva;
    }

    public Reserva cancelarReserva(UUID reservaId) {
        Reserva reserva = obtenerReserva(reservaId);
        reserva.cancelar();
        reserva.getHorario().liberar();
        reservaRepository.guardar(reserva);
        notificarParticipantes(reserva, "Tutoría cancelada.");
        return reserva;
    }

    public Reserva reprogramarReserva(UUID reservaId, HorarioDisponible nuevoHorario) {
        Reserva reserva = obtenerReserva(reservaId);
        Objects.requireNonNull(nuevoHorario, "El nuevo horario es obligatorio");

        if (!nuevoHorario.estaDisponible()) {
            throw new IllegalStateException("El nuevo horario no está disponible");
        }
        if (!reserva.getDocente().getId().equals(nuevoHorario.getDocente().getId())) {
            throw new IllegalArgumentException("La reprogramación debe conservar al mismo docente");
        }

        HorarioDisponible horarioAnterior = reserva.getHorario();
        nuevoHorario.reservar();
        try {
            reserva.reprogramar(nuevoHorario);
            horarioAnterior.liberar();
            reservaRepository.guardar(reserva);
        } catch (RuntimeException ex) {
            nuevoHorario.liberar();
            throw ex;
        }

        notificarParticipantes(reserva, "Tutoría reprogramada. Nuevo estado: " + reserva.getEstado());
        return reserva;
    }

    public Reserva completarReserva(UUID reservaId) {
        Reserva reserva = obtenerReserva(reservaId);
        reserva.completar();
        reservaRepository.guardar(reserva);
        notificarParticipantes(reserva, "Tutoría completada.");
        return reserva;
    }

    private Reserva obtenerReserva(UUID id) {
        Objects.requireNonNull(id, "El id de reserva es obligatorio");
        return reservaRepository.buscarPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("No existe una reserva con id " + id));
    }

    private void notificarParticipantes(Reserva reserva, String mensaje) {
        notificador.enviar(reserva.getEstudiante(), mensaje);
        notificador.enviar(reserva.getDocente(), mensaje);
    }
}
