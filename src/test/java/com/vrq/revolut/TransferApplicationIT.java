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

import static java.math.BigDecimal.*;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(DropwizardExtensionsSupport.class)
@TestMethodOrder(OrderAnnotation.class)
public class TransferApplicationIT {

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

        Response response = client.target(String.format(ACCOUNTS_URI, RULE.getLocalPort()))
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
                        String.format(ACCOUNTS_URI, RULE.getLocalPort()))
                        .request()
                        .post(Entity.json(new Account()));

                latch.countDown();
                assertThat(response.getStatus()).isEqualTo(200);
            });
        }
        latch.await();

        Response getAllResponse = client.target(String.format(ACCOUNTS_URI, RULE.getLocalPort()))
                .request()
                .get();
        List<Account> accounts = getAllResponse.readEntity(accountListType);
        assertThat(accounts.size()).isEqualTo(NUMBER_OF_ACCOUNTS);
    }

    @Test
    @Order(3)
    void getTransfersWithoutAnyTransfersMadeReturnsEmptyList() {

        Response response = client.target(String.format(TRANSFERS_URI, RULE.getLocalPort()))
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

        for (int i = 0; i < NUMBER_OF_TRANSFERS; i++) {
            service.submit(() -> {
                Response response = client.target(
                        String.format(TRANSFERS_URI, RULE.getLocalPort()))
                        .request()
                        .post(Entity.json(new Transfer(SINGLE_TRANSFER_AMOUNT, new Account(1), new Account(2))));

                latch.countDown();
                assertThat(response.getStatus()).isEqualTo(200);
            });
        }
        latch.await();

        Response getAllResponse = client.target(String.format(TRANSFERS_URI, RULE.getLocalPort()))
                .request()
                .get();
        List<Transfer> transfers = getAllResponse.readEntity(transfersListType);
        assertThat(transfers.size()).isEqualTo(NUMBER_OF_TRANSFERS);
    }



}