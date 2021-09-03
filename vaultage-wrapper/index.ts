import express from 'express';
import { json } from 'body-parser';
import {v4 as uuidGenerator} from 'uuid';

console.log('starting vaultage-wrapper.');

const expressServer = express();
const serverShutdownToken = uuidGenerator();

expressServer.use(json());

let port = 3001;

function startServer() {
    expressServer.listen(port, '127.0.0.1', () => {
        console.log(`%MSG:${port}/${serverShutdownToken}`);
        require('vaultage');
    }).on('error', (e) => {
        if((e as any).code == 'EADDRINUSE') {
            port++;
            startServer();
        }
    });
}

expressServer.post('/', (req, res) => {
    const body = req.body as any;
    if(body.token == serverShutdownToken) {
        res.send('OK');
        process.exit(0);
    } else {
        res.send('wrong-token');
    }
});

startServer();