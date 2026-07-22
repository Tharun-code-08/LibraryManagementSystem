package com.university.lms.database;

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

        // Entity classes are added here as each module's persistence layer is implemented.
        metadataSources.addAnnotatedClass(Branch.class);
        metadataSources.addAnnotatedClass(Permission.class);
        metadataSources.addAnnotatedClass(Role.class);
        metadataSources.addAnnotatedClass(User.class);
        metadataSources.addAnnotatedClass(UserSession.class);
        metadataSources.addAnnotatedClass(PasswordResetToken.class);
        metadataSources.addAnnotatedClass(AuditLog.class);
        metadataSources.addAnnotatedClass(Author.class);
        metadataSources.addAnnotatedClass(Publisher.class);
        metadataSources.addAnnotatedClass(Category.class);
        metadataSources.addAnnotatedClass(Tag.class);
        metadataSources.addAnnotatedClass(Book.class);
        metadataSources.addAnnotatedClass(BookCopy.class);
        metadataSources.addAnnotatedClass(Student.class);
        metadataSources.addAnnotatedClass(Faculty.class);
        metadataSources.addAnnotatedClass(MembershipType.class);
        metadataSources.addAnnotatedClass(Membership.class);
        metadataSources.addAnnotatedClass(Issue.class);
        metadataSources.addAnnotatedClass(Return.class);
        metadataSources.addAnnotatedClass(Reservation.class);
        metadataSources.addAnnotatedClass(Fine.class);
        metadataSources.addAnnotatedClass(Payment.class);
        metadataSources.addAnnotatedClass(Supplier.class);
        metadataSources.addAnnotatedClass(PurchaseOrder.class);
        metadataSources.addAnnotatedClass(PurchaseOrderItem.class);
        metadataSources.addAnnotatedClass(Invoice.class);
        metadataSources.addAnnotatedClass(InventoryAudit.class);
        metadataSources.addAnnotatedClass(InventoryAuditItem.class);
        metadataSources.addAnnotatedClass(Notification.class);
        metadataSources.addAnnotatedClass(Setting.class);
        metadataSources.addAnnotatedClass(Backup.class);

        return metadataSources.buildMetadata().buildSessionFactory();
    }
}
