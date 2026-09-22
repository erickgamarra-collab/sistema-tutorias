# Laboratorio 1 — Línea base del código heredado

**Versión original:** commit `9589a9e` en `semana6-labs-ae5`. Fuente de verdad: `ServicioReservas.procesar`, `Reserva` y `Main` en ese commit. Este laboratorio registra hechos y diagnósticos; **no cambia código productivo**.

## Entorno y ejecución

Java 21 y Maven; `mvn -f semana6-lab-diagnostico/pom.xml clean compile`; `java -cp semana6-lab-diagnostico/target/classes edu.uees.refactor.app.Main`. La demostración del proyecto original da: `Guardando reserva R-001`, `Correo enviado a ana@uees.edu.ec`, `Estado: CONFIRMADA`, `Total: 34.0`. Consultar el workflow "Semana 6 — laboratorios y Ae5" de este commit para la ejecución reproducible.

## Comportamiento vs implementación

| Hecho | Clasificación | Fundamento |
|---|---|---|
| Una reserva válida termina CONFIRMADA | Observable | `Reserva.getEstado()` |
| Se imprime "Guardando reserva ..." | Observable | salida estándar |
| El VIP devuelve 34.0 | Observable | retorno de `procesar` |
| Hay un `if` para VIP | Estructura interna | detalle del algoritmo |
| Se almacenan inicio y fin en campos separados | Estructura interna | representación |

## Seis escenarios de línea base

Datos: inicio fijo `2026-09-20T10:00`, fin una hora después salvo LB-04; correo `ana@uees.edu.ec` salvo LB-03. Este cuadro registra el **contrato derivado de ejecutar el algoritmo original**; el Laboratorio 2 lo convierte en tests reproducibles (ver `ServicioReservasTest`).

| ID | Entrada | Estado final | Total | Consola / excepción |
|---|---|---|---:|---|
| LB-01 | NORMAL, correo válido, 5 h | CONFIRMADA | 40.0 | "Guardando reserva R-...", "Correo enviado a ..." en ese orden; sin excepción |
| LB-02 | VIP, correo válido, 5 h | CONFIRMADA | 34.0 | Mismos dos mensajes; sin excepción |
| LB-03 | correo sin @ | PENDIENTE | 0.0 | sin mensajes; sin excepción |
| LB-04 | fin igual o anterior a inicio | PENDIENTE | 0.0 | sin mensajes; sin excepción |
| LB-05 | NORMAL válido, exactamente 2 h | CONFIRMADA | 40.0 | dos mensajes; sin excepción |
| LB-06 | NORMAL válido, 1 h | PENDIENTE | 0.0 | sin mensajes; sin excepción |

También se registra `procesar(null, 5) == 0.0` sin excepción. La validación evalúa correo antes que período y anticipación. `"VIP".equals(tipo)` hace que tipo nulo/no VIP cueste 40 si lo demás es válido.

**No cambiar accidentalmente:** los resultados 40/34/0, umbral inclusivo de dos horas, estados, orden y contenido de mensajes, retorno en lugar de excepciones para entradas inválidas. La consola es una simulación, no almacenamiento ni envío de correos reales.
