const WebSocket = require('ws');

const wss = new WebSocket.Server({ port: process.env.PORT || 8080 });
const clients = new Map(); // udcin -> WebSocket

wss.on('connection', (ws) => {
    let currentUdcin = null;

    ws.on('message', (message) => {
        try {
            const data = JSON.parse(message);
            
            if (data.type === 'register') {
                currentUdcin = data.udcin;
                clients.set(currentUdcin, ws);
                console.log(`Registered UDCIN: ${currentUdcin}`);
                ws.send(JSON.stringify({ type: 'registered', udcin: currentUdcin }));
            } else if (data.targetUdcin) {
                const targetWs = clients.get(data.targetUdcin);
                if (targetWs && targetWs.readyState === WebSocket.OPEN) {
                    targetWs.send(JSON.stringify({
                        ...data,
                        senderUdcin: currentUdcin
                    }));
                } else {
                    ws.send(JSON.stringify({ type: 'error', message: 'Target not found or offline' }));
                }
            }
        } catch (e) {
            console.error('Invalid message format', e);
        }
    });

    ws.on('close', () => {
        if (currentUdcin) {
            clients.delete(currentUdcin);
            console.log(`Unregistered UDCIN: ${currentUdcin}`);
        }
    });
});

console.log('WebSocket signaling server running on port 8080');
