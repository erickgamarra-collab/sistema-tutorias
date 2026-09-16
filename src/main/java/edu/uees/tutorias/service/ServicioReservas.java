package edu.uees.tutorias.service;

import edu.uees.tutorias.builder.ReservaBuilder;
import edu.uees.tutorias.domain.Estudiante;
import edu.uees.tutorias.domain.HorarioDisponible;
import edu.uees.tutorias.domain.Reserva;
import edu.uees.tutorias.notification.Notificador;
import edu.uees.tutorias.observer.NotificacionReservaObserver;
import edu.uees.tutorias.observer.ReservaObserver;
import edu.uees.tutorias.repository.ReservaRepository;
import edu.uees.tutorias.strategy.CancelacionEstandar;
import edu.uees.tutorias.strategy.PoliticaCancelacion;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Caso de uso que coordina reservas y persistencia. Las políticas variables y
 * las reacciones a eventos se delegan a Strategy y Observer.
 */
public final class ServicioReservas {
    private static final String EVENTO_SOLICITADA = "Nueva tutoría solicitada.";
    private static final String EVENTO_CONFIRMADA = "Tutoría confirmada.";
    private static final String EVENTO_CANCELADA = "Tutoría cancelada.";
    private static final String EVENTO_REPROGRAMADA = "Tutoría reprogramada.";
    private static final String EVENTO_COMPLETADA = "Tutoría completada.";

    private final ReservaRepository reservaRepository;
    private final List<ReservaObserver> observers = new ArrayList<>();
    private PoliticaCancelacion politicaCancelacion;

    /**
     * Constructor compatible con Ae1: conserva la notificación y utiliza la
     * política estándar de cancelación.
     */
    public ServicioReservas(ReservaRepository reservaRepository, Notificador notificador) {
        this(reservaRepository, new CancelacionEstandar());
        registrarObserver(new NotificacionReservaObserver(notificador));
    }

    public ServicioReservas(ReservaRepository reservaRepository, PoliticaCancelacion politicaCancelacion) {
        this.reservaRepository = Objects.requireNonNull(reservaRepository, "El repositorio es obligatorio");
        this.politicaCancelacion = Objects.requireNonNull(
                politicaCancelacion, "La política de cancelación es obligatoria");
    }

    public void registrarObserver(ReservaObserver observer) {
        observers.add(Objects.requireNonNull(observer, "El observer es obligatorio"));
    }

    public void eliminarObserver(ReservaObserver observer) {
        observers.remove(observer);
    }

    public void cambiarPoliticaCancelacion(PoliticaCancelacion politicaCancelacion) {
        this.politicaCancelacion = Objects.requireNonNull(
                politicaCancelacion, "La política de cancelación es obligatoria");
    }

    public Reserva solicitarTutoria(Estudiante estudiante, HorarioDisponible horario) {
        Objects.requireNonNull(estudiante, "El estudiante es obligatorio");
        Objects.requireNonNull(horario, "El horario es obligatorio");

        if (!horario.estaDisponible()) {
            throw new IllegalStateException("No es posible reservar un horario ocupado");
        }

        horario.reservar();
        Reserva reserva = new ReservaBuilder()
                .estudiante(estudiante)
                .horario(horario)
                .build();
        registrarCambio(reserva, EVENTO_SOLICITADA);
        return reserva;
    }

    public Reserva confirmarReserva(UUID reservaId) {
        Reserva reserva = obtenerReserva(reservaId);
        reserva.confirmar();
        registrarCambio(reserva, EVENTO_CONFIRMADA);
        return reserva;
    }

    public Reserva cancelarReserva(UUID reservaId) {
        Reserva reserva = obtenerReserva(reservaId);
        if (!politicaCancelacion.puedeCancelar(reserva)) {
            throw new IllegalStateException(
                    "La reserva no puede cancelarse con la política: " + politicaCancelacion.descripcion());
        }
        reserva.cancelar();
        reserva.getHorario().liberar();
        registrarCambio(reserva, EVENTO_CANCELADA);
        return reserva;
    }

    public Reserva reprogramarReserva(UUID reservaId, HorarioDisponible nuevoHorario) {
        Reserva reserva = obtenerReserva(reservaId);
        validarReprogramacion(reserva, nuevoHorario);
        moverHorario(reserva, nuevoHorario);
        notificarCambio(reserva, EVENTO_REPROGRAMADA);
        return reserva;
    }

    private void validarReprogramacion(Reserva reserva, HorarioDisponible nuevoHorario) {
        Objects.requireNonNull(nuevoHorario, "El nuevo horario es obligatorio");

        if (!nuevoHorario.estaDisponible()) {
            throw new IllegalStateException("El nuevo horario no está disponible");
        }
        if (!reserva.getDocente().getId().equals(nuevoHorario.getDocente().getId())) {
            throw new IllegalArgumentException("La reprogramación debe conservar al mismo docente");
        }
    }

    private void moverHorario(Reserva reserva, HorarioDisponible nuevoHorario) {
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
    }

    public Reserva completarReserva(UUID reservaId) {
        Reserva reserva = obtenerReserva(reservaId);
        reserva.completar();
        registrarCambio(reserva, EVENTO_COMPLETADA);
        return reserva;
    }

    private Reserva obtenerReserva(UUID id) {
        Objects.requireNonNull(id, "El id de reserva es obligatorio");
        return reservaRepository.buscarPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("No existe una reserva con id " + id));
    }

    private void registrarCambio(Reserva reserva, String evento) {
        reservaRepository.guardar(reserva);
        notificarCambio(reserva, evento);
    }

    private void notificarCambio(Reserva reserva, String evento) {
        for (ReservaObserver observer : List.copyOf(observers)) {
            observer.actualizar(reserva, evento);
        }
    }
}
