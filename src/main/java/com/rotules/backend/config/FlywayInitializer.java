package com.rotules.backend.config;

import jakarta.annotation.PostConstruct;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import javax.sql.DataSource;

@Configuration
@DependsOn("databaseInitializer")  // Assure que DatabaseInitializer s'exécute d'abord
public class FlywayInitializer {

    @Autowired
    private DataSource dataSource;

    @PostConstruct
    public void migrateFlyway() {
        Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration")
                .baselineOnMigrate(true)
                .load();

        flyway.migrate();
    }
}