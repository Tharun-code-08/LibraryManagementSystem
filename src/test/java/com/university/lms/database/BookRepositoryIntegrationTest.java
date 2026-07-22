package com.university.lms.database;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.Optional;

import org.hibernate.SessionFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

import com.university.lms.config.ConfigurationManager;
import com.university.lms.entity.Book;
import com.university.lms.entity.BookCopy;
import com.university.lms.entity.BookCopyCondition;
import com.university.lms.entity.Branch;
import com.university.lms.repository.BookRepository;
import com.university.lms.repository.impl.HibernateBookRepository;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

/**
 * A real-database counterpart to the Mockito-based {@code BookServiceImplTest}: applies the
 * actual Flyway migrations and builds a real Hibernate {@link SessionFactory} against a
 * Testcontainers MySQL instance, then exercises {@link HibernateBookRepository} against the
 * genuine schema — the class of test the Phase 9/10 missing-entity-registration bug would have
 * been caught by immediately. Skips itself (rather than failing) wherever Docker isn't
 * reachable, since this sandbox has no daemon; it runs for real on any Docker-capable machine.
 */
class BookRepositoryIntegrationTest {

    private static MySQLContainer<?> mysql;
    private static HikariDataSource dataSource;
    private static SessionFactory sessionFactory;

    @BeforeAll
    static void startContainerAndMigrate() {
        Assumptions.assumeTrue(DockerClientFactory.instance().isDockerAvailable(),
                "Docker is not available in this environment — skipping repository integration test");

        mysql = new MySQLContainer<>(DockerImageName.parse("mysql:8.0.36"))
                .withDatabaseName("library_management_test");
        mysql.start();

        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(mysql.getJdbcUrl());
        hikariConfig.setUsername(mysql.getUsername());
        hikariConfig.setPassword(mysql.getPassword());
        hikariConfig.setDriverClassName(mysql.getDriverClassName());
        dataSource = new HikariDataSource(hikariConfig);

        FlywayMigrationRunner.migrate(dataSource);
        sessionFactory = HibernateSessionFactoryProvider.build(new ConfigurationManager(), dataSource);
    }

    @AfterAll
    static void tearDown() {
        if (sessionFactory != null) {
            sessionFactory.close();
        }
        if (dataSource != null) {
            dataSource.close();
        }
        if (mysql != null) {
            mysql.stop();
        }
    }

    @Test
    void savesAndFindsABookAndItsCopyAgainstRealMysql() {
        BookRepository bookRepository = new HibernateBookRepository(sessionFactory);

        Book book = new Book("978-0132350884", "Clean Code", null, null, null, "en", null, null,
                BigDecimal.valueOf(45), null, null);
        Book savedBook = bookRepository.save(book);
        assertTrue(savedBook.getId() != null && savedBook.getId() > 0);

        Optional<Book> found = bookRepository.findByIsbn("978-0132350884");
        assertTrue(found.isPresent());
        assertEquals("Clean Code", found.get().getTitle());

        try (org.hibernate.Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            Branch branch = new Branch("Main Library", "MAIN-IT", null, null);
            session.persist(branch);
            BookCopy copy = new BookCopy(found.get(), branch, "IT-BC-1", null, null, null,
                    BookCopyCondition.NEW, null);
            session.persist(copy);
            session.getTransaction().commit();
        }

        try (org.hibernate.Session session = sessionFactory.openSession()) {
            Long copyCount = session.createQuery("select count(c) from BookCopy c where c.book.id = :bookId", Long.class)
                    .setParameter("bookId", savedBook.getId())
                    .uniqueResult();
            assertEquals(1L, copyCount);
        }
    }
}
