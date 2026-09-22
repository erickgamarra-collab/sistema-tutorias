# Laboratorio 1 — Diagnóstico, riesgo y plan

## Mapa de responsabilidades original

| Fragmento | Responsabilidad | Dueño actual |
|---|---|---|
| null/correo/período/anticipación | validar elegibilidad | ServicioReservas |
| 40 y 0.85 para VIP | tarifa | ServicioReservas |
| println "Guardando" | persistencia simulada | ServicioReservas |
| println "Correo" | notificación simulada | ServicioReservas |
| `confirmar()` | transición de estado | Reserva |

`ServicioReservas` tiene motivos de cambio independientes: reglas de aceptación, tarifas, persistencia simulada y notificaciones. `Reserva` conserva el estado. Las llamadas `getCorreo/getInicio/getFin/getTipo` no son automáticamente Feature Envy: validar reglas del caso de uso es tarea del servicio; la cohorte inicio/fin puede ser un concepto del dominio.

## Matriz de diagnóstico (seis hallazgos concretos)

| # | Ubicación | Smell / problema | Categoría | Impacto / riesgo | Candidato | Prueba necesaria |
|---|---|---|---|---|---|---|
| 1 | `ServicioReservas.procesar` | validación, tarifa y efectos mezclados | responsabilidades / Long Method | cambios en tarifa rozan efectos y validaciones | Extract Class tarifa / reorganizar | NORMAL=40, VIP=34 |
| 2 | `procesar` y `Reserva` | inicio y fin como primitivos relacionados | Data Clumps | invariantes de período se dispersan | Value Object `PeriodoReserva` | fin anterior/igual/null retorna 0 y PENDIENTE |
| 3 | `procesar` | consola de persistencia dentro del servicio | infraestructura mezclada | difícil aislar cambios de registro | Extract Class `RegistroReservas` | dos mensajes en orden |
| 4 | `procesar` | consola de correo dentro del servicio | infraestructura mezclada | notificación acoplada al proceso | Extract Class `NotificacionReservas` | correo solo en reserva válida |
| 5 | `procesar` | número 40, factor 0.85 y literal VIP | Magic Numbers / Primitive Obsession | tarifa y variación poco expresivas | `CalculadoraTarifa` con constantes | 40/34, no VIP=40 |
| 6 | `Reserva` constructor | cinco argumentos, dos fechas acopladas | Long Parameter List | construcción opaca y más cambio al extender | compatibilidad + Value Object interno | construcción original y getters sin cambios |

**Condicionales y contratos:** `r==null` evita acceso a null; correo null/sin @ rechaza; inicio/fin null o `fin<=inicio` rechaza; `horas<2` rechaza; tipo VIP descuenta. Los retornos tempranos existentes son legibles; refactorizarlos por estética no es prioridad.

## Matriz de riesgo

| Cambio | Probabilidad | Impacto | Nivel | Mitigación |
|---|---|---|---|---|
| Extraer tarifas | baja | alto si cambia 34 | medio | NORMAL/VIP/no VIP |
| Separar notificación / persistencia | media | alto si cambia orden o texto | alto | capturar consola y estados |
| Encapsular período | alta | alto si excepción sustituye retorno 0 | alto | null, fin igual/anterior y getters |
| Simplificar condiciones | media | medio | medio | null, 1 h y 2 h |
| Introducir Correo validante | alta | alto | alto | no hacerlo mientras contrato inválido sea retorno 0 |

## Pruebas protectoras propuestas

NORMAL: total 40 y CONFIRMADA; VIP: total 34 y CONFIRMADA; correo inválido: 0, PENDIENTE y silencio; período inválido: idem; 2 h exactas: confirma; 1 h: rechaza; reserva null: 0; mensajes de salida exactos y ordenados. Para Value Object, conservar constructor público original, getters y retorno ante fechas inválidas.

## Plan priorizado

1. Fijar línea base y versionar código original (sin cambiar producción).
2. Crear JUnit 5 y pruebas de los seis casos y null; registrar commit separado.
3. Micro-refactor `calcularTotal` protegido por NORMAL/VIP.
4. Extract Class para tarifa, con tests verdes y commit.
5. Extract Class para efectos de consola, preservando orden, con tests y commit.
6. Value Object para período, conservando el contrato sobre entradas inválidas, con tests y commit.

La selección prioriza primero lo simple y observable; los cambios con riesgo de constructor/excepciones se realizan solo después de cobertura adicional.

## Reflexión técnica (Laboratorio 1, 250–350 palabras)

El principal riesgo del servicio original no es que falle en la demostración, sino que reúne decisiones con motivos de cambio distintos. Un nuevo precio VIP podría requerir alterar un método que también valida el correo y el período, imprime mensajes y confirma la reserva. Ese acoplamiento aumenta la superficie de regresión y dificulta identificar qué comportamiento debe permanecer igual. El `if` de VIP es pequeño, pero está situado entre validaciones y efectos externos; extraerlo tiene sentido si queda protegido por pruebas de NORMAL y VIP. En cambio, eliminar ese condicional o imponer un formato de correo más estricto solo porque parece elegante introduciría cambios de negocio no autorizados.

El riesgo más sutil es convertir los dos campos de fecha en un objeto que rechace todo período inválido al construirse. En el código actual la reserva puede existir con fechas inválidas y el servicio devuelve cero sin confirmarla. Si el constructor nuevo lanzara una excepción, habríamos cambiado el contrato observable. Por ello, primero debemos caracterizar fechas nulas, iguales o invertidas, y conservar una ruta que permita al servicio decidir el rechazo. De forma similar, no debemos reemplazar los mensajes de consola sin comprobar texto y orden: aunque sean una simulación, son efectos observables del ejercicio.

La primera responsabilidad que separaría sería el cálculo de tarifa, porque los resultados esperados son concretos y fáciles de asegurar. Después aislaría los dos efectos de consola en clases que expresen su intención, preservando la secuencia guardar, notificar y confirmar. Finalmente encapsularía el período, un cambio con más riesgo al afectar la representación interna y la construcción del objeto. El plan escalonado evita acumular cambios no verificados: cada paso comienza y termina con pruebas verdes y deja un commit que permite localizar o revertir una regresión. La evidencia importante es la comparación reproducible entre el comportamiento inicial y el final, no solamente que el código se vea más corto.
