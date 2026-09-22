# Laboratorio 2 — JUnit 5 y red de seguridad

## Recuperación del proyecto original
El commit `9589a9e` conserva el proyecto previo a los cambios; `719001a` documenta el diagnóstico de Lab 1. Java 21 y Maven. `mvn -f semana6-lab-diagnostico/pom.xml clean test`.

## Suite JUnit 5 / AAA
**Commit dedicado a pruebas:** `c049f6a`. `ServicioReservasTest` tiene 14 tests; `DineroTest`, uno adicional aislado que practica `assertThrows` sin cambiar el contrato de reservas. Cada test organiza datos (Arrange), ejecuta `procesar` (Act) y compara retorno/estado/salida (Assert). Se usan `assertEquals`, `assertAll`, `assertTrue`, `assertFalse` y `assertThrows`.

| Grupo | Pruebas protectoras |
|---|---|
| Normal/alternativo | NORMAL=40, VIP=34, tipo null=40 |
| Inválido | correo sin @, correo null, inicio null, fin null, fin anterior, fin igual, reserva null |
| Límite | 2 h acepta; 1 h rechaza |
| Efectos | mensajes exactos y en orden para reserva válida; silencio y PENDIENTE para inválida |
| Excepción aislada | `Dinero(-1)` lanza IllegalArgumentException con texto esperado |

`Dinero` es **solo** una clase didáctica aislada; no interviene en `ServicioReservas`.

## Primera refactorización protegida
**Antes:** tarifa 40 y condicional VIP estaban en `procesar`. **Después:** `double total = calcularTotal(r);` y método privado con exactamente `40 * 0.85`. Commit `6536265`. Prueba verde antes (`c049f6a`, workflow [35671651112](https://github.com/erickgamarra-collab/sistema-tutorias/actions/runs/35671651112)) y después ([35671705692](https://github.com/erickgamarra-collab/sistema-tutorias/actions/runs/35671705692)); 15 tests, sin fallos.

## Regresión intencional, comprobada en rama aislada
Se creó `semana6-demostracion-regresion` **desde el commit verde de la micro-refactorización**. Único cambio deliberado: `40 * 0.85` → `40 * 0.80`, commit `2516bb4`; PR de demostración [#3](https://github.com/erickgamarra-collab/sistema-tutorias/pull/3), cerrado **sin fusionar**. La ejecución real de GitHub Actions [35671943445](https://github.com/erickgamarra-collab/sistema-tutorias/actions/runs/35671943445) registró:

```text
ServicioReservasTest.vipActualmenteRetornaTreintaYCuatroYConfirma
expected: <34.0> but was: <32.0>
Tests run: 15, Failures: 1, Errors: 0, Skipped: 0
BUILD FAILURE
```

La rama de entrega `semana6-labs-ae5` **nunca recibió esa mutación**; conserva 0.85 y pruebas verdes. La regresión evidencia que el test no es decorativo.

## Reflexión técnica (250–350 palabras)

La prueba que más confianza aportó fue la de VIP, porque comprueba simultáneamente un valor preciso y el estado final. Una modificación aparentemente pequeña de 0.85 a 0.80 provoca que la reserva siga confirmándose, pero el total cambie de 34 a 32. La ejecución fallida, aislada de la rama de entrega, demostró que JUnit detecta esa regresión; el resultado anterior no dependía solamente de confiar en que el nuevo método fuese equivalente. También resultaron importantes las pruebas que capturan el texto y el orden de los dos mensajes de consola: al extraer responsabilidades podría conservarse el total y, sin embargo, perderse la notificación o invertirse la secuencia observable.

Los casos límite de una y dos horas protegen un riesgo distinto. La diferencia entre `< 2` y `<= 2` podría pasar desapercibida si solo probáramos cinco horas de anticipación. Al fijar fechas independientes del reloj real, las ejecuciones son repetibles; al comprobar tanto retorno como estado, una reserva rechazada no podría pasar inadvertidamente a CONFIRMADA. Las fechas nulas, iguales e invertidas protegen además el contrato de período antes de agrupar los dos campos en un Value Object.

Una prueba de caracterización no afirma que toda regla heredada sea ideal, sino que documenta lo que el sistema hace hoy. Por ejemplo, un correo inválido retorna cero sin lanzar excepción. Introducir una clase Correo que lo rechace en su constructor cambiaría ese contrato, aunque parezca una validación mejor. Por eso el ejercicio aislado con Dinero permite practicar `assertThrows` sin imponer excepciones nuevas a la reserva.

El commit de tests precede al de `Extract Method`, y cada refactorización posterior tiene una ejecución verde identificable. Es esa combinación de escenario específico, fallo demostrable, restauración del contrato y trazabilidad Git la que hace posible defender una mejora de diseño sin modificar silenciosamente la funcionalidad.
