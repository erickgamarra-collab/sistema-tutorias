package edu.uees.refactor.service;

import edu.uees.refactor.domain.Reserva;

public class ServicioReservas {
    public double procesar(Reserva r, int horasAnticipacion) {
        if (r == null) {
            return 0;
        }
        if (r.getCorreo() == null || !r.getCorreo().contains("@")) {
            return 0;
        }
        if (r.getInicio() == null || r.getFin() == null
                || !r.getFin().isAfter(r.getInicio())) {
            return 0;
        }
        if (horasAnticipacion < 2) {
            return 0;
        }

        double total = 40;
        if ("VIP".equals(r.getTipo())) {
            total = total * 0.85;
        }

        System.out.println("Guardando reserva " + r.getId());
        System.out.println("Correo enviado a " + r.getCorreo());
        r.confirmar();
        return total;
    }
}
