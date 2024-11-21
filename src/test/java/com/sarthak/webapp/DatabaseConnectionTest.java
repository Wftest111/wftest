//package com.sarthak.webapp;
//
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.ActiveProfiles;
//
//import javax.sql.DataSource;
//import java.sql.Connection;
//import java.sql.SQLException;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertNotNull;
//import static org.junit.jupiter.api.Assertions.assertTrue;
//
//@SpringBootTest
//@ActiveProfiles("test")  // This will activate application-test.properties
//public class DatabaseConnectionTest {
//
//    @Autowired
//    private DataSource dataSource;
//
//    @Test
//    public void testDatabaseConnection() throws SQLException {
//        assertNotNull(dataSource);
//
//        try (Connection connection = dataSource.getConnection()) {
//            assertTrue(connection.isValid(1));
//
//            // Verify we're actually using H2
//            String dbProduct = connection.getMetaData().getDatabaseProductName();
//            assertEquals("H2", dbProduct);
//        }
//    }
//}