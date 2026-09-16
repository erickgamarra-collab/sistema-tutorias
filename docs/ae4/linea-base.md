# Ae4 - Línea base ANTES de refactorizar

## Estado inicial

La kata Ae4 parte de la versión de Ae3 integrada en `main` en el commit:

`2b7e7d9440b6e33d0c3121f404146560b62ef925`

En esta fase no se modificó código de producción. Se añadieron pruebas de caracterización para congelar el comportamiento observable antes de cualquier refactorización.

## Qué hace el sistema

El sistema permite publicar horarios de tutoría, solicitar una reserva, confirmarla, cancelarla, reprogramarla y completarla. La versión inicial también integra Builder, Factory Method, Strategy y Observer.

## Comandos de verificación

```bash
mvn clean compile
mvn clean test
java -cp target/classes edu.uees.tutorias.App
```

## Casos de línea base

| Caso | Comportamiento observable que debe conservarse |
|---|---|
| 1. Solicitar tutoría | La reserva queda `SOLICITADA`, el horario queda ocupado y la reserva se persiste. |
| 2. Confirmar tutoría | Una reserva solicitada pasa a `CONFIRMADA`. |
| 3. Cancelar tutoría | Una reserva confirmada pasa a `CANCELADA` y el horario queda nuevamente disponible. |
| 4. Reprogramar tutoría | La reserva pasa a `REPROGRAMADA`, mantiene el mismo docente, libera el horario anterior y ocupa el nuevo. |

Estos cuatro casos se encuentran automatizados en `Ae4LineaBaseTest` y se utilizarán después de cada refactorización para demostrar preservación del comportamiento.

## Código inicial relevante

Los principales puntos que se analizarán sin cambiar resultados son:

- `domain/Reserva.java`: reglas de transición de estado.
- `service/ServicioReservas.java`: coordinación de persistencia, validaciones y publicación de eventos.
- `observer/*`: reacción ante cambios de las reservas.
- `strategy/*`: política variable de cancelación.

## Regla de la kata

A partir de esta línea base, cada cambio debe seguir el ciclo:

**refactorizar -> compilar/probar -> comparar -> commit -> siguiente refactorización**.

No se agregarán funcionalidades ni reglas de negocio nuevas.
