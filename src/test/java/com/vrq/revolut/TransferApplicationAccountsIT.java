package com.vrq.revolut;

import com.vrq.revolut.core.Account;
import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit5.DropwizardAppExtension;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(DropwizardExtensionsSupport.class)
public class TransferApplicationAccountsIT {

    private static DropwizardAppExtension<TransferAppConfiguration> RULE = new DropwizardAppExtension<>(TransferApplication.class, ResourceHelpers.resourceFilePath("test-config.yml"));
    private static final String ACCOUNTS_URI = "http://localhost:%d/accounts/";
    private static GenericType<List<Account>> accountListType = new GenericType<List<Account>>() {};
    private static final Client client = new JerseyClientBuilder().build();

    @Test
    void postAccountRunParallelMultipleTimesCorrectlyCreatesAccounts() throws InterruptedException {
        final int numberOfAccounts = 200;
        final int numberOfThreads = 10;

        CountDownLatch latch = new CountDownLatch(numberOfAccounts);
        ExecutorService service = Executors.newFixedThreadPool(numberOfThreads);

        for (int i = 0; i < numberOfAccounts; i++) {
            service.submit(() -> {
                Response response = postCreateAccount();
                latch.countDown();
                assertThat(response.getStatus()).isEqualTo(200);
            });
        }
        latch.await();
        Response getAllResponse = getAllAccounts();
        List<Account> accounts = getAllResponse.readEntity(accountListType);

        assertThat(accounts.size()).isEqualTo(numberOfAccounts);
    }

    private Response getAllAccounts() {
        return client.target(format(ACCOUNTS_URI, RULE.getLocalPort()))
                    .request()
                    .get();
    }

    private Response postCreateAccount() {
        return client.target(
                            format(ACCOUNTS_URI, RULE.getLocalPort()))
                            .request()
                            .post(Entity.json(new Account()));
    }
}