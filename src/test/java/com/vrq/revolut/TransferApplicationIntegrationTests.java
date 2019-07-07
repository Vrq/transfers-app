package com.vrq.revolut;

import com.vrq.revolut.core.Account;
import com.vrq.revolut.core.Transfer;
import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit5.DropwizardAppExtension;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.lang.String.format;
import static java.math.BigDecimal.*;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(DropwizardExtensionsSupport.class)
@TestMethodOrder(OrderAnnotation.class)
public class TransferApplicationIntegrationTests {

    private static final DropwizardAppExtension<TransferAppConfiguration> RULE =
            new DropwizardAppExtension<>(TransferApplication.class, ResourceHelpers.resourceFilePath("test-config.yml"));
    private static final String ACCOUNTS_URI = "http://localhost:%d/accounts/";
    private static final String TRANSFERS_URI = "http://localhost:%d/transfers/";
    private static Client client = new JerseyClientBuilder().build();
    private static GenericType<List<Account>> accountListType = new GenericType<List<Account>>() {
    };
    private static GenericType<List<Transfer>> transfersListType = new GenericType<List<Transfer>>() {
    };

    @Test
    @Order(1)
    void getAccountsWithFreshDbReturnsEmptyList() {
        Response response = client.target(format(ACCOUNTS_URI, RULE.getLocalPort()))
                .request()
                .get();
        List<Account> accounts = response.readEntity(accountListType);

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(accounts.size()).isEqualTo(0);
    }

    @Test
    @Order(2)
    void postAccountRunParallelMultipleTimesCorrectlyCreatesAccounts() throws InterruptedException {
        final int numberOfAccounts = 200;
        final int numberOfThreads = 10;

        CountDownLatch latch = new CountDownLatch(numberOfAccounts);
        ExecutorService service = Executors.newFixedThreadPool(numberOfThreads);

        for (int i = 0; i < numberOfAccounts; i++) {
            service.submit(() -> {
                Response response = client.target(
                        format(ACCOUNTS_URI, RULE.getLocalPort()))
                        .request()
                        .post(Entity.json(new Account()));

                latch.countDown();
                assertThat(response.getStatus()).isEqualTo(200);
            });
        }
        latch.await();
        Response getAllResponse = client.target(format(ACCOUNTS_URI, RULE.getLocalPort()))
                .request()
                .get();
        List<Account> accounts = getAllResponse.readEntity(accountListType);

        assertThat(accounts.size()).isEqualTo(numberOfAccounts);
    }

    @Test
    @Order(3)
    void getTransfersWithoutAnyTransfersMadeReturnsEmptyList() {
        Response response = client.target(format(TRANSFERS_URI, RULE.getLocalPort()))
                .request()
                .get();
        List<Transfer> transfers = response.readEntity(transfersListType);

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(transfers.size()).isEqualTo(0);
    }

    @Test
    @Order(4)
    void postTransferInvokedParallelMultipleTimesResultsInCorrectBalances() throws InterruptedException {
        final int numberOfTransfers = 100;
        final BigDecimal singleTransferAmount = valueOf(250);
        final int numberOfThreads = 10;
        final int depositAmount = 100000;
        final long accountId1 = 1;
        final long accountId2 = 2;
        CountDownLatch latch = new CountDownLatch(numberOfTransfers);
        ExecutorService service = Executors.newFixedThreadPool(numberOfThreads);

        client.target(format(ACCOUNTS_URI + accountId1 + "/deposit/"+ depositAmount, RULE.getLocalPort()))
                .request()
                .post(Entity.json(null));
        for (int i = 0; i < numberOfTransfers; i++) {
            service.submit(() -> {
                Response response = client.target(format(TRANSFERS_URI, RULE.getLocalPort()))
                        .request()
                        .post(Entity.json(new Transfer(singleTransferAmount, new Account(accountId1), new Account(accountId2))));
                latch.countDown();
                assertThat(response.getStatus()).isEqualTo(200);
            });
        }
        latch.await();

        Response getAllTransfers = client.target(format(TRANSFERS_URI, RULE.getLocalPort()))
                .request()
                .get();
        Response getAccount1 = client.target(format(ACCOUNTS_URI + accountId1, RULE.getLocalPort()))
                .request()
                .get();
        Response getAccount2 = client.target(format(ACCOUNTS_URI + accountId2, RULE.getLocalPort()))
                .request()
                .get();
        List<Transfer> transfers = getAllTransfers.readEntity(transfersListType);
        BigDecimal balance1 = getAccount1.readEntity(Account.class).getBalance();
        BigDecimal balance2 = getAccount2.readEntity(Account.class).getBalance();

        assertThat(transfers.size()).isEqualTo(numberOfTransfers);
        assertThat(balance1).isEqualTo(valueOf(75000).setScale(2));
        assertThat(balance2).isEqualTo(valueOf(25000).setScale(2));
    }

    @Test
    @Order(5)
    void postDepositParallelMultipleTimesResultsInCorrectlyIncreasedAccountBalance() throws InterruptedException {
        final int numberOfDeposits = 200;
        final long depositAmount = 7000000;
        final int numberOfThreads = 10;
        final long accountId = 3;

        CountDownLatch latch = new CountDownLatch(numberOfDeposits);
        ExecutorService service = Executors.newFixedThreadPool(numberOfThreads);

        for (int i = 0; i < numberOfDeposits; i++) {
            service.submit(() -> {
                Response response = client.target(format(ACCOUNTS_URI + accountId + "/deposit/" + depositAmount, RULE.getLocalPort()))
                        .request()
                        .post(Entity.json(null));
                latch.countDown();
                assertThat(response.getStatus()).isEqualTo(200);
            });
        }
        latch.await();
        Response getAllResponse = client.target(format(ACCOUNTS_URI + accountId, RULE.getLocalPort()))
                .request()
                .get();
        Account depositedAccount = getAllResponse.readEntity(Account.class);

        assertThat(depositedAccount.getBalance()).isEqualTo(BigDecimal.valueOf(numberOfDeposits * depositAmount).setScale(2));
    }

}