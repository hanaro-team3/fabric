'use strict';

const { Gateway, Wallets } = require('fabric-network');
const FabricCAServices = require('fabric-ca-client');
const path = require('path');
const {
    buildCAClient,
    registerAndEnrollUser,
    enrollAdmin,
} = require('../../test-application/javascript/CAUtil.js');
const {
    buildCCPOrg1,
    buildWallet,
} = require('../../test-application/javascript/AppUtil.js');

const channelName = 'mychannel';
const chaincodeName = 'basic';
const mspOrg1 = 'Org1MSP';
const walletPath = path.join(__dirname, 'wallet');
const org1UserId = 'appUser';
const org2UserId = 'appuser2';

let contract;

async function initFabric() {
    const ccp = buildCCPOrg1();
    const caClient = buildCAClient(
        FabricCAServices,
        ccp,
        'ca.org1.example.com'
    );
    const wallet = await buildWallet(Wallets, walletPath);

    await enrollAdmin(caClient, wallet, mspOrg1);
    await registerAndEnrollUser(
        caClient,
        wallet,
        mspOrg1,
        org1UserId,
        'org1.department1'
    );
    await registerAndEnrollUser(
        caClient,
        wallet,
        mspOrg1,
        org2UserId,
        'org1.department2'
    );

    const gateway = new Gateway();
    await gateway.connect(ccp, {
        wallet,
        identity: org1UserId,
        discovery: { enabled: true, asLocalhost: true },
    });

    const network = await gateway.getNetwork(channelName);
    contract = network.getContract(chaincodeName);
    await contract.submitTransaction('InitLedger');
}

function getContract() {
    return contract;
}

module.exports = { initFabric, getContract };
