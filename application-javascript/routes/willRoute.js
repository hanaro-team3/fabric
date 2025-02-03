'use strict';

const express = require('express');
const { getContract } = require('../config/fabric');
const RandomHash = require('random-hash');

const router = express.Router();

function formatInheritance(inheritances) {
    return inheritances.map(inheritance => ({
        type: inheritance.type,
        subType: inheritance.subType,
        financialInstitution: inheritance.financialInstitution || '',
        asset: inheritance.asset,
        amount: inheritance.amount || '',
        ancestors: inheritance.ancestors.map(ancestor => ({
		name: ancestor.name,
		relation: ancestor.relation,
		ratio: ancestor.ratio,
		phoneNumber: ancestor.phoneNumber || ''
        })),
    }));
}

function formatExecutor(executors) {
    return executors.map(executor => ({
        name: executor.name,
        relation: executor.relation,
        priority: executor.priority,
    }));
}

function formatFinalMessages(finalMessages) {
    return finalMessages.map(finalMessage => ({
        name: finalMessage.name,
        relation: finalMessage.relation,
        message: finalMessage.message,
    }));
}

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
        const { inheritances, executors, finalMessages, shareAt } = req.body;
        const contract = getContract();

        const formattedInheritances = formatInheritance(inheritances);
        const formattedExecutors = formatExecutor(executors);
        const formattedFinalMessages = formatFinalMessages(finalMessages);

        const result = await contract.submitTransaction(
            'CreateWill',
            willId,
            JSON.stringify(formattedInheritances),
            JSON.stringify(formattedExecutors),
            JSON.stringify(formattedFinalMessages),
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
