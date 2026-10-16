package com.hkhandbook;

import java.util.Map;
import javax.sql.DataSource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
class HealthController {
    private final DataSource dataSource;

    HealthController(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @GetMapping("/api/health")
    ResponseEntity<Map<String, String>> health() {
        try (var connection = dataSource.getConnection();
             var statement = connection.createStatement();
             var result = statement.executeQuery("SELECT 1")) {
            if (result.next() && result.getInt(1) == 1) {
                return ResponseEntity.ok(Map.of("status", "UP"));
            }
        } catch (Exception ignored) {
            // Do not expose database paths or connection details in a public endpoint.
        }
        return ResponseEntity.status(503).body(Map.of("status", "DOWN"));
    }
}
