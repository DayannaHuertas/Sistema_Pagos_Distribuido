<img width="1362" height="767" alt="image" src="https://github.com/user-attachments/assets/36bdb5e0-c5fc-449f-9e61-c4c97443df44" />

# Plataforma E-Commerce Distribuida

## Integrantes
- Leyber Ruiz  
- Dayanna Huertas  
- Yeison Romero  
- Laura Porras  

---

## Descripcion del proyecto 

Este proyecto consiste en el diseño e implementación de una plataforma **e-commerce distribuida**, enfocada en el procesamiento seguro y consistente de transacciones de pago en entornos con múltiples servicios y regiones.  

La solución propuesta por el grupo implementa una **arquitectura basada en eventos** y el **patrón Saga**, permitiendo coordinar transacciones distribuidas, manejar fallos parciales y ejecutar mecanismos de compensación para garantizar la integridad de los datos y la recuperación automática del sistema.

El sistema integra:
- **Node.js** para el servicio principal de pagos.
- **Java** para la lógica de compensación mediante Saga.
- **PostgreSQL** como sistema de persistencia transaccional.
- Simulación y validación de fallos para asegurar resiliencia y consistencia eventual.

---

## Problema que Soluciona

En plataformas e-commerce distribuidas, una falla durante el procesamiento de pagos puede generar:
- Transacciones incompletas.
- Duplicidad de pagos.
- Inconsistencias entre servicios.
- Pérdida de información crítica.

Este proyecto busca garantizar que todas las operaciones relacionadas con una transacción se completen correctamente o, en caso de error, se reviertan automáticamente mediante procesos de compensación.

---

## Características Principales

- Arquitectura distribuida basada en eventos.
- Comunicación asíncrona entre servicios.
- Implementación del patrón Saga.
- Persistencia transaccional segura.
- Recuperación automática ante fallos.
- Registro y monitoreo de eventos.
- Integración continua con GitHub Actions.
- Escalabilidad y tolerancia a fallos.

---

## Tecnologías Utilizadas

- Node.js
- Java
- PostgreSQL
- RabbitMQ
- GitHub Actions
- Docker
- Cursor IDE

---

## Objetivo General

Diseñar e implementar un sistema distribuido de procesamiento de pagos que garantice la consistencia transaccional y la recuperación ante fallos mediante una arquitectura orientada a eventos y el patrón Saga.

---

## Resultados Esperados

- Mayor resiliencia del sistema.
- Consistencia eventual de transacciones.
- Recuperación automática ante errores.
- Reducción de inconsistencias en pagos.
- Escalabilidad y mantenibilidad de la plataforma.

---

## Repositorio

Proyecto académico desarrollado para la asignatura de **Sistemas Distribuidos** en la Fundación Universitaria Juan de Castellanos.
