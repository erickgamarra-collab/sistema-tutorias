package edu.uees.tutorias;

import edu.uees.tutorias.domain.Docente;
import edu.uees.tutorias.domain.EstadoReserva;
import edu.uees.tutorias.domain.Estudiante;
import edu.uees.tutorias.domain.HorarioDisponible;
import edu.uees.tutorias.domain.Reserva;
import edu.uees.tutorias.repository.MemoriaReservaRepository;
import edu.uees.tutorias.service.ServicioReservas;
import edu.uees.tutorias.strategy.CancelacionEstandar;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas de caracterización para congelar el comportamiento observable antes
 * de iniciar la kata de refactorización Ae4.
 */
class Ae4LineaBaseTest {

    @Test
    void caso1SolicitarTutoriaConservaEstadoYOcupaHorario() {
        MemoriaReservaRepository repository = new MemoriaReservaRepository();
        ServicioReservas servicio = new ServicioReservas(repository, new CancelacionEstandar());
        Estudiante estudiante = estudiante();
        HorarioDisponible horario = horario(docente(), 10, 11);

        Reserva reserva = servicio.solicitarTutoria(estudiante, horario);

        assertEquals(EstadoReserva.SOLICITADA, reserva.getEstado());
        assertFalse(horario.estaDisponible());
        assertTrue(repository.buscarPorId(reserva.getId()).isPresent());
    }

    @Test
    void caso2ConfirmarTutoriaConservaEstadoConfirmado() {
        ServicioReservas servicio = new ServicioReservas(
                new MemoriaReservaRepository(), new CancelacionEstandar());
        Reserva reserva = servicio.solicitarTutoria(estudiante(), horario(docente(), 10, 11));

        servicio.confirmarReserva(reserva.getId());

        assertEquals(EstadoReserva.CONFIRMADA, reserva.getEstado());
    }

    @Test
    void caso3CancelarTutoriaLiberaHorario() {
        ServicioReservas servicio = new ServicioReservas(
                new MemoriaReservaRepository(), new CancelacionEstandar());
        HorarioDisponible horario = horario(docente(), 10, 11);
        Reserva reserva = servicio.solicitarTutoria(estudiante(), horario);
        servicio.confirmarReserva(reserva.getId());

        servicio.cancelarReserva(reserva.getId());

        assertEquals(EstadoReserva.CANCELADA, reserva.getEstado());
        assertTrue(horario.estaDisponible());
    }

    @Test
    void caso4ReprogramarConservaDocenteYMueveLaOcupacion() {
        ServicioReservas servicio = new ServicioReservas(
                new MemoriaReservaRepository(), new CancelacionEstandar());
        Docente docente = docente();
        HorarioDisponible original = horario(docente, 10, 11);
        HorarioDisponible nuevo = horario(docente, 12, 13);
        Reserva reserva = servicio.solicitarTutoria(estudiante(), original);

        servicio.reprogramarReserva(reserva.getId(), nuevo);

        assertEquals(EstadoReserva.REPROGRAMADA, reserva.getEstado());
        assertSame(nuevo, reserva.getHorario());
        assertTrue(original.estaDisponible());
        assertFalse(nuevo.estaDisponible());
    }

    private static Estudiante estudiante() {
        return new Estudiante("Ana Torres", "ana.torres@uees.edu.ec", "Computación");
    }

    private static Docente docente() {
        return new Docente("Carlos Ruiz", "carlos.ruiz@uees.edu.ec", "Programación");
    }

    private static HorarioDisponible horario(Docente docente, int horaInicio, int horaFin) {
        return docente.publicarHorario(
                LocalDateTime.of(2026, 9, 20, horaInicio, 0),
                LocalDateTime.of(2026, 9, 20, horaFin, 0));
    }
}
