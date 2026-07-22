package com.university.lms.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import com.university.lms.util.JdbcUrlParser.ConnectionInfo;

class JdbcUrlParserTest {

    @Test
    void parsesHostPortAndDatabase() {
        ConnectionInfo info = JdbcUrlParser.parse(
                "jdbc:mysql://localhost:3306/library_management?useSSL=true&serverTimezone=UTC");

        assertEquals("localhost", info.host());
        assertEquals(3306, info.port());
        assertEquals("library_management", info.database());
    }

    @Test
    void defaultsToPort3306WhenOmitted() {
        ConnectionInfo info = JdbcUrlParser.parse("jdbc:mysql://db.internal/library_management");

        assertEquals("db.internal", info.host());
        assertEquals(3306, info.port());
        assertEquals("library_management", info.database());
    }

    @Test
    void rejectsNonMysqlUrls() {
        assertThrows(IllegalArgumentException.class, () -> JdbcUrlParser.parse("jdbc:postgresql://localhost/db"));
    }
}
