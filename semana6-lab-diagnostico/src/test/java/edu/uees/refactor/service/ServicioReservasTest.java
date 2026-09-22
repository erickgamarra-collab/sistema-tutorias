package edu.uees.refactor.service;

import edu.uees.refactor.domain.EstadoReserva;
import edu.uees.refactor.domain.Reserva;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ServicioReservasTest {
    private static final LocalDateTime INICIO = LocalDateTime.of(2026, 9, 20, 10, 0);
    private final ServicioReservas servicio = new ServicioReservas();

    private Reserva reserva(String id, String correo, LocalDateTime inicio,
                            LocalDateTime fin, String tipo) {
        return new Reserva(id, correo, inicio, fin, tipo);
    }

    private Reserva normal() {
        return reserva("R-NORMAL", "ana@uees.edu.ec", INICIO, INICIO.plusHours(1), "NORMAL");
    }

    private Reserva vip() {
        return reserva("R-VIP", "vip@uees.edu.ec", INICIO, INICIO.plusHours(1), "VIP");
    }

    @Test
    void normalActualmenteRetornaCuarentaYConfirma() {
        Reserva r = normal();
        double total = servicio.procesar(r, 5);
        assertAll(
                () -> assertEquals(40, total, 0.001),
                () -> assertEquals(EstadoReserva.CONFIRMADA, r.getEstado()));
    }

    @Test
    void vipActualmenteRetornaTreintaYCuatroYConfirma() {
        Reserva r = vip();
        double total = servicio.procesar(r, 5);
        assertEquals(34, total, 0.001);
        assertEquals(EstadoReserva.CONFIRMADA, r.getEstado());
    }

    @Test
    void correoInvalidoNoProcesa() {
        Reserva r = reserva("R-EMAIL", "correo-invalido", INICIO, INICIO.plusHours(1), "NORMAL");
        assertEquals(0, servicio.procesar(r, 5), 0.001);
        assertEquals(EstadoReserva.PENDIENTE, r.getEstado());
    }

    @Test
    void correoNuloNoProcesa() {
        Reserva r = reserva("R-EMAIL-NULL", null, INICIO, INICIO.plusHours(1), "NORMAL");
        assertEquals(0, servicio.procesar(r, 5), 0.001);
        assertEquals(EstadoReserva.PENDIENTE, r.getEstado());
    }

    @Test
    void periodoInvertidoNoProcesa() {
        Reserva r = reserva("R-PERIODO", "ana@uees.edu.ec", INICIO, INICIO.minusHours(1), "NORMAL");
        assertEquals(0, servicio.procesar(r, 5), 0.001);
        assertEquals(EstadoReserva.PENDIENTE, r.getEstado());
    }

    @Test
    void periodoConFinIgualNoProcesa() {
        Reserva r = reserva("R-EQUAL", "ana@uees.edu.ec", INICIO, INICIO, "NORMAL");
        assertEquals(0, servicio.procesar(r, 5), 0.001);
        assertEquals(EstadoReserva.PENDIENTE, r.getEstado());
    }

    @Test
    void periodoConInicioNuloNoProcesa() {
        Reserva r = reserva("R-START-NULL", "ana@uees.edu.ec", null, INICIO, "NORMAL");
        assertEquals(0, servicio.procesar(r, 5), 0.001);
        assertEquals(EstadoReserva.PENDIENTE, r.getEstado());
    }

    @Test
    void periodoConFinNuloNoProcesa() {
        Reserva r = reserva("R-END-NULL", "ana@uees.edu.ec", INICIO, null, "NORMAL");
        assertEquals(0, servicio.procesar(r, 5), 0.001);
        assertEquals(EstadoReserva.PENDIENTE, r.getEstado());
    }

    @Test
    void dosHorasExactasPermitenProcesar() {
        Reserva r = normal();
        assertEquals(40, servicio.procesar(r, 2), 0.001);
        assertEquals(EstadoReserva.CONFIRMADA, r.getEstado());
    }

    @Test
    void unaHoraNoPermiteProcesar() {
        Reserva r = normal();
        assertEquals(0, servicio.procesar(r, 1), 0.001);
        assertEquals(EstadoReserva.PENDIENTE, r.getEstado());
    }

    @Test
    void reservaNulaRetornaCero() {
        assertEquals(0, servicio.procesar(null, 5), 0.001);
    }

    @Test
    void tipoNuloNoVipConservaPrecioNormal() {
        Reserva r = reserva("R-TIPO", "ana@uees.edu.ec", INICIO, INICIO.plusHours(1), null);
        assertEquals(40, servicio.procesar(r, 5), 0.001);
        assertEquals(EstadoReserva.CONFIRMADA, r.getEstado());
    }

    @Test
    void reservaValidaConservaMensajesExactosYOrden() {
        Reserva r = normal();
        String salida = capturarSalida(() -> assertEquals(40, servicio.procesar(r, 5), 0.001));
        assertEquals("Guardando reserva R-NORMAL" + System.lineSeparator()
                + "Correo enviado a ana@uees.edu.ec" + System.lineSeparator(), salida);
        assertTrue(salida.indexOf("Guardando reserva") < salida.indexOf("Correo enviado"));
    }

    @Test
    void reservaInvalidaNoImprimeMensajes() {
        Reserva r = reserva("R-INVALIDA", "sin-arroba", INICIO, INICIO.plusHours(1), "VIP");
        String salida = capturarSalida(() -> assertEquals(0, servicio.procesar(r, 5), 0.001));
        assertTrue(salida.isEmpty());
        assertFalse(r.getEstado() == EstadoReserva.CONFIRMADA);
    }

    private String capturarSalida(Runnable accion) {
        PrintStream anterior = System.out;
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try (PrintStream temporal = new PrintStream(buffer, true, StandardCharsets.UTF_8)) {
            System.setOut(temporal);
            accion.run();
        } finally {
            System.setOut(anterior);
        }
        return buffer.toString(StandardCharsets.UTF_8);
    }
}
