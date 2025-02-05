/*
 * Copyright IBM Corp. All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import io.grpc.Grpc;
import io.grpc.ManagedChannel;
import io.grpc.TlsChannelCredentials;
import org.hyperledger.fabric.client.CommitException;
import org.hyperledger.fabric.client.CommitStatusException;
import org.hyperledger.fabric.client.Contract;
import org.hyperledger.fabric.client.EndorseException;
import org.hyperledger.fabric.client.Gateway;
import org.hyperledger.fabric.client.GatewayException;
import org.hyperledger.fabric.client.Hash;
import org.hyperledger.fabric.client.SubmitException;
import org.hyperledger.fabric.client.identity.Identities;
import org.hyperledger.fabric.client.identity.Identity;
import org.hyperledger.fabric.client.identity.Signer;
import org.hyperledger.fabric.client.identity.Signers;
import org.hyperledger.fabric.client.identity.X509Identity;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.cert.CertificateException;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

public final class AppWill {
    private static final String MSP_ID = System.getenv().getOrDefault("MSP_ID", "Org1MSP");
    private static final String CHANNEL_NAME = System.getenv().getOrDefault("CHANNEL_NAME", "mychannel");
    private static final String CHAINCODE_NAME = System.getenv().getOrDefault("CHAINCODE_NAME", "willchaincode");

    private static final Path CRYPTO_PATH = Paths.get("../../test-network/organizations/peerOrganizations/org1.example.com");
    private static final Path CERT_DIR_PATH = CRYPTO_PATH.resolve(Paths.get("users/User1@org1.example.com/msp/signcerts"));
    private static final Path KEY_DIR_PATH = CRYPTO_PATH.resolve(Paths.get("users/User1@org1.example.com/msp/keystore"));
    private static final Path TLS_CERT_PATH = CRYPTO_PATH.resolve(Paths.get("peers/peer0.org1.example.com/tls/ca.crt"));

    private static final String PEER_ENDPOINT = "localhost:7051";
    private static final String OVERRIDE_AUTH = "peer0.org1.example.com";

    private final Contract contract;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static void main(final String[] args) throws Exception {
        var channel = newGrpcConnection();

        var builder = Gateway.newInstance()
                .identity(newIdentity())
                .signer(newSigner())
                .hash(Hash.SHA256)
                .connection(channel)
                .evaluateOptions(options -> options.withDeadlineAfter(5, TimeUnit.SECONDS))
                .endorseOptions(options -> options.withDeadlineAfter(15, TimeUnit.SECONDS))
                .submitOptions(options -> options.withDeadlineAfter(5, TimeUnit.SECONDS))
                .commitStatusOptions(options -> options.withDeadlineAfter(1, TimeUnit.MINUTES));

        try (var gateway = builder.connect()) {
            new AppWill(gateway).run();
        } finally {
            channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
        }
    }

    private static ManagedChannel newGrpcConnection() throws IOException {
        var credentials = TlsChannelCredentials.newBuilder()
                .trustManager(TLS_CERT_PATH.toFile())
                .build();
        return Grpc.newChannelBuilder(PEER_ENDPOINT, credentials)
                .overrideAuthority(OVERRIDE_AUTH)
                .build();
    }

    private static Identity newIdentity() throws IOException, CertificateException {
        try (var certReader = Files.newBufferedReader(getFirstFilePath(CERT_DIR_PATH))) {
            var certificate = Identities.readX509Certificate(certReader);
            return new X509Identity(MSP_ID, certificate);
        }
    }

    private static Signer newSigner() throws IOException, InvalidKeyException {
        try (var keyReader = Files.newBufferedReader(getFirstFilePath(KEY_DIR_PATH))) {
            var privateKey = Identities.readPrivateKey(keyReader);
            return Signers.newPrivateKeySigner(privateKey);
        }
    }

    private static Path getFirstFilePath(Path dirPath) throws IOException {
        try (var keyFiles = Files.list(dirPath)) {
            return keyFiles.findFirst().orElseThrow();
        }
    }

    public AppWill(final Gateway gateway) {
        var network = gateway.getNetwork(CHANNEL_NAME);
        contract = network.getContract(CHAINCODE_NAME);
    }

    public void run() throws GatewayException, CommitException {
        createWill();
        readWillById();
        transferWillOwnership();
		readWillById();
        deleteWill();
    }

    private void createWill() throws EndorseException, SubmitException, CommitStatusException, CommitException {
        String willId = "will" + Instant.now().toEpochMilli();
        System.out.println("\n--> Submit Transaction: CreateWill");

        contract.submitTransaction("CreateWill", willId, "John Doe", "Beneficiary: Jane Doe", "Executor: Mark Smith");

        System.out.println("*** Will created successfully");
    }

    private void readWillById() throws GatewayException {
        String willId = "will12345"; // Example ID
        System.out.println("\n--> Evaluate Transaction: ReadWill");

        var result = contract.evaluateTransaction("ReadWill", willId);

        System.out.println("*** Will details: " + prettyJson(result));
    }

//    private void updateWill() throws EndorseException, SubmitException, CommitException {
//        String willId = "will12345"; // Example ID
//        System.out.println("\n--> Submit Transaction: UpdateWill");
//
//        contract.submitTransaction("UpdateWill", willId, "John Doe", "Beneficiary: Emily Doe", "Executor: Mark Smith");
//
//        System.out.println("*** Will updated successfully");
//    }

    private void transferWillOwnership() throws EndorseException, SubmitException, CommitStatusException {
        String willId = "will12345"; // Example ID
        System.out.println("\n--> Submit Transaction: TransferWill");

        var commit = contract.newProposal("TransferWill")
                .addArguments(willId, "NewOwner")
                .build()
                .endorse()
                .submitAsync();

        var status = commit.getStatus();
        if (!status.isSuccessful()) {
            throw new RuntimeException("Transaction " + status.getTransactionId() +
                    " failed to commit with status code " + status.getCode());
        }

        System.out.println("*** Will ownership transferred successfully");
    }

    private void deleteWill() {
        String willId = "will12345"; // Example ID
        System.out.println("\n--> Submit Transaction: DeleteWill");

        try {
            contract.submitTransaction("DeleteWill", willId);
            System.out.println("*** Will deleted successfully");
        } catch (Exception e) {
            System.out.println("*** Failed to delete will: " + e.getMessage());
        }
    }

    private String prettyJson(final byte[] json) {
        return prettyJson(new String(json, StandardCharsets.UTF_8));
    }

    private String prettyJson(final String json) {
        var parsedJson = JsonParser.parseString(json);
        return gson.toJson(parsedJson);
    }
}

