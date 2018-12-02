package com.piotrbednarczyk.ta.service.error;

import javax.ws.rs.WebApplicationException;

import static javax.ws.rs.core.Response.Status.CONFLICT;
import static javax.ws.rs.core.Response.status;

public class TransactionException extends WebApplicationException {

    public TransactionException(String message) {
        super(message, status(CONFLICT).entity(message).build());
    }
}
