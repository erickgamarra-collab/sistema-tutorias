# Sistema de gestión de tutorías

Proyecto desarrollado para la **Actividad 5 | Ae1 - Diseño orientado a objetos de un sistema** de la asignatura **Diseño de Software (UCOM0310)**.

## Propósito

Modelar una solución orientada a objetos para gestionar tutorías entre estudiantes y docentes. El sistema permite publicar horarios, solicitar una tutoría, confirmar, cancelar, reprogramar y completar reservas, además de notificar eventos relevantes y aislar la persistencia detrás de una abstracción.

## Problema abordado

El dominio requiere coordinar estudiantes que solicitan tutorías, docentes que publican horarios y reservas que cambian de estado. La lógica de negocio no debe depender directamente de una base de datos ni de una tecnología específica de notificación.

## Clases principales y responsabilidades

- `Usuario`: abstracción con la información común de los usuarios.
- `Estudiante`: representa a quien solicita la tutoría.
- `Docente`: representa al tutor y administra sus horarios publicados.
- `HorarioDisponible`: encapsula una franja de tiempo y su disponibilidad.
- `Reserva`: registra el encuentro y protege las transiciones de estado.
- `ServicioReservas`: coordina los casos de uso de solicitar, confirmar, cancelar, reprogramar y completar.
- `ReservaRepository`: interfaz que desacopla la lógica de aplicación de la tecnología de persistencia.
- `MemoriaReservaRepository`: implementación en memoria del repositorio.
- `Notificador`: interfaz para comunicar eventos.
- `NotificadorConsola`: implementación básica de notificación por consola.

## Decisiones de diseño

1. **Herencia para usuarios**: `Estudiante` y `Docente` heredan de `Usuario` porque ambos son tipos de usuario y comparten identidad, nombre y correo.
2. **Composición para horarios**: un `Docente` publica y administra una colección de `HorarioDisponible`; el horario tiene sentido dentro de la disponibilidad de un docente.
3. **Encapsulación de reglas**: `HorarioDisponible` protege su estado de reservado/libre y `Reserva` controla sus transiciones de estado.
4. **Persistencia aislada**: `ServicioReservas` depende de `ReservaRepository`, no de una base de datos concreta.
5. **Notificación desacoplada**: `ServicioReservas` depende de `Notificador`, lo que permite sustituir la implementación sin cambiar los casos de uso.

## Principios SOLID aplicados

### SRP - Single Responsibility Principle

Las responsabilidades se separan por motivo de cambio. `Reserva` gestiona el estado del encuentro, `HorarioDisponible` gestiona disponibilidad, `ServicioReservas` coordina casos de uso, el repositorio se ocupa de persistencia y `Notificador` de comunicación.

### DIP - Dependency Inversion Principle

`ServicioReservas` recibe `ReservaRepository` y `Notificador` mediante el constructor. Por ello la lógica no depende de `MemoriaReservaRepository` ni de `NotificadorConsola`; depende de abstracciones.

### OCP - Open/Closed Principle

Se pueden agregar nuevas implementaciones, por ejemplo un repositorio basado en base de datos o un notificador por correo, implementando las interfaces existentes sin modificar `ServicioReservas`.

## Diagrama UML

- Fuente PlantUML: [`docs/modelo-clases.puml`](docs/modelo-clases.puml)
- Imagen: [`docs/modelo-clases.png`](docs/modelo-clases.png)

## Estructura

```text
sistema-tutorias/
├── README.md
├── pom.xml
├── docs/
│   ├── modelo-clases.puml
│   └── modelo-clases.png
└── src/
    └── main/
        └── java/
            └── edu/uees/tutorias/
                ├── App.java
                ├── domain/
                ├── notification/
                ├── repository/
                └── service/
```

## Requisitos

- JDK 17 o superior.
- Apache Maven 3.8 o superior.

## Compilación

Desde la raíz del proyecto:

```bash
mvn clean compile
```

Para ejecutar el ejemplo después de compilar:

```bash
java -cp target/classes edu.uees.tutorias.App
```

## Repositorio GitHub

**URL:** https://github.com/erickgamarra-collab/sistema-tutorias.git

## Declaración de uso de inteligencia artificial

Durante el desarrollo de esta actividad utilicé herramientas de inteligencia artificial como apoyo para organizar el análisis del dominio, revisar la distribución de responsabilidades, proponer una estructura inicial del código y mejorar la redacción de la documentación. Verifiqué y adapté el contenido generado, revisé la coherencia entre UML y código y asumo la responsabilidad de comprender y justificar las decisiones presentadas.
