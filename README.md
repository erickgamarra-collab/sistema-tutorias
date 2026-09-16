# Sistema de gestión de tutorías — Ae4 Refactorización

Proyecto de **Diseño de Software (UCOM0310)** evolucionado desde Ae1, Ae2 y Ae3. La versión actual corresponde a **Ae4 – Kata de refactorización: Antes y después**.

## Propósito

Gestionar tutorías entre estudiantes y docentes mediante un diseño orientado a objetos. El sistema permite publicar horarios, solicitar, confirmar, cancelar, reprogramar y completar reservas, manteniendo separadas las reglas del dominio, persistencia, notificación, políticas variables y reacciones a eventos.

## Patrones existentes de Ae3

El proyecto conserva los patrones implementados en el incremento anterior:

- **Builder:** `ReservaBuilder` construye reservas con datos obligatorios y opcionales.
- **Factory Method:** `NotificacionFactory` y sus fábricas concretas crean diferentes notificadores.
- **Strategy:** `PoliticaCancelacion` permite intercambiar políticas de cancelación.
- **Observer:** `ReservaObserver` desacopla notificación y auditoría de los cambios de una reserva.

## Ae4 - Objetivo de la refactorización

Ae4 no agrega funcionalidades. La actividad mejora la estructura interna y verifica que el comportamiento observable se conserve.

La línea base utilizada fue la versión de Ae3 integrada en `main` en el commit:

```text
2b7e7d9440b6e33d0c3121f404146560b62ef925
```

### Code Smells identificados

| Ubicación | Smell | Mejora aplicada |
|---|---|---|
| `Reserva.cancelar()` y `Reserva.reprogramar()` | Condicional duplicado | Extract Method: `validarEstadoNoFinal(...)` |
| `ServicioReservas.reprogramarReserva()` | Método largo / responsabilidades mezcladas | Extract Method: `validarReprogramacion(...)` y `moverHorario(...)` |
| `ServicioReservas` | Secuencia repetida de persistencia y notificación | Extract Method: `registrarCambio(...)` |
| `ServicioReservas` | Magic Strings de eventos | Introduce Constant: constantes `EVENTO_*` |

El diagnóstico completo está en [`docs/ae4/diagnostico-plan.md`](docs/ae4/diagnostico-plan.md).

## Línea base y preservación del comportamiento

Se añadieron cuatro pruebas de caracterización en `Ae4LineaBaseTest` para congelar los casos principales antes de modificar el código:

1. Solicitar tutoría -> `SOLICITADA`, horario ocupado y reserva persistida.
2. Confirmar tutoría -> `CONFIRMADA`.
3. Cancelar tutoría -> `CANCELADA` y horario liberado.
4. Reprogramar tutoría -> `REPROGRAMADA`, horario anterior libre y nuevo ocupado.

La evidencia inicial está en [`docs/ae4/linea-base.md`](docs/ae4/linea-base.md).

## Refactorizaciones realizadas

### 1. Extraer validación de estado final

La condición que impedía operar sobre reservas `CANCELADA` o `COMPLETADA` estaba repetida. Se extrajo a:

```java
private void validarEstadoNoFinal(String mensaje)
```

### 2. Extraer pasos de reprogramación

`reprogramarReserva()` mezclaba varias responsabilidades. Ahora delega en:

```java
validarReprogramacion(reserva, nuevoHorario);
moverHorario(reserva, nuevoHorario);
```

El rollback existente se conserva.

### 3. Centralizar persistencia y notificación

La secuencia repetida se concentró en:

```java
private void registrarCambio(Reserva reserva, String evento)
```

La reprogramación conserva su persistencia dentro del bloque protegido por rollback para no cambiar el comportamiento ante errores.

### 4. Reemplazar mensajes literales por constantes

Los textos de eventos se concentraron en constantes privadas `EVENTO_*` sin modificar su contenido.

## Comparación antes/después

La comparación detallada, fragmentos de código, tabla de preservación y conclusión están en [`docs/ae4/evidencia-despues.md`](docs/ae4/evidencia-despues.md).

## Verificación

Requiere JDK 17 y Maven.

```bash
mvn clean compile
mvn clean test
java -cp target/classes edu.uees.tutorias.App
```

La verificación automática final obtuvo:

```text
Tests run: 9, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

Los 9 tests corresponden a 5 pruebas de Ae3 y 4 pruebas de caracterización de Ae4. La demostración también conserva el flujo:

```text
SOLICITADA -> CONFIRMADA -> CANCELADA
```

## Historial incremental de Ae4

```text
975e4fa refactor: reemplazar mensajes de eventos por constantes
87b5356 refactor: centralizar persistencia y notificacion
e4908de refactor: extraer pasos de reprogramacion
67337e5 refactor: extraer validacion de estado final
0b6e2d1 docs: diagnosticar code smells y planificar Ae4
b279e4b chore: registrar linea base de Ae4
```

Este historial evidencia el ciclo solicitado: refactorización -> compilación/pruebas -> comparación -> commit -> siguiente cambio.

## Estructura principal

```text
sistema-tutorias/
├── README.md
├── pom.xml
├── docs/
│   ├── modelo-clases.puml
│   ├── uml-incremento1.puml
│   └── ae4/
│       ├── linea-base.md
│       ├── diagnostico-plan.md
│       └── evidencia-despues.md
├── src/
│   ├── main/java/edu/uees/tutorias/
│   │   ├── App.java
│   │   ├── builder/
│   │   ├── domain/
│   │   ├── factory/
│   │   ├── notification/
│   │   ├── observer/
│   │   ├── repository/
│   │   ├── service/
│   │   └── strategy/
│   └── test/java/edu/uees/tutorias/
│       ├── Ae3PatternsTest.java
│       └── Ae4LineaBaseTest.java
└── .github/workflows/
    └── ae3-ci.yml
```

## Repositorio

https://github.com/erickgamarra-collab/sistema-tutorias

Rama de Ae4: `ae4-refactorizacion`.

## Declaración de uso de inteligencia artificial

Durante Ae4 se utilizaron herramientas de inteligencia artificial como apoyo para analizar el código existente, identificar señales de Code Smells, proponer refactorizaciones pequeñas, revisar que no se introdujeran reglas de negocio nuevas, preparar pruebas de caracterización y organizar la documentación técnica. Cada cambio fue verificado mediante compilación, pruebas automatizadas y ejecución del programa. El estudiante debe comprender y poder defender las decisiones presentadas.
