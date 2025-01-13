'use strict';

const express = require('express');
const { getContract } = require('../config/fabric');
const RandomHash = require('random-hash');

const router = express.Router();

router.get('/', async (req, res) => {
    try {
        const contract = getContract();
        const result = await contract.evaluateTransaction('GetAllWills');
        res.status(200).json({ result: JSON.parse(result.toString()) });
    } catch (error) {
        res.status(500).json({ error: error.message });
    }
});

router.get('/:willId', async (req, res) => {
    try {
        const { willId } = req.params;
        const contract = getContract();
        const result = await contract.submitTransaction('ReadWill', willId);
        res.status(200).json({ result: JSON.parse(result.toString()) });
    } catch (error) {
        res.status(500).json({ error: error.message });
    }
});

router.post('/', async (req, res) => {
    try {
        const willId = RandomHash.generateHash();
        const { inheritances, executors, wills, shareAt } = req.body;
        const contract = getContract();
        const result = await contract.submitTransaction(
            'CreateWill',
            willId,
            JSON.stringify(inheritances),
            JSON.stringify(executors),
            JSON.stringify(wills),
            shareAt
        );
        if (`${result}` === '') {
            res.status(200).json({ result: { willId } });
        } else {
            res.status(400).json({ result: JSON.parse(result.toString()) });
        }
    } catch (error) {
        res.status(500).json({ error: error.message });
    }
});

module.exports = router;
