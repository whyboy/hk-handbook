package com.hkhandbook;

import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest(properties = "spring.datasource.url=jdbc:sqlite::memory:")
class HealthControllerTests {
    @Autowired HealthController controller;

    @Test
    void connectedDatabaseIsHealthy() {
        var response = controller.health();
        assertEquals(200, response.getStatusCode().value());
        assertEquals("UP", response.getBody().get("status"));
    }

    @Test
    void unavailableDatabaseReturns503WithoutConnectionDetails() throws Exception {
        var source = mock(DataSource.class);
        when(source.getConnection()).thenThrow(new SQLException("private database path"));
        var response = new HealthController(source).health();
        assertEquals(503, response.getStatusCode().value());
        assertEquals(java.util.Map.of("status", "DOWN"), response.getBody());
    }
}
