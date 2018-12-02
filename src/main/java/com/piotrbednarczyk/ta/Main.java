package com.piotrbednarczyk.ta;

import org.glassfish.grizzly.http.server.HttpServer;

import java.io.IOException;

import static java.lang.String.join;
import static java.lang.System.in;
import static java.lang.System.out;
import static java.net.URI.create;
import static java.text.MessageFormat.format;
import static org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory.createHttpServer;

public class Main {
    public static final String BASE_URI = "http://localhost:8080/rest/";

    public static void main(String[] args) throws IOException {
        HttpServer server = startServer();

        out.println(join("\n",
                format("App started with WADL available at {0}application.wadl", BASE_URI),
                "Hit enter to stop it...", ""));
        in.read();

        server.shutdown();
    }

    public static HttpServer startServer() {
        return createHttpServer(
                create(BASE_URI),
                new AppConfig());
    }
}

