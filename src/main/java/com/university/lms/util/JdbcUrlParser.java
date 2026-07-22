package com.university.lms.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Extracts the host/port/database name a {@code mysqldump}/{@code mysql} CLI invocation needs
 *  out of a MySQL JDBC URL, since {@code database.properties} only stores the URL form. */
public final class JdbcUrlParser {

    private static final Pattern MYSQL_URL_PATTERN =
            Pattern.compile("jdbc:mysql://([^:/?]+)(?::(\\d+))?/([^?]+)");

    private JdbcUrlParser() {
    }

    public record ConnectionInfo(String host, int port, String database) {
    }

    public static ConnectionInfo parse(String jdbcUrl) {
        Matcher matcher = MYSQL_URL_PATTERN.matcher(jdbcUrl);
        if (!matcher.find()) {
            throw new IllegalArgumentException("Unrecognized MySQL JDBC URL: " + jdbcUrl);
        }
        String host = matcher.group(1);
        int port = matcher.group(2) != null ? Integer.parseInt(matcher.group(2)) : 3306;
        String database = matcher.group(3);
        return new ConnectionInfo(host, port, database);
    }
}
