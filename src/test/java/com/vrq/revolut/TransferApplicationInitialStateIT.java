package com.vrq.revolut;

import com.vrq.revolut.core.Account;
import com.vrq.revolut.core.Transfer;
import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit5.DropwizardAppExtension;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import java.util.List;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(DropwizardExtensionsSupport.class)
public class TransferApplicationInitialStateIT {

    private static DropwizardAppExtension<TransferAppConfiguration> RULE = new DropwizardAppExtension<>(TransferApplication.class, ResourceHelpers.resourceFilePath("test-config.yml"));
    private static final String ACCOUNTS_URI = "http://localhost:%d/accounts/";
    private static final String TRANSFERS_URI = "http://localhost:%d/transfers/";
    private static GenericType<List<Account>> accountListType = new GenericType<List<Account>>() {};
    private static GenericType<List<Transfer>> transfersListType = new GenericType<List<Transfer>>() {};
    private static final Client client = new JerseyClientBuilder().build();

    @Test
    void getAccountsWithFreshDbReturnsEmptyList() {
        Response response = getAll(ACCOUNTS_URI);
        List<Account> accounts = response.readEntity(accountListType);

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(accounts.size()).isEqualTo(0);
    }

    @Test
    void getTransfersWithoutAnyTransfersMadeReturnsEmptyList() {
        Response response = getAll(TRANSFERS_URI);
        List<Transfer> transfers = response.readEntity(transfersListType);

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(transfers.size()).isEqualTo(0);
    }

    private Response getAll(String resourceUri) {
        return client.target(format(resourceUri, RULE.getLocalPort()))
                .request()
                .get();
    }
}