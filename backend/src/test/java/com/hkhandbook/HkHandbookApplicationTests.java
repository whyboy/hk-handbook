package com.hkhandbook;

import java.sql.Connection;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(properties = "spring.datasource.url=jdbc:sqlite::memory:")
class HkHandbookApplicationTests {
    @Autowired DataSource dataSource;

    @Test
    void sqliteConnectionWorks() throws Exception {
        try (Connection connection = dataSource.getConnection();
             var statement = connection.createStatement()) {
            try (var result = statement.executeQuery("SELECT 1")) {
                result.next();
                assertEquals(1, result.getInt(1));
            }
            try (var result = statement.executeQuery("PRAGMA foreign_keys")) {
                result.next();
                assertEquals(1, result.getInt(1));
            }
        }
    }
}
