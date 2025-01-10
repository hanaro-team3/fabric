// /*
//  * SPDX-License-Identifier: Apache-2.0
//  */

// package org.hyperledger.fabric.samples.willtransfer;

// import java.util.ArrayList;
// import java.util.List;

// import org.hyperledger.fabric.contract.Context;
// import org.hyperledger.fabric.contract.ContractInterface;
// import org.hyperledger.fabric.contract.annotation.Contract;
// import org.hyperledger.fabric.contract.annotation.Default;
// import org.hyperledger.fabric.contract.annotation.Info;
// import org.hyperledger.fabric.contract.annotation.License;
// import org.hyperledger.fabric.contract.annotation.Transaction;
// import org.hyperledger.fabric.shim.ChaincodeException;
// import org.hyperledger.fabric.shim.ChaincodeStub;
// import org.hyperledger.fabric.shim.ledger.KeyValue;
// import org.hyperledger.fabric.shim.ledger.QueryResultsIterator;

// import com.owlike.genson.Genson;

// @Contract(
//         name = "willtransfer",
//         info = @Info(
//                 title = "Will Transfer",
//                 description = "Will transfer management contract",
//                 version = "0.0.1-SNAPSHOT",
//                 license = @License(
//                         name = "Apache 2.0 License",
//                         url = "http://www.apache.org/licenses/LICENSE-2.0.html"))
// )
// @Default
// public final class WillTransfer implements ContractInterface {

//     private final Genson genson = new Genson();

//     private enum WillTransferErrors {
//         WILL_NOT_FOUND,
//         WILL_ALREADY_EXISTS
//     }

//     /**
//      * Initializes some sample wills on the ledger.
//      *
//      * @param ctx the transaction context
//      */
//     @Transaction(intent = Transaction.TYPE.SUBMIT)
//     public void InitLedger(final Context ctx) {
//         putWill(ctx, new Will("will1", "Alice", List.of("Bob", "Charlie"), "Alice's last will", "2025-01-01"));
//         putWill(ctx, new Will("will2", "Bob", List.of("Alice", "David"), "Bob's last will", "2025-01-02"));
//     }

//     /**
//      * Creates a new will on the ledger.
//      *
//      * @param ctx the transaction context
//      * @param willId the ID of the new will
//      * @param owner the owner of the will
//      * @param beneficiaries the beneficiaries of the will
//      * @param content the content of the will
//      * @param timestamp the timestamp of the will creation
//      * @return the created will
//      */
//     @Transaction(intent = Transaction.TYPE.SUBMIT)
//     public Will CreateWill(final Context ctx, final String willId, final String owner, final List<String> beneficiaries,
//                            final String content, final String timestamp) {

//         if (WillExists(ctx, willId)) {
//             String errorMessage = String.format("Will %s already exists", willId);
//             System.out.println(errorMessage);
//             throw new ChaincodeException(errorMessage, WillTransferErrors.WILL_ALREADY_EXISTS.toString());
//         }

//         return putWill(ctx, new Will(willId, owner, beneficiaries, content, timestamp));
//     }

//     private Will putWill(final Context ctx, final Will will) {
//         // Use Genson to serialize the Will object to a JSON string
//         String sortedJson = genson.serialize(will);
//         ctx.getStub().putStringState(will.getWillId(), sortedJson);

//         return will;
//     }

//     /**
//      * Retrieves a will with the specified ID from the ledger.
//      *
//      * @param ctx the transaction context
//      * @param willId the ID of the will
//      * @return the will found on the ledger if there was one
//      */
//     @Transaction(intent = Transaction.TYPE.EVALUATE)
//     public Will ReadWill(final Context ctx, final String willId) {
//         String willJSON = ctx.getStub().getStringState(willId);

//         if (willJSON == null || willJSON.isEmpty()) {
//             String errorMessage = String.format("Will %s does not exist", willId);
//             System.out.println(errorMessage);
//             throw new ChaincodeException(errorMessage, WillTransferErrors.WILL_NOT_FOUND.toString());
//         }

//         return genson.deserialize(willJSON, Will.class);
//     }

//     /**
//      * Updates the properties of a will on the ledger.
//      *
//      * @param ctx the transaction context
//      * @param willId the ID of the will being updated
//      * @param owner the owner of the will being updated
//      * @param beneficiaries the beneficiaries of the will being updated
//      * @param content the content of the will being updated
//      * @param timestamp the timestamp of the will being updated
//      * @return the updated will
//      */
//     @Transaction(intent = Transaction.TYPE.SUBMIT)
//     public Will UpdateWill(final Context ctx, final String willId, final String owner, final List<String> beneficiaries,
//                            final String content, final String timestamp) {

//         if (!WillExists(ctx, willId)) {
//             String errorMessage = String.format("Will %s does not exist", willId);
//             System.out.println(errorMessage);
//             throw new ChaincodeException(errorMessage, WillTransferErrors.WILL_NOT_FOUND.toString());
//         }

//         return putWill(ctx, new Will(willId, owner, beneficiaries, content, timestamp));
//     }

//     /**
//      * Deletes a will from the ledger.
//      *
//      * @param ctx the transaction context
//      * @param willId the ID of the will being deleted
//      */
//     @Transaction(intent = Transaction.TYPE.SUBMIT)
//     public void DeleteWill(final Context ctx, final String willId) {
//         if (!WillExists(ctx, willId)) {
//             String errorMessage = String.format("Will %s does not exist", willId);
//             System.out.println(errorMessage);
//             throw new ChaincodeException(errorMessage, WillTransferErrors.WILL_NOT_FOUND.toString());
//         }

//         ctx.getStub().delState(willId);
//     }

//     /**
//      * Checks the existence of a will on the ledger.
//      *
//      * @param ctx the transaction context
//      * @param willId the ID of the will
//      * @return boolean indicating the existence of the will
//      */
//     @Transaction(intent = Transaction.TYPE.EVALUATE)
//     public boolean WillExists(final Context ctx, final String willId) {
//         String willJSON = ctx.getStub().getStringState(willId);

//         return (willJSON != null && !willJSON.isEmpty());
//     }

//     /**
//      * Retrieves all wills from the ledger.
//      *
//      * @param ctx the transaction context
//      * @return array of wills found on the ledger
//      */
//     @Transaction(intent = Transaction.TYPE.EVALUATE)
//     public String GetAllWills(final Context ctx) {
//         ChaincodeStub stub = ctx.getStub();

//         List<Will> queryResults = new ArrayList<>();

//         // Retrieve all wills from the ledger using getStateByRange with empty startKey & endKey.
//         QueryResultsIterator<KeyValue> results = stub.getStateByRange("", "");

//         for (KeyValue result : results) {
//             Will will = genson.deserialize(result.getStringValue(), Will.class);
//             System.out.println(will);
//             queryResults.add(will);
//         }

//         return genson.serialize(queryResults);
//     }
// }
