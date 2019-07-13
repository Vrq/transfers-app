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
import javax.ws.rs.core.Response;
import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(DropwizardExtensionsSupport.class)
public class TransferApplicationDepositIT {

    private static DropwizardAppExtension<TransferAppConfiguration> RULE = new DropwizardAppExtension<>(TransferApplication.class, ResourceHelpers.resourceFilePath("test-config.yml"));
    private static final String ACCOUNTS_URI = "http://localhost:%d/accounts/";
    private static final Client client = new JerseyClientBuilder().build();

    @Test
    void postDepositParallelMultipleTimesResultsInCorrectlyIncreasedAccountBalance() throws InterruptedException {
        final int numberOfDeposits = 150;
        final long depositAmount = 7000000;
        final int numberOfThreads = 10;
        final long accountId = 1;

        postCreateAccount();
        CountDownLatch latch = new CountDownLatch(numberOfDeposits);
        ExecutorService service = Executors.newFixedThreadPool(numberOfThreads);

        for (int i = 0; i < numberOfDeposits; i++) {
            service.submit(() -> {
                Response response = postDeposit(depositAmount, accountId);
                latch.countDown();
                assertThat(response.getStatus()).isEqualTo(200);
            });
        }

        latch.await();
        Response getAllResponse = getAllAccounts(accountId);
        Account depositedAccount = getAllResponse.readEntity(Account.class);

        assertThat(depositedAccount.getBalance()).isEqualTo(BigDecimal.valueOf(numberOfDeposits * depositAmount).setScale(2));
    }

    private Response getAllAccounts(long accountId) {
        return client.target(format(ACCOUNTS_URI + accountId, RULE.getLocalPort()))
                    .request()
                    .get();
    }

    private Response postDeposit(long depositAmount, long accountId) {
        return client.target(format(ACCOUNTS_URI + accountId + "/deposit/" + depositAmount, RULE.getLocalPort()))
                            .request()
                            .post(Entity.json(null));
    }

    private void postCreateAccount() {
        client.target(
                format(ACCOUNTS_URI, RULE.getLocalPort()))
                .request()
                .post(Entity.json(new Account()));
    }

}