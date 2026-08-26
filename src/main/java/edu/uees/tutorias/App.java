package edu.uees.tutorias;

import edu.uees.tutorias.domain.Docente;
import edu.uees.tutorias.domain.Estudiante;
import edu.uees.tutorias.domain.HorarioDisponible;
import edu.uees.tutorias.domain.Reserva;
import edu.uees.tutorias.notification.NotificadorConsola;
import edu.uees.tutorias.repository.MemoriaReservaRepository;
import edu.uees.tutorias.service.ServicioReservas;

import java.time.LocalDateTime;

/** Ejemplo mínimo de ejecución del modelo. */
public final class App {
    private App() {
    }

    public static void main(String[] args) {
        Estudiante estudiante = new Estudiante(
                "Ana Torres", "ana.torres@uees.edu.ec", "Ingeniería en Computación");
        Docente docente = new Docente(
                "Carlos Ruiz", "carlos.ruiz@uees.edu.ec", "Programación");

        HorarioDisponible horario = docente.publicarHorario(
                LocalDateTime.of(2026, 8, 28, 15, 0),
                LocalDateTime.of(2026, 8, 28, 16, 0));

        ServicioReservas servicio = new ServicioReservas(
                new MemoriaReservaRepository(),
                new NotificadorConsola());

        Reserva reserva = servicio.solicitarTutoria(estudiante, horario);
        servicio.confirmarReserva(reserva.getId());

        System.out.println("Reserva final: " + reserva.getId() + " - " + reserva.getEstado());
    }
}
