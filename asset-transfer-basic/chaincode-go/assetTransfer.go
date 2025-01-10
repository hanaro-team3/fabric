/*
SPDX-License-Identifier: Apache-2.0
*/

package main

import (
	"log"

	"github.com/hyperledger/fabric-contract-api-go/v2/contractapi"
	"github.com/hyperledger/fabric-samples/asset-transfer-basic/chaincode-go/chaincode"
)

func main() {
	chaincode, err := contractapi.NewChaincode(&chaincode.WillTransferContract{})
	if err != nil {
		log.Panicf("Error creating willtransfer chaincode: %v", err)
		return
	}

	if err := chaincode.Start(); err != nil {
		log.Panicf("Error starting willtransfer chaincode: %v", err)
	}
}
