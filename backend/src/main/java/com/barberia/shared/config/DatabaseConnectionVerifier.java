package com.barberia.shared.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import javax.sql.DataSource;
import java.sql.Connection;

@Component
public class DatabaseConnectionVerifier implements CommandLineRunner {

    private final DataSource dataSource;

    public DatabaseConnectionVerifier(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void run(String... args) throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            if (connection != null && !connection.isClosed()) {
                System.out.println("\n✅ ============================================");
                System.out.println("✅ CONEXIÓN A LA BASE DE DATOS ESTABLECIDA CORRECTAMENTE");
                System.out.println("✅ URL: " + connection.getMetaData().getURL());
                System.out.println("✅ Usuario: " + connection.getMetaData().getUserName());
                System.out.println("✅ Driver: " + connection.getMetaData().getDriverName());
                System.out.println("✅ ============================================\n");
            }
        } catch (Exception e) {
            System.err.println("\n❌ ============================================");
            System.err.println("❌ ERROR EN LA CONEXIÓN A LA BASE DE DATOS");
            System.err.println("❌ Mensaje: " + e.getMessage());
            System.err.println("❌ ============================================\n");
            throw e;
        }
    }
}
