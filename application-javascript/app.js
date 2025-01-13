'use strict';

const express = require('express');
const { initFabric } = require('./config/fabric');
const willRoute = require('./routes/willRoute');

const app = express();
app.use(express.json());

app.use('/wills', willRoute);

initFabric()
    .then(() => {
        app.listen(3000, () => {
            console.log('Express server is running on port 3000');
        });
    })
    .catch((error) => {
        console.error(`Failed to initialize Fabric: ${error}`);
    });
