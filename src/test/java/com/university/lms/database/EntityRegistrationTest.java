package com.university.lms.database;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.persistence.Entity;

import org.junit.jupiter.api.Test;

/**
 * Regression guard for the exact bug hit twice already (Phase 9's {@code Notification} and
 * Phase 10's {@code Setting}/{@code Backup}): a new JPA entity compiles cleanly and every
 * Mockito-based unit test still passes, because nothing in those tests ever builds a real
 * Hibernate {@link org.hibernate.SessionFactory} — the missing
 * {@code MetadataSources.addAnnotatedClass(...)} registration only surfaces at runtime against
 * an actual database. This scans the compiled {@code entity} package for every {@code @Entity}
 * class and asserts each one is present in {@link HibernateSessionFactoryProvider#ENTITY_CLASSES}.
 */
class EntityRegistrationTest {

    @Test
    void everyEntityClassIsRegisteredWithHibernate() throws Exception {
        List<Class<?>> entityClasses = scanEntityPackage();
        assertFalse(entityClasses.isEmpty(), "Expected to find at least one @Entity class on the classpath");

        Set<Class<?>> registered = new HashSet<>(HibernateSessionFactoryProvider.ENTITY_CLASSES);
        List<String> missing = new ArrayList<>();
        for (Class<?> entityClass : entityClasses) {
            if (!registered.contains(entityClass)) {
                missing.add(entityClass.getName());
            }
        }

        assertTrue(missing.isEmpty(), "Entity class(es) not registered in "
                + "HibernateSessionFactoryProvider.ENTITY_CLASSES — add them there: " + missing);
    }

    private List<Class<?>> scanEntityPackage() throws Exception {
        String packageName = "com.university.lms.entity";
        String packagePath = packageName.replace('.', '/');
        URL packageUrl = Thread.currentThread().getContextClassLoader().getResource(packagePath);
        assertTrue(packageUrl != null, "Could not locate compiled entity package on the classpath");

        File directory = new File(packageUrl.toURI());
        File[] classFiles = directory.listFiles((dir, name) -> name.endsWith(".class"));
        assertTrue(classFiles != null, "Entity package directory listing returned null");

        List<Class<?>> entityClasses = new ArrayList<>();
        for (File classFile : classFiles) {
            String simpleName = classFile.getName().substring(0, classFile.getName().length() - ".class".length());
            Class<?> loaded = Class.forName(packageName + "." + simpleName);
            if (loaded.isAnnotationPresent(Entity.class)) {
                entityClasses.add(loaded);
            }
        }
        return entityClasses;
    }
}
