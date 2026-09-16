# Ae4 - Evidencia DESPUÉS y comparación técnica

## Resultado de verificación final

La versión refactorizada fue verificada en GitHub Actions sobre el commit `975e4fa6a6a151aca91988ac53265ea6d49e5903`.

Comandos ejecutados:

```bash
mvn -B clean test
java -cp target/classes edu.uees.tutorias.App
```

Resultado:

```text
Tests run: 9, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

La demostración conservó el flujo observable:

```text
SOLICITADA -> CONFIRMADA -> CANCELADA
```

## Evidencia de preservación

| Caso | Antes | Después | ¿Preservado? |
|---|---|---|---|
| 1. Solicitar tutoría | `SOLICITADA`, horario ocupado, reserva persistida | Mismo resultado | Sí |
| 2. Confirmar tutoría | `CONFIRMADA` | `CONFIRMADA` | Sí |
| 3. Cancelar tutoría | `CANCELADA` y horario liberado | Mismo resultado | Sí |
| 4. Reprogramar tutoría | `REPROGRAMADA`, horario anterior libre y nuevo ocupado | Mismo resultado | Sí |

Los cuatro casos están automatizados en `Ae4LineaBaseTest` y se ejecutaron junto con las cinco pruebas de Ae3.

## Refactorización 1 - Extraer validación de estado final

**Smell:** condicional duplicado en `Reserva.cancelar()` y `Reserva.reprogramar()`.

Antes:

```java
if (estado == EstadoReserva.CANCELADA || estado == EstadoReserva.COMPLETADA) {
    throw new IllegalStateException(...);
}
```

Después:

```java
private void validarEstadoNoFinal(String mensaje) {
    if (estado == EstadoReserva.CANCELADA || estado == EstadoReserva.COMPLETADA) {
        throw new IllegalStateException(mensaje);
    }
}
```

Se conservan los mismos tipos y textos de excepción.

## Refactorización 2 - Extraer pasos de reprogramación

**Smell:** `reprogramarReserva()` mezclaba validación, movimiento del horario, rollback, persistencia y notificación.

Antes, el método contenía todos esos pasos directamente. Después, su intención principal queda visible:

```java
Reserva reserva = obtenerReserva(reservaId);
validarReprogramacion(reserva, nuevoHorario);
moverHorario(reserva, nuevoHorario);
notificarCambio(reserva, EVENTO_REPROGRAMADA);
```

`validarReprogramacion(...)` y `moverHorario(...)` conservan el orden de validaciones y el rollback previo.

## Refactorización 3 - Centralizar persistencia y notificación

**Smell:** varios casos de uso repetían la misma secuencia:

```java
reservaRepository.guardar(reserva);
notificarCambio(reserva, evento);
```

Después:

```java
private void registrarCambio(Reserva reserva, String evento) {
    reservaRepository.guardar(reserva);
    notificarCambio(reserva, evento);
}
```

La reprogramación mantiene su persistencia dentro del bloque protegido por rollback para no alterar su comportamiento frente a una excepción del repositorio.

## Refactorización 4 - Reemplazar Magic Strings por constantes

**Smell:** los textos de eventos estaban dispersos dentro de los métodos del servicio.

Después se concentran en constantes privadas como:

```java
private static final String EVENTO_CONFIRMADA = "Tutoría confirmada.";
private static final String EVENTO_CANCELADA = "Tutoría cancelada.";
```

Los valores de texto no cambiaron, por lo que la salida de los observers se conserva.

## Comparación técnica antes/después

| Dimensión | Antes | Después | Evidencia |
|---|---|---|---|
| Nombres | La intención de pasos internos estaba oculta dentro de métodos grandes | `validarEstadoNoFinal`, `validarReprogramacion`, `moverHorario`, `registrarCambio` explicitan intención | Código final |
| Métodos / responsabilidades | `reprogramarReserva()` concentraba múltiples tareas | Coordinación principal separada de validación y movimiento seguro | Refactor 2 |
| Condicionales / flujo | Regla de estado final duplicada | Una validación privada compartida | Refactor 1 |
| Constantes / reglas | Mensajes de eventos como literales | Constantes `EVENTO_*` | Refactor 4 |
| Comportamiento | 4 casos de línea base + pruebas Ae3 | Los mismos 9 tests pasan sin fallos | GitHub Actions |
| Git | Código Ae3 integrado | Commits pequeños por diagnóstico y refactorización | Historial de rama |

## Historial principal de Ae4

```text
975e4fa refactor: reemplazar mensajes de eventos por constantes
87b5356 refactor: centralizar persistencia y notificacion
e4908de refactor: extraer pasos de reprogramacion
67337e5 refactor: extraer validacion de estado final
0b6e2d1 docs: diagnosticar code smells y planificar Ae4
b279e4b chore: registrar linea base de Ae4
```

## Conclusión

El código inicial cumplía su función, pero presentaba duplicación de condicionales, responsabilidades mezcladas, secuencias repetidas y literales de eventos dispersos. Se aplicaron `Extract Method` e `Introduce Constant` de manera incremental. Después de cada refactorización se ejecutaron compilación, pruebas y demostración, conservando los mismos resultados observables. La versión final mejora legibilidad, mantenibilidad e intención sin incorporar nuevas reglas de negocio.
