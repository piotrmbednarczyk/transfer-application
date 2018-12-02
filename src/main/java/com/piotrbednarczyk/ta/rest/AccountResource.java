package com.piotrbednarczyk.ta.rest;

import com.piotrbednarczyk.ta.model.Account;
import com.piotrbednarczyk.ta.service.AccountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.container.ResourceContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.Optional;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.*;

@Path("/accounts")
@Produces(APPLICATION_JSON)
public class AccountResource {

    private static final Logger LOG = LoggerFactory.getLogger(AccountResource.class);

    @Inject
    private AccountService accountService;

    @Context
    private ResourceContext resourceContext;

    @GET
    @javax.ws.rs.Produces(APPLICATION_JSON)
    public Response getAccounts() {
        return ok(accountService.getAccounts()).build();
    }

    @GET
    @Path("/{id}")
    public Response getAccount(@PathParam("id") Long id) {
        Optional<Account> account = accountService.getAccount(id);

        if(account.isPresent()) {
            return ok(account.get()).build();
        }
        return status(NOT_FOUND).build();
    }

    @POST
    public Response createAccount(@Context UriInfo uriInfo) {
        return ok(accountService.createAccount()).build();
    }

    @DELETE
    @Path("/{id}")
    public Response deleteAccount(@PathParam("id") Long id) {
        accountService.deleteAccount(id);
        return noContent().build();
    }

    @Path("/{id}/transactions")
    public TransactionResource getTransactionResource() {
        return resourceContext.getResource(TransactionResource.class);
    }
}
