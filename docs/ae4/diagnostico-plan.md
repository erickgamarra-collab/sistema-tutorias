# Ae4 - Matriz de Code Smells y plan de refactorización

## Matriz de Code Smells

| Código / ubicación | Smell | Evidencia concreta | Impacto |
|---|---|---|---|
| `Reserva.cancelar()` y `Reserva.reprogramar()` | Condicional duplicado | Ambos métodos repiten la condición `estado == CANCELADA || estado == COMPLETADA`. | Si cambia la definición de estado final, la misma regla debe modificarse en varios lugares y puede quedar inconsistente. |
| `ServicioReservas.reprogramarReserva()` | Método largo / responsabilidades mezcladas | En un solo método se busca la reserva, se valida el nuevo horario, se valida el docente, se ocupa el horario, se ejecuta la reprogramación, se hace rollback, se persiste y se notifica. | Dificulta leer la intención principal y aumenta el costo de modificar o revisar el flujo. |
| `ServicioReservas` | Código duplicado | Varias operaciones repiten `reservaRepository.guardar(reserva)` seguido de `notificarCambio(reserva, ...)`. | La secuencia de persistencia y publicación puede divergir entre casos de uso y obliga a repetir cambios. |
| `ServicioReservas` | Magic Strings | Los eventos `Nueva tutoría solicitada.`, `Tutoría confirmada.`, `Tutoría cancelada.`, `Tutoría reprogramada.` y `Tutoría completada.` están embebidos directamente en los métodos. | Los textos de eventos quedan dispersos y son más difíciles de localizar y mantener. |

## Plan de refactorización

| Prioridad | Problema | Refactorización | Justificación |
|---:|---|---|---|
| 1 | Regla de estado final repetida en `Reserva` | **Extract Method**: extraer una validación privada de estado no final. | Es un cambio pequeño, de bajo riesgo y elimina una duplicación dentro del dominio. |
| 2 | `reprogramarReserva()` mezcla demasiados pasos | **Extract Method**: separar validación y movimiento seguro del horario. | Hace visible la intención del caso de uso sin cambiar el algoritmo ni sus excepciones. |
| 3 | Persistir y notificar se repite en varios casos | **Extract Method**: centralizar la secuencia en `registrarCambio`. | Reduce duplicación y garantiza el mismo orden de persistencia y publicación. |
| 4 | Textos de eventos dispersos | **Introduce Constant**: declarar constantes privadas para los mensajes. | Evita literales repetidos y concentra nombres del flujo observable sin cambiar su contenido. |

## Criterio de seguridad

Después de **cada** refactorización se ejecutarán las pruebas de Ae3 y las cuatro pruebas de caracterización de Ae4. Los casos de línea base deben continuar produciendo exactamente los mismos estados y efectos observables.
