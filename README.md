# Sistema de gestión de tutorías — Ae3 Incremento 1

Proyecto de **Diseño de Software (UCOM0310)** evolucionado a partir de Ae1 y Ae2 para el **Ae3 – Incremento 1 del proyecto**.

## Propósito

Gestionar tutorías entre estudiantes y docentes manteniendo separadas las reglas del dominio, la persistencia, la creación de objetos, las políticas variables y las reacciones ante eventos.

## Estado inicial recuperado de Ae1

La base del sistema conserva las entidades `Usuario`, `Estudiante`, `Docente`, `HorarioDisponible`, `Reserva` y `EstadoReserva`, además de `ReservaRepository`, `MemoriaReservaRepository`, `Notificador` y `ServicioReservas`.

La lógica principal continúa dependiendo de abstracciones para evitar acoplamiento directo con tecnologías concretas.

## Patrones recuperados de Ae2

### Builder

Se mantiene `ReservaBuilder` porque `Reserva` ahora maneja datos obligatorios y opcionales:

- `id`, `estudiante` y `horario` son obligatorios.
- `tema`, `observaciones` y `enviarRecordatorio` son opcionales.
- El estado inicial permanece controlado como `SOLICITADA`.

Esto evita constructores extensos y permite una construcción progresiva y legible.

### Factory Method

Se mantiene Factory Method para crear distintas implementaciones de `Notificador` sin acoplar al cliente a clases concretas.

Participantes principales:

- `NotificacionFactory`
- `EmailFactory`
- `SmsFactory`
- `WhatsAppFactory`
- `PushFactory`
- `NotificadorEmail`
- `NotificadorSms`
- `NotificadorWhatsApp`
- `NotificadorPush`

## Problemas de diseño identificados para Ae3

### 1. Política de cancelación variable

La regla para permitir una cancelación puede cambiar de manera independiente del resto del caso de uso. Mantenerla fija dentro de `ServicioReservas` aumenta el acoplamiento entre coordinación y política.

**Solución:** patrón **Strategy**.

- `PoliticaCancelacion`
- `CancelacionEstandar`
- `CancelacionSoloSolicitada`

`ServicioReservas` trabaja contra la interfaz `PoliticaCancelacion`, por lo que la estrategia puede cambiar sin modificar el servicio.

### 2. Múltiples componentes reaccionan a cambios de una reserva

Notificaciones, auditoría u otros componentes pueden necesitar enterarse cuando una reserva se solicita, confirma, cancela, reprograma o completa.

**Solución:** patrón **Observer**.

- `ReservaObserver`
- `NotificacionReservaObserver`
- `AuditoriaReservaObserver`

`ServicioReservas` mantiene una colección de observers y publica los eventos sin conocer los detalles de cada reacción.

## Patrones utilizados

| Patrón | Uso en el proyecto |
|---|---|
| Builder | Construcción progresiva de `Reserva` |
| Factory Method | Creación de canales de notificación |
| Strategy | Políticas intercambiables de cancelación |
| Observer | Reacción desacoplada a cambios de reserva |

## Principios SOLID relacionados

### SRP — Single Responsibility Principle

- `Reserva` protege su ciclo de vida.
- `ReservaBuilder` se ocupa de construir reservas.
- Las fábricas se ocupan de crear notificadores.
- Las estrategias encapsulan políticas de cancelación.
- Los observers encapsulan reacciones ante eventos.
- `ServicioReservas` coordina el caso de uso.

### OCP — Open/Closed Principle

Se pueden agregar nuevos canales de notificación, nuevas políticas de cancelación u observers sin reescribir las clases existentes que dependen de las abstracciones.

### DIP — Dependency Inversion Principle

`ServicioReservas` depende de `ReservaRepository`, `PoliticaCancelacion` y `ReservaObserver`, no de implementaciones concretas de infraestructura o políticas.

## Estructura principal

```text
sistema-tutorias/
├── README.md
├── pom.xml
├── docs/
│   ├── modelo-clases.puml
│   └── uml-incremento1.puml
└── src/
    ├── main/java/edu/uees/tutorias/
    │   ├── App.java
    │   ├── builder/
    │   ├── domain/
    │   ├── factory/
    │   ├── notification/
    │   ├── observer/
    │   ├── repository/
    │   ├── service/
    │   └── strategy/
    └── test/java/edu/uees/tutorias/
        └── Ae3PatternsTest.java
```

## Compilar

Requiere JDK 17 y Maven.

```bash
mvn clean compile
```

## Ejecutar pruebas

```bash
mvn clean test
```

Las pruebas verifican:

- valores por defecto de Builder;
- intercambio de políticas Strategy;
- publicación de eventos Observer;
- rechazo de una cancelación cuando la estrategia seleccionada no la permite.

## Ejecutar la demostración

```bash
java -cp target/classes edu.uees.tutorias.App
```

La demostración crea una reserva, utiliza `EmailFactory`, registra observers, confirma la reserva y posteriormente la cancela utilizando `CancelacionEstandar`.

## UML del incremento

Fuente PlantUML actualizada:

- [`docs/uml-incremento1.puml`](docs/uml-incremento1.puml)

El diagrama incluye las clases del dominio y las relaciones introducidas por Builder, Factory Method, Strategy y Observer.

## Repositorio

https://github.com/erickgamarra-collab/sistema-tutorias

Rama de desarrollo del Ae3: `ae3-incremento1`.

## Declaración de uso de inteligencia artificial

Durante el desarrollo se utilizaron herramientas de inteligencia artificial como apoyo para revisar la continuidad con Ae1 y Ae2, proponer e implementar la integración de patrones, revisar coherencia entre responsabilidades, código y UML, y mejorar la documentación. El código y las decisiones deben ser revisados, ejecutados y comprendidos por el estudiante antes de su entrega.
