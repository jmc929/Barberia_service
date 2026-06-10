package com.barberia.shared.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import javax.sql.DataSource;
import java.sql.Connection;

@Component
public class DatabaseConnectionVerifier implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DatabaseConnectionVerifier.class);

    private final DataSource dataSource;

    public DatabaseConnectionVerifier(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void run(String... args) throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            if (connection != null && !connection.isClosed() && log.isInfoEnabled()) {
                log.info("Conexion a la base de datos establecida correctamente");
                log.info("URL: {}", connection.getMetaData().getURL());
                log.info("Usuario: {}", connection.getMetaData().getUserName());
                log.info("Driver: {}", connection.getMetaData().getDriverName());
            }
        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error("Error en la conexion a la base de datos: {}", e.getMessage());
            }
        }
    }
}
