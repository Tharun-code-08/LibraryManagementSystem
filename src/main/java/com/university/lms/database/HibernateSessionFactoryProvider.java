package com.university.lms.database;

import java.util.List;

import javax.sql.DataSource;

import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Environment;
import org.hibernate.service.ServiceRegistry;

import com.university.lms.config.ConfigurationManager;
import com.university.lms.entity.AuditLog;
import com.university.lms.entity.Author;
import com.university.lms.entity.Backup;
import com.university.lms.entity.Book;
import com.university.lms.entity.BookCopy;
import com.university.lms.entity.Branch;
import com.university.lms.entity.Category;
import com.university.lms.entity.Faculty;
import com.university.lms.entity.Fine;
import com.university.lms.entity.InventoryAudit;
import com.university.lms.entity.InventoryAuditItem;
import com.university.lms.entity.Invoice;
import com.university.lms.entity.Issue;
import com.university.lms.entity.Membership;
import com.university.lms.entity.MembershipType;
import com.university.lms.entity.Notification;
import com.university.lms.entity.PasswordResetToken;
import com.university.lms.entity.Payment;
import com.university.lms.entity.Permission;
import com.university.lms.entity.Publisher;
import com.university.lms.entity.PurchaseOrder;
import com.university.lms.entity.PurchaseOrderItem;
import com.university.lms.entity.Reservation;
import com.university.lms.entity.Return;
import com.university.lms.entity.Role;
import com.university.lms.entity.Setting;
import com.university.lms.entity.Student;
import com.university.lms.entity.Supplier;
import com.university.lms.entity.Tag;
import com.university.lms.entity.User;
import com.university.lms.entity.UserSession;

/**
 * Builds the single application-wide Hibernate {@link SessionFactory}, reusing the already
 * constructed {@link DataSource} (HikariCP) rather than letting Hibernate open its own pool.
 * Entity classes are registered here as they are introduced module-by-module in later phases.
 */
public final class HibernateSessionFactoryProvider {

    /**
     * Every JPA entity in the application, registered explicitly rather than via package
     * scanning. A class missing from this list compiles fine and every Mockito-based unit test
     * still passes (nothing here touches a real database) — it only fails at runtime against a
     * real database, which is exactly the class of bug {@code ArchitectureAndMappingTest}
     * guards against by asserting this list matches every {@code @Entity} class on the compiled
     * classpath. Keep this the single place new entities are added.
     */
    public static final List<Class<?>> ENTITY_CLASSES = List.of(
            Branch.class, Permission.class, Role.class, User.class, UserSession.class,
            PasswordResetToken.class, AuditLog.class, Author.class, Publisher.class, Category.class,
            Tag.class, Book.class, BookCopy.class, Student.class, Faculty.class, MembershipType.class,
            Membership.class, Issue.class, Return.class, Reservation.class, Fine.class, Payment.class,
            Supplier.class, PurchaseOrder.class, PurchaseOrderItem.class, Invoice.class, InventoryAudit.class,
            InventoryAuditItem.class, Notification.class, Setting.class, Backup.class);

    private HibernateSessionFactoryProvider() {
    }

    public static SessionFactory build(ConfigurationManager config, DataSource dataSource) {
        StandardServiceRegistryBuilder registryBuilder = new StandardServiceRegistryBuilder();
        registryBuilder.applySetting(Environment.DATASOURCE, dataSource);
        registryBuilder.applySetting(Environment.DIALECT, config.db("hibernate.dialect"));
        registryBuilder.applySetting(Environment.HBM2DDL_AUTO, config.db("hibernate.hbm2ddl.auto", "validate"));
        registryBuilder.applySetting(Environment.SHOW_SQL, config.db("hibernate.show_sql", "false"));
        registryBuilder.applySetting(Environment.FORMAT_SQL, config.db("hibernate.format_sql", "true"));

        ServiceRegistry serviceRegistry = registryBuilder.build();
        MetadataSources metadataSources = new MetadataSources(serviceRegistry);
        ENTITY_CLASSES.forEach(metadataSources::addAnnotatedClass);

        return metadataSources.buildMetadata().buildSessionFactory();
    }
}
