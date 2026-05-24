const express = require('express');
const axios   = require('axios');
const cors    = require('cors');
const morgan  = require('morgan');
const path    = require('path');
const EventEmitter = require('events');

const app  = express();
const PORT = 3000;
const SPRING_BOOT_URL = 'http://localhost:8084';

// ============================================================
// EVENT BUS (Pub/Sub simulado)
// ============================================================
const eventBus = new EventEmitter();
const eventStore = []; // Log inmutable de eventos

function publicarEvento(tipo, payload) {
    const evento = {
        id: Date.now(),
        tipo,
        payload,
        timestamp: new Date().toISOString()
    };
    eventStore.push(evento);
    console.log(`[EVENT BUS] Publicando: ${tipo}`);
    eventBus.emit(tipo, evento);
    eventBus.emit('TODOS_LOS_EVENTOS', evento);
}

// ============================================================
// SERVICIO DE NOTIFICACIONES (suscriptor)
// ============================================================
eventBus.on('PAGO_COMPLETADO', (evento) => {
    console.log(`[NOTIFICACIONES] ✅ Pago completado - Monto: ${evento.payload.monto} COP - Ref: ${evento.payload.referencia}`);
});

eventBus.on('PAGO_FALLIDO', (evento) => {
    console.log(`[NOTIFICACIONES] ❌ Pago fallido - Motivo: ${evento.payload.motivo}`);
});

eventBus.on('COMPENSACION_EJECUTADA', (evento) => {
    console.log(`[NOTIFICACIONES] ⚠ Compensación ejecutada - Transacción: ${evento.payload.transaccionId}`);
});

eventBus.on('TODOS_LOS_EVENTOS', (evento) => {
    console.log(`[EVENT STORE] Guardando evento #${evento.id}: ${evento.tipo}`);
});

// ============================================================
// MIDDLEWARES
// ============================================================
app.use(cors());
app.use(morgan('dev'));
app.use(express.json());
app.use(express.static(path.join(__dirname, 'public')));

// ============================================================
// POST /gateway/pagar
// ============================================================
app.post('/gateway/pagar', async (req, res) => {
    console.log('=== PAGO RECIBIDO EN NODE GATEWAY ===');
    const { cuentaOrigenId, cuentaDestinoId, monto, regionId, descripcion } = req.body;

    if (!cuentaOrigenId || !cuentaDestinoId || !monto || !regionId) {
        return res.status(400).json({ error: 'Faltan campos obligatorios' });
    }
    if (monto <= 0) return res.status(400).json({ error: 'El monto debe ser mayor a 0' });
    if (cuentaOrigenId === cuentaDestinoId) return res.status(400).json({ error: 'La cuenta origen y destino no pueden ser la misma' });

    // Publicar evento de inicio
    publicarEvento('PAGO_INICIADO', { cuentaOrigenId, cuentaDestinoId, monto, regionId });

    try {
        const response = await axios.post(`${SPRING_BOOT_URL}/api/transacciones/pagar`, {
            cuentaOrigenId, cuentaDestinoId, monto, regionId,
            descripcion: descripcion || 'Pago desde Node Gateway'
        });

        const transaccion = response.data;

        // Publicar evento según resultado
        if (transaccion.estado === 'COMPLETADA') {
            publicarEvento('PAGO_COMPLETADO', {
                transaccionId: transaccion.id,
                referencia: transaccion.referencia,
                monto: transaccion.monto
            });
        } else if (transaccion.estado === 'FALLIDA') {
            publicarEvento('PAGO_FALLIDO', {
                transaccionId: transaccion.id,
                motivo: 'Saldo insuficiente u otro error'
            });
        } else if (transaccion.estado === 'COMPENSADA') {
            publicarEvento('COMPENSACION_EJECUTADA', {
                transaccionId: transaccion.id
            });
        }

        return res.status(201).json({
            mensaje: 'Pago procesado exitosamente',
            origen: 'node-gateway',
            transaccion: response.data
        });

    } catch (error) {
        publicarEvento('PAGO_FALLIDO', { motivo: error.message });
        const status  = error.response?.status  || 500;
        const mensaje = error.response?.data?.error || 'Error interno del gateway';
        return res.status(status).json({ error: mensaje, origen: 'node-gateway' });
    }
});

// ============================================================
// GET /gateway/transacciones
// ============================================================
app.get('/gateway/transacciones', async (req, res) => {
    try {
        const response = await axios.get(`${SPRING_BOOT_URL}/api/transacciones`);
        return res.status(200).json(response.data);
    } catch (error) {
        return res.status(500).json({ error: 'Error al obtener transacciones' });
    }
});

// ============================================================
// GET /gateway/transacciones/:id/pasos
app.get('/gateway/transacciones/:id/pasos', async (req, res) => {
    try {
        const response = await axios.get(`${SPRING_BOOT_URL}/api/transacciones/${req.params.id}/pasos`);
        return res.status(200).json(response.data);
    } catch (error) {
        return res.status(500).json({ error: 'Error al obtener pasos' });
    }
});

// GET /gateway/transacciones/:id/flujo
// ============================================================
app.get('/gateway/transacciones/:id/flujo', async (req, res) => {
    try {
        const response = await axios.get(`${SPRING_BOOT_URL}/api/transacciones/${req.params.id}/flujo`);
        return res.status(200).json(response.data);
    } catch (error) {
        return res.status(500).json({ error: 'Error al obtener flujo' });
    }
});

// ============================================================
// GET /gateway/cuentas/:id/saldo
// ============================================================
app.get('/gateway/cuentas/:id/saldo', async (req, res) => {
    try {
        const response = await axios.get(`${SPRING_BOOT_URL}/api/cuentas/${req.params.id}/saldo`);
        return res.status(200).json(response.data);
    } catch (error) {
        return res.status(500).json({ error: 'Error al consultar saldo' });
    }
});

// ============================================================
// GET /gateway/cuentas/:id — cuenta completa con titular
// ============================================================
app.get('/gateway/cuentas/:id', async (req, res) => {
    try {
        const [cuentaRes, saldoRes] = await Promise.all([
            axios.get(`${SPRING_BOOT_URL}/api/cuentas/${req.params.id}`),
            axios.get(`${SPRING_BOOT_URL}/api/cuentas/${req.params.id}/saldo`)
        ]);
        const cuenta = cuentaRes.data;
        const saldo = saldoRes.data.saldo !== undefined ? saldoRes.data.saldo : saldoRes.data;
        return res.status(200).json({
            id: cuenta.id,
            saldo: saldo,
            moneda: cuenta.moneda || 'COP',
            activa: cuenta.activa,
            titular: cuenta.cliente ? cuenta.cliente.nombre : 'Sin nombre',
            email: cuenta.cliente ? cuenta.cliente.email : '',
            region: cuenta.cliente && cuenta.cliente.region ? cuenta.cliente.region.nombre : ''
        });
    } catch (error) {
        return res.status(500).json({ error: 'Error al consultar cuenta' });
    }
});

// ============================================================
// GET /gateway/cuentas — todas las cuentas
// ============================================================
app.get('/gateway/cuentas', async (req, res) => {
    try {
        const response = await axios.get(`${SPRING_BOOT_URL}/api/cuentas`);
        return res.status(200).json(response.data);
    } catch (error) {
        return res.status(500).json({ error: 'Error al obtener cuentas' });
    }
});

// ============================================================
// GET /gateway/eventos — Event Store (log inmutable)
// ============================================================
app.get('/gateway/eventos', (req, res) => {
    res.json({
        total: eventStore.length,
        eventos: eventStore.slice(-50).reverse()
    });
});

// ============================================================
// GET /gateway/health
// ============================================================
app.get('/gateway/health', (req, res) => {
    res.json({
        status: 'UP',
        servicio: 'node-gateway',
        springBoot: SPRING_BOOT_URL,
        eventBus: 'ACTIVO',
        eventosRegistrados: eventStore.length,
        timestamp: new Date().toISOString()
    });
});

// ============================================================
// ARRANCAR SERVIDOR
// ============================================================
app.listen(PORT, () => {
    console.log('===========================================');
    console.log(`Node Gateway corriendo en puerto ${PORT}`);
    console.log(`Spring Boot URL: ${SPRING_BOOT_URL}`);
    console.log(`Event Bus: ACTIVO (Pub/Sub simulado)`);
    console.log('===========================================');
    console.log(`  WEB      http://localhost:${PORT}`);
    console.log(`  POST     http://localhost:${PORT}/gateway/pagar`);
    console.log(`  GET      http://localhost:${PORT}/gateway/transacciones`);
    console.log(`  GET      http://localhost:${PORT}/gateway/cuentas/:id/saldo`);
    console.log(`  GET      http://localhost:${PORT}/gateway/eventos`);
    console.log(`  GET      http://localhost:${PORT}/gateway/health`);
    console.log('===========================================');
});