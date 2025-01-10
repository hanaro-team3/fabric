package chaincode

import (
	"encoding/json"
	"fmt"

	"github.com/hyperledger/fabric-contract-api-go/v2/contractapi"
)

type WillTransferContract struct {
	contractapi.Contract
}

type Will struct {
	WillID       string         `json:"willId"`
	Inheritances []Inheritance  `json:"inheritances"`
	Executors    []Executor     `json:"executors"`
	Wills        []WillContent  `json:"wills"`
	ShareAt      int            `json:"shareAt"`
}

type Inheritance struct {
	Name     string `json:"name"`
	Relation string `json:"relation"`
	Asset    string `json:"asset"`
}

type Executor struct {
	Name     string `json:"name"`
	Relation string `json:"relation"`
}

type WillContent struct {
	Relation string `json:"relation"`
	Content  string `json:"content"`
}

func (c *WillTransferContract) InitLedger(ctx contractapi.TransactionContextInterface) error {
	initialWills := []Will{
		{
			WillID: "will1",
			Inheritances: []Inheritance{
				{Name: "Sohee", Relation: "Daughter", Asset: "Building"},
				{Name: "Bob", Relation: "Son", Asset: "Apartment"},
			},
			Executors: []Executor{
				{Name: "Charlie", Relation: "Executor1"},
			},
			Wills: []WillContent{
				{Relation: "Daughter", Content: "This is the first will."},
			},
			ShareAt: 2025,
		},
		{
			WillID: "will2",
			Inheritances: []Inheritance{
				{Name: "David", Relation: "Son", Asset: "Million dollars"},
				{Name: "Eve", Relation: "Son", Asset: "Billion dollars"},
			},
			Executors: []Executor{
				{Name: "Frank", Relation: "Executor2"},
			},
			Wills: []WillContent{
				{Relation: "Son", Content: "This is the second will."},
			},
			ShareAt: 2026,
		},
	}

	for _, will := range initialWills {
		willJSON, err := json.Marshal(will)
		if err != nil {
			return fmt.Errorf("failed to marshal initial will: %v", err)
		}
		err = ctx.GetStub().PutState(will.WillID, willJSON)
		if err != nil {
			return fmt.Errorf("failed to put will in ledger: %v", err)
		}
	}
	return nil
}

func (c *WillTransferContract) CreateWill(ctx contractapi.TransactionContextInterface, willID string, inheritances []Inheritance, executors []Executor, wills []WillContent, shareAt int) error {
	exists, err := c.WillExists(ctx, willID)
	if err != nil {
		return err
	}
	if exists {
		return fmt.Errorf("will %s already exists", willID)
	}

	will := Will{
		WillID:       willID,
		Inheritances: inheritances,
		Executors:    executors,
		Wills:        wills,
		ShareAt:      shareAt,
	}

	willJSON, err := json.Marshal(will)
	if err != nil {
		return fmt.Errorf("failed to marshal will: %v", err)
	}

	return ctx.GetStub().PutState(willID, willJSON)
}

func (c *WillTransferContract) ReadWill(ctx contractapi.TransactionContextInterface, willID string) (*Will, error) {
	willJSON, err := ctx.GetStub().GetState(willID)
	if err != nil {
		return nil, fmt.Errorf("failed to read will from world state: %v", err)
	}
	if willJSON == nil {
		return nil, fmt.Errorf("will %s does not exist", willID)
	}

	var will Will
	err = json.Unmarshal(willJSON, &will)
	if err != nil {
		return nil, fmt.Errorf("failed to unmarshal will: %v", err)
	}

	return &will, nil
}

func (c *WillTransferContract) UpdateWill(ctx contractapi.TransactionContextInterface, willID string, inheritances []Inheritance, executors []Executor, wills []WillContent, shareAt int) error {
	exists, err := c.WillExists(ctx, willID)
	if err != nil {
		return err
	}
	if !exists {
		return fmt.Errorf("will %s does not exist", willID)
	}

	will := Will{
		WillID:       willID,
		Inheritances: inheritances,
		Executors:    executors,
		Wills:        wills,
		ShareAt:      shareAt,
	}

	willJSON, err := json.Marshal(will)
	if err != nil {
		return fmt.Errorf("failed to marshal will: %v", err)
	}

	return ctx.GetStub().PutState(willID, willJSON)
}

func (c *WillTransferContract) DeleteWill(ctx contractapi.TransactionContextInterface, willID string) error {
	exists, err := c.WillExists(ctx, willID)
	if err != nil {
		return err
	}
	if !exists {
		return fmt.Errorf("will %s does not exist", willID)
	}

	return ctx.GetStub().DelState(willID)
}

func (c *WillTransferContract) WillExists(ctx contractapi.TransactionContextInterface, willID string) (bool, error) {
	willJSON, err := ctx.GetStub().GetState(willID)
	if err != nil {
		return false, fmt.Errorf("failed to read will from world state: %v", err)
	}

	return willJSON != nil, nil
}

func (c *WillTransferContract) GetAllWills(ctx contractapi.TransactionContextInterface) ([]Will, error) {
	resultsIterator, err := ctx.GetStub().GetStateByRange("", "")
	if err != nil {
		return nil, fmt.Errorf("failed to get all wills: %v", err)
	}
	defer resultsIterator.Close()

	var wills []Will
	for resultsIterator.HasNext() {
		queryResponse, err := resultsIterator.Next()
		if err != nil {
			return nil, err
		}

		var will Will
		err = json.Unmarshal(queryResponse.Value, &will)
		if err != nil {
			return nil, err
		}
		wills = append(wills, will)
	}

	return wills, nil
}
