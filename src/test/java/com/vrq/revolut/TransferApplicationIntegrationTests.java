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
    private static final String ACCOUNTS_URI = "http://localhost:%d/accounts";
    private static final String TRANSFERS_URI = "http://localhost:%d/transfers";
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
        final int NUMBER_OF_ACCOUNTS = 200;
        final int NUMBER_OF_THREADS = 10;

        CountDownLatch latch = new CountDownLatch(NUMBER_OF_ACCOUNTS);
        ExecutorService service = Executors.newFixedThreadPool(NUMBER_OF_THREADS);

        for (int i = 0; i < NUMBER_OF_ACCOUNTS; i++) {
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

        assertThat(accounts.size()).isEqualTo(NUMBER_OF_ACCOUNTS);
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
        final int NUMBER_OF_TRANSFERS = 100;
        final BigDecimal SINGLE_TRANSFER_AMOUNT = valueOf(250);
        final int NUMBER_OF_THREADS = 10;

        CountDownLatch latch = new CountDownLatch(NUMBER_OF_TRANSFERS);
        ExecutorService service = Executors.newFixedThreadPool(NUMBER_OF_THREADS);

        client.target(format(ACCOUNTS_URI + "/1/deposit/100000", RULE.getLocalPort()))
                .request()
                .post(Entity.json(null));
        for (int i = 0; i < NUMBER_OF_TRANSFERS; i++) {
            int finalI = i;
            System.out.println("This is outside : " + finalI);
            service.submit(() -> {
                Response response = client.target(format(TRANSFERS_URI, RULE.getLocalPort()))
                        .request()
                        .post(Entity.json(new Transfer(SINGLE_TRANSFER_AMOUNT, new Account(1), new Account(2))));
                System.out.println("This is outside : " + finalI);
                latch.countDown();
                assertThat(response.getStatus()).isEqualTo(200);
            });
        }
        latch.await();

        Response getAllTransfers = client.target(format(TRANSFERS_URI, RULE.getLocalPort()))
                .request()
                .get();
        Response getAccount1 = client.target(format(ACCOUNTS_URI + "/1", RULE.getLocalPort()))
                .request()
                .get();
        Response getAccount2 = client.target(format(ACCOUNTS_URI + "/2", RULE.getLocalPort()))
                .request()
                .get();
        List<Transfer> transfers = getAllTransfers.readEntity(transfersListType);
        BigDecimal balance1 = getAccount1.readEntity(Account.class).getBalance();
        BigDecimal balance2 = getAccount2.readEntity(Account.class).getBalance();

        assertThat(transfers.size()).isEqualTo(NUMBER_OF_TRANSFERS);
        assertThat(balance1).isEqualTo(valueOf(75000).setScale(2));
        assertThat(balance2).isEqualTo(valueOf(25000).setScale(2));
    }

    @Test
    @Order(5)
    void postDepositParallelMultipleTimesResultsInCorrectlyIncreasedAccountBalance() throws InterruptedException {
        final int NUMBER_OF_DEPOSITS = 200;
        final long DEPOSIT_AMOUNT = 7000000;
        final int NUMBER_OF_THREADS = 10;
        final long ACCOUNT_ID = 3;

        CountDownLatch latch = new CountDownLatch(NUMBER_OF_DEPOSITS);
        ExecutorService service = Executors.newFixedThreadPool(NUMBER_OF_THREADS);

        for (int i = 0; i < NUMBER_OF_DEPOSITS; i++) {
            int finalI = i;
            System.out.println("This is outside : " + finalI);

            service.submit(() -> {
                Response response = client.target(format(ACCOUNTS_URI + "/" + ACCOUNT_ID + "/deposit/" + DEPOSIT_AMOUNT, RULE.getLocalPort()))
                        .request()
                        .post(Entity.json(null));
                System.out.println("This is inside: " + finalI);
                latch.countDown();
                assertThat(response.getStatus()).isEqualTo(200);
            });
        }
        latch.await();
        Response getAllResponse = client.target(format(ACCOUNTS_URI + "/" + ACCOUNT_ID, RULE.getLocalPort()))
                .request()
                .get();
        Account depositedAccount = getAllResponse.readEntity(Account.class);

        assertThat(depositedAccount.getBalance()).isEqualTo(BigDecimal.valueOf(NUMBER_OF_DEPOSITS*DEPOSIT_AMOUNT).setScale(2));
    }

}