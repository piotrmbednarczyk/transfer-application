package com.piotrbednarczyk.ta.rest;

import com.piotrbednarczyk.ta.model.Transaction;
import com.piotrbednarczyk.ta.service.TransactionService;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.math.BigDecimal;
import java.util.Optional;

import static javax.ws.rs.core.MediaType.APPLICATION_FORM_URLENCODED;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.ok;
import static javax.ws.rs.core.Response.status;

@Produces(APPLICATION_JSON)
public class TransactionResource {

    @Inject
    private TransactionService transactionService;

    @GET
    public Response getTransactions(@PathParam("id") Long accountId) {
        return ok(transactionService.getTransactionsForAccount(accountId)).build();
    }

    @GET
    @Path("/{transactionId}")
    public Response getTransactions(@PathParam("id") Long id, @PathParam("transactionId") Long transactionId) {
        Optional<Transaction> transaction = transactionService.getTransaction(id);

        if(transaction.isPresent()) {
            return ok(transaction.get()).build();
        }
        return status(NOT_FOUND).build();
    }

    @Path("/withdrawal")
    @POST
    @Consumes(value = APPLICATION_FORM_URLENCODED)
    public Response createWithdrawal(@PathParam("id") Long accountId, @FormParam("amount") BigDecimal amount) {
        return ok(transactionService.withdrawal(accountId, amount)).build();
    }

    @Path("/deposit")
    @POST
    @Consumes(value = APPLICATION_FORM_URLENCODED)
    public Response createDeposit(@PathParam("id") Long accountId, @FormParam("amount") BigDecimal amount) {
        return ok(transactionService.deposit(accountId, amount)).build();
    }

    @Path("/transfer")
    @POST
    @Consumes(value = APPLICATION_FORM_URLENCODED)
    public Response createTransfer(@PathParam("id") Long accountId, @FormParam("toAccountId") Long toAccountId,
                                   @FormParam("amount") BigDecimal amount) {
        return ok(transactionService.transfer(accountId, toAccountId, amount)).build();
    }
}
