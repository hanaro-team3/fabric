package org.hyperledger.fabric.samples.willtransfer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.contract.annotation.Contract;
import org.hyperledger.fabric.contract.annotation.Default;
import org.hyperledger.fabric.contract.annotation.Info;
import org.hyperledger.fabric.contract.annotation.License;
import org.hyperledger.fabric.contract.annotation.Transaction;
import org.hyperledger.fabric.shim.ChaincodeException;
import org.hyperledger.fabric.shim.ledger.KeyValue;
import org.hyperledger.fabric.shim.ledger.QueryResultsIterator;

import com.owlike.genson.Genson;

@Contract(
        name = "willtransfer",
        info = @Info(
                title = "Will Transfer",
                description = "Will transfer management contract",
                version = "0.0.1-SNAPSHOT",
                license = @License(
                        name = "Apache 2.0 License",
                        url = "http://www.apache.org/licenses/LICENSE-2.0.html"))
)
@Default
public final class WillTransfer implements ContractInterface {

    private final Genson genson = new Genson();

    private enum WillTransferErrors {
        WILL_NOT_FOUND,
        WILL_ALREADY_EXISTS
    }

    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public void InitLedger(final Context ctx) {
        List<Will> initialWills = Arrays.asList(
                new Will("will1",
                        Arrays.asList(new Will.Inheritance("Alice", "Daughter", "Building"), new Will.Inheritance("Bob", "Son", "Apartment")),
                        Arrays.asList(new Will.Executor("Charlie", "Executor1")),
                        Arrays.asList(new Will.WillContent("Daughter", "This is the first will.")),
                        2025),
                new Will("will2",
                        Arrays.asList(new Will.Inheritance("David", "Son", "Million dollars"), new Will.Inheritance("Eve", "Son", "Billion dollars")),
                        Arrays.asList(new Will.Executor("Frank", "Executor2")),
                        Arrays.asList(new Will.WillContent("Son", "This is the second will.")),
                        2026)
        );

        for (Will will : initialWills) {
            putWill(ctx, will);
        }
    }

    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public Will CreateWill(final Context ctx, final String willId,
                           final List<Will.Inheritance> inheritances,
                           final List<Will.Executor> executors,
                           final List<Will.WillContent> wills,
                           final int shareAt) {

        System.out.println("willwillwillwillwill");
        if (WillExists(ctx, willId)) {
            String errorMessage = String.format("Will %s already exists", willId);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, WillTransferErrors.WILL_ALREADY_EXISTS.toString());
        }

        Will will = new Will(willId, inheritances, executors, wills, shareAt);
        return putWill(ctx, will);
    }

    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public Will UpdateWill(final Context ctx, final String willId,
                           final List<Will.Inheritance> inheritances,
                           final List<Will.Executor> executors,
                           final List<Will.WillContent> wills,
                           final int shareAt) {

        if (!WillExists(ctx, willId)) {
            String errorMessage = String.format("Will %s does not exist", willId);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, WillTransferErrors.WILL_NOT_FOUND.toString());
        }

        Will will = new Will(willId, inheritances, executors, wills, shareAt);
        return putWill(ctx, will);
    }

    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public Will ReadWill(final Context ctx, final String willId) {
        String willJSON = ctx.getStub().getStringState(willId);

        if (willJSON == null || willJSON.isEmpty()) {
            String errorMessage = String.format("Will %s does not exist", willId);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, WillTransferErrors.WILL_NOT_FOUND.toString());
        }

        return genson.deserialize(willJSON, Will.class);
    }

    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public void DeleteWill(final Context ctx, final String willId) {
        if (!WillExists(ctx, willId)) {
            String errorMessage = String.format("Will %s does not exist", willId);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, WillTransferErrors.WILL_NOT_FOUND.toString());
        }

        ctx.getStub().delState(willId);
    }

    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public boolean WillExists(final Context ctx, final String willId) {
        String willJSON = ctx.getStub().getStringState(willId);
        return (willJSON != null && !willJSON.isEmpty());
    }

    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public String GetAllWills(final Context ctx) {
        List<Will> queryResults = new ArrayList<>();
        QueryResultsIterator<KeyValue> results = ctx.getStub().getStateByRange("", "");

        for (KeyValue result : results) {
            Will will = genson.deserialize(result.getStringValue(), Will.class);
            queryResults.add(will);
        }

        return genson.serialize(queryResults);
    }

    private Will putWill(final Context ctx, final Will will) {
        String sortedJson = genson.serialize(will);
        ctx.getStub().putStringState(will.getWillId(), sortedJson);
        return will;
    }
}
