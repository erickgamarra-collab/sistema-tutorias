package edu.uees.refactor.domain;

import java.time.LocalDateTime;

public class Reserva {
    private final String id;
    private final String correo;
    private final PeriodoReserva periodo;
    private final String tipo;
    private EstadoReserva estado = EstadoReserva.PENDIENTE;

    /** Firma heredada preservada para clientes existentes. */
    public Reserva(String id, String correo, LocalDateTime inicio,
                   LocalDateTime fin, String tipo) {
        this.id = id;
        this.correo = correo;
        this.periodo = new PeriodoReserva(inicio, fin);
        this.tipo = tipo;
    }

    public void confirmar() {
        estado = EstadoReserva.CONFIRMADA;
    }

    public String getId() { return id; }
    public String getCorreo() { return correo; }
    public LocalDateTime getInicio() { return periodo.inicio(); }
    public LocalDateTime getFin() { return periodo.fin(); }
    public PeriodoReserva getPeriodo() { return periodo; }
    public String getTipo() { return tipo; }
    public EstadoReserva getEstado() { return estado; }
}
