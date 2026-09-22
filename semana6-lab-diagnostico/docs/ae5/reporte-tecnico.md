# Ae5 — Reporte técnico breve: refactorización respaldada por pruebas

**Erick Gamarra · Diseño de Software UCOM0310 · Semana 6 · PEL 4, 2026.**

## 1. Problema y estado inicial
Caso heredado entregado para Semana 6, aislado del proyecto Ae3/Ae4 en la carpeta `semana6-lab-diagnostico`. Commit base `9589a9e`. `ServicioReservas.procesar` mezclaba validación, tarifa, persistencia y notificación simuladas y confirmación. `Reserva` almacenaba las fechas de inicio/fin por separado.

## 2. Línea base y diagnóstico
Lab 1 en `docs/lab1/linea-base-y-observaciones.md` y `diagnostico-plan.md`. Seis escenarios LB-01 a LB-06, seis hallazgos concretos, mapa de responsabilidades, matriz de riesgos y plan priorizado. Comportamiento: NORMAL=40, VIP=34, entradas inválidas=0 con PENDIENTE, exactamente 2 h acepta; la consola imprime guardar y correo en ese orden. No se asumió que el correo se envía realmente: son salidas simuladas.

## 3. Pruebas de seguridad (ANTES)
Commit de tests `c049f6a`: 14 tests de `ServicioReservasTest` y un `DineroTest` aislado; AAA, normales, VIP, inválidos, límites, null y efectos de consola. La [ejecución previa verde](https://github.com/erickgamarra-collab/sistema-tutorias/actions/runs/35671651112) y la [micro-refactorización protegida](https://github.com/erickgamarra-collab/sistema-tutorias/actions/runs/35671705692) prueban la red de seguridad. La prueba VIP [detectó deliberadamente 34 esperado frente a 32 real](https://github.com/erickgamarra-collab/sistema-tutorias/actions/runs/35671943445) en PR #3 aislado y cerrado.

## 4. Refactorización 1 — Extract Class de tarifa
- **Smell y riesgo:** `procesar` calcula precios entre validaciones y efectos, mezclando razones de cambio; tocar VIP puede afectar la coordinación.
- **Antes:** `double total = calcularTotal(r)`; método privado en `ServicioReservas` que devolvía 40 o `40*0.85`.
- **Técnica / después:** extraer `CalculadoraTarifa` como clase especializada, con constantes `TARIFA_BASE` y `FACTOR_VIP`; `procesar` delega el cálculo y mantiene orden.
- **Pruebas:** NORMAL=40, VIP=34 y tipo null=40; salida y estado. [CI verde](https://github.com/erickgamarra-collab/sistema-tutorias/actions/runs/35671754913).
- **Commit:** `824fb08`. **Costo:** una clase adicional; **beneficio:** reglas de tarifa separadas de validación y efectos.

## 5. Refactorización 2 — Extract Class / reorganizar responsabilidades
- **Smell y riesgo:** el servicio imprimía persistencia y correo directamente; modificar un mensaje podía alterar secuencias y contaminar la lógica principal.
- **Antes:** `System.out.println("Guardando reserva " + r.getId());` y `System.out.println("Correo enviado a " + r.getCorreo());` dentro de `procesar`.
- **Técnica / después:** `RegistroReservaConsola.guardar(r)` y `NotificacionReservaConsola.enviar(r)`, invocados en el mismo orden y antes de `r.confirmar()`. No se incorpora un servicio externo real.
- **Pruebas:** texto exacto, orden de mensajes, ausencia de mensajes ante entradas inválidas, retorno y estado. [CI verde](https://github.com/erickgamarra-collab/sistema-tutorias/actions/runs/35671807357).
- **Commit:** `3c79b15`. **Costo:** dos clases y dependencias internas; **beneficio:** cada efecto tiene responsabilidad localizable.

## 6. Refactorización 3 — Value Object / agrupar Data Clumps
- **Smell y riesgo:** `inicio` y `fin` viajan juntos y comparten la regla `fin>inicio`. Encapsularlos con validación estricta en constructor introduciría una excepción nueva.
- **Antes:** dos campos `LocalDateTime` en `Reserva`; `ServicioReservas` comprueba null y `isAfter`.
- **Técnica / después:** `PeriodoReserva` como `record` con `esValido()`; `Reserva` conserva el constructor de cinco argumentos y getters `getInicio/getFin`. **No se lanzan nuevas excepciones** al construir períodos inválidos; `procesar` sigue devolviendo 0.
- **Pruebas:** fechas null, fin anterior, fin igual, período válido, estado PENDIENTE para invalidez. [CI verde](https://github.com/erickgamarra-collab/sistema-tutorias/actions/runs/35671876628).
- **Commit:** `c806c04`. **Costo:** un objeto adicional y delegación; **beneficio:** el par de fechas y su regla expresan un concepto único.

## 7. Comparación final

| Dimensión | Antes | Después | Evidencia |
|---|---|---|---|
| Responsabilidades | servicio valida, calcula, imprime y confirma | tarifa y efectos en clases propias | commits 824fb08 / 3c79b15 |
| Cohesión | tarifa y consola dentro del proceso | cada clase agrupa una tarea | código final |
| Acoplamiento | lógica del servicio conoce textos y números | delega en clases de responsabilidad específica | diff del PR |
| Datos del dominio | inicio + fin dispersos | `PeriodoReserva` | c806c04 |
| Condicionales | validez de fecha en servicio | `periodo.esValido()` | c806c04 |
| Pruebas | cero tests al recibir el proyecto | 15 JUnit ejecutables | c049f6a y CI |
| Git | línea base original | commits separados tests, micro y tres mejoras | historial de rama |

## 8. Verificación y Git
Orden trazable: `9589a9e` código inicial → `719001a` diagnóstico → `c049f6a` tests → `6536265` micro-refactorización → `824fb08` tarifa → `3c79b15` efectos → `c806c04` período → documentación. Tras **cada uno de los tres cambios avanzados**, `mvn clean test` y `Main` finalizaron correctamente en GitHub Actions. Resultado de la suite: 15 pruebas, 0 fallos, 0 errores, 0 omitidas, BUILD SUCCESS. La mutación VIP se creó en una rama independiente y no forma parte de la rama de entrega.

## 9. Conclusión
La mejora de diseño se demuestra con los límites de responsabilidad más claros y el agrupamiento de datos del dominio, no con el número de líneas. La misma suite protege retorno, estados, límites, datos inválidos y salida; su capacidad de detectar una regresión VIP fue observada. Se preservó el contrato del código heredado sin introducir una regla de negocio nueva. El costo es mayor número de clases, pero las razones de cambio se encuentran menos mezcladas.

## 10. Declaración de IA
Utilicé herramientas de inteligencia artificial para preparar la implementación de la guía, organizar el diagnóstico, proponer pruebas, comprobar la coherencia del refactor y redactar la documentación. La validación técnica se realizó con GitHub Actions y el historial de commits. **Asumo la responsabilidad de comprender y defender las decisiones presentadas.**

## 11. Repositorio
https://github.com/erickgamarra-collab/sistema-tutorias — rama final `main`; Rama utilizada durante el desarrollo `semana6-labs-ae5`; Pull Request: #4 `fusionado correctamente`
