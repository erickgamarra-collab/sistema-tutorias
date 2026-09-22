# Semana 6 — Laboratorio 1, Laboratorio 2 y Ae5

Este módulo es el **caso heredado independiente de Semana 6**, dentro de `sistema-tutorias`. No reemplaza el proyecto Ae3/Ae4 de la raíz; preserva los contratos del código que proporcionó el docente. **Java 21, Maven, JUnit 5.**

## Ejecutar desde la raíz del repositorio

```bash
mvn -f semana6-lab-diagnostico/pom.xml clean compile
mvn -f semana6-lab-diagnostico/pom.xml clean test
java -cp semana6-lab-diagnostico/target/classes edu.uees.refactor.app.Main
```

O desde esta carpeta: `mvn clean test`. En GitHub, workflow **Semana 6 — laboratorios y Ae5** realiza estas verificaciones. El ejemplo imprime `Guardando reserva R-001`, `Correo enviado a ana@uees.edu.ec`, `Estado: CONFIRMADA`, `Total: 34.0`.

## Contrato que debe preservarse

NORMAL=40.0; VIP=34.0; correo/período inválidos o anticipación de una hora retornan 0 y conservan PENDIENTE; exactamente dos horas permite procesar; primero imprime el guardado, después el correo simulado y confirma la reserva. El correo y la persistencia **no son reales**.

## Organización

- `src/main/java/edu/uees/refactor/domain`: `Reserva`, `EstadoReserva`, Value Object `PeriodoReserva`; `Dinero` existe solo para el ejercicio aislado de assertThrows.
- `src/main/java/edu/uees/refactor/service`: `ServicioReservas`, `CalculadoraTarifa`, `RegistroReservaConsola` y `NotificacionReservaConsola`.
- `src/test/java/edu/uees/refactor`: 15 pruebas JUnit en total, incluidas regresiones de NORMAL/VIP, entradas inválidas, casos límite y efectos de consola.
- `docs/lab1`: línea base, observaciones, matriz con seis smells, riesgos, plan y reflexión.
- `docs/lab2`: estrategia AAA, pruebas, micro-refactorización, regresión intencional en PR #3 y reflexión.
- `docs/ae5`: reporte técnico, justificaciones y comparación antes/después.

## Historial y evidencias

| Etapa | Commit | CI |
|---|---|---|
| Original / Lab 1 | `9589a9e` | [verde](https://github.com/erickgamarra-collab/sistema-tutorias/actions/runs/35671517224) |
| Diagnóstico Lab 1 | `719001a` | [verde](https://github.com/erickgamarra-collab/sistema-tutorias/actions/runs/35671605380) |
| JUnit / Lab 2 | `c049f6a` | [verde](https://github.com/erickgamarra-collab/sistema-tutorias/actions/runs/35671651112) |
| Micro-refactorización | `6536265` | [verde](https://github.com/erickgamarra-collab/sistema-tutorias/actions/runs/35671705692) |
| Ae5 Extract Class tarifa | `824fb08` | [verde](https://github.com/erickgamarra-collab/sistema-tutorias/actions/runs/35671754913) |
| Ae5 Extraer efectos | `3c79b15` | [verde](https://github.com/erickgamarra-collab/sistema-tutorias/actions/runs/35671807357) |
| Ae5 Value Object | `c806c04` | [verde](https://github.com/erickgamarra-collab/sistema-tutorias/actions/runs/35671876628) |

**Demostración controlada:** [PR #3 cerrado sin merge](https://github.com/erickgamarra-collab/sistema-tutorias/pull/3) cambia solo factor 0.85→0.80 y [la prueba VIP falla con esperado 34, real 32](https://github.com/erickgamarra-collab/sistema-tutorias/actions/runs/35671943445). No se contaminó la rama de entrega.

## Declaración de inteligencia artificial

Utilicé herramientas de inteligencia artificial como apoyo para construir el caso de la guía, plantear mejoras y revisar documentación y pruebas. Verifiqué los resultados mediante compilación, JUnit y ejecución automatizada. **Asumo la responsabilidad de comprender y defender las decisiones presentadas.**
