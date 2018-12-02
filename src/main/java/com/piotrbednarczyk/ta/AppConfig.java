package com.piotrbednarczyk.ta;

import com.piotrbednarczyk.ta.rest.AccountResource;
import com.piotrbednarczyk.ta.rest.TransactionResource;
import com.piotrbednarczyk.ta.service.AccountService;
import com.piotrbednarczyk.ta.service.TransactionService;
import io.ebean.EbeanServer;
import io.ebean.config.ServerConfig;
import io.ebean.datasource.DataSourceConfig;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.linking.DeclarativeLinkingFeature;
import org.glassfish.jersey.server.ResourceConfig;

import java.io.IOException;
import java.util.Properties;

import static io.ebean.EbeanServerFactory.create;

public class AppConfig extends ResourceConfig {

    public static final String EBEAN_PROPERTIES = "ebean.properties";

    public AppConfig() {
        packages("com.piotrbednarczyk.ta");
        register(DeclarativeLinkingFeature.class);
        register(AccountResource.class);
        register(TransactionResource.class);
        register(new AbstractBinder() {

            @Override
            protected void configure() {
                bind(create(getEbeanServerConfig())).to(EbeanServer.class);
                bindAsContract(AccountService.class);
                bindAsContract(TransactionService.class);
            }
        });
    }

    private ServerConfig getEbeanServerConfig() {
        Properties properties = getDbProperties();

        ServerConfig config = new ServerConfig();
        config.setDefaultServer(true);
        config.setName(properties.getProperty("db.name"));
        config.setDatabasePlatformName(properties.getProperty("db.platform"));
        config.setDdlGenerate(true);
        config.setDdlRun(true);
        config.setDdlSeedSql("init.sql");
        config.setDataSourceConfig(getDataSourceConfig(properties));

        return config;
    }

    private Properties getDbProperties() {
        Properties properties = new Properties();
        try {
            properties.load(this.getClass().getClassLoader().getResourceAsStream(EBEAN_PROPERTIES));
        } catch (IOException e) {
            throw new IllegalStateException("Db configuration file " + EBEAN_PROPERTIES + " not found!");
        }
        return properties;
    }

    private DataSourceConfig getDataSourceConfig(Properties properties) {
        DataSourceConfig ds = new DataSourceConfig();

        ds.setDriver(properties.getProperty("db.driver"));
        ds.setUrl(properties.getProperty("db.url"));
        ds.setUsername(properties.getProperty("db.user"));
        ds.setPassword(properties.getProperty("db.pass"));

        return ds;
    }
}
