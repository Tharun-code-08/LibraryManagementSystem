package com.university.lms.repository.impl;

import java.util.List;
import java.util.Optional;

import org.hibernate.SessionFactory;
import org.hibernate.query.Query;

import com.university.lms.entity.Category;
import com.university.lms.repository.CategoryRepository;

public final class HibernateCategoryRepository implements CategoryRepository {

    private final SessionFactory sessionFactory;

    public HibernateCategoryRepository(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public Optional<Category> findById(Long id) {
        try (org.hibernate.Session session = sessionFactory.openSession()) {
            return Optional.ofNullable(session.get(Category.class, id));
        }
    }

    @Override
    public Optional<Category> findByNameAndParent(String name, Long parentId) {
        try (org.hibernate.Session session = sessionFactory.openSession()) {
            String hql = parentId == null
                    ? "from Category c where c.name = :name and c.parent is null"
                    : "from Category c where c.name = :name and c.parent.id = :parentId";
            Query<Category> query = session.createQuery(hql, Category.class);
            query.setParameter("name", name);
            if (parentId != null) {
                query.setParameter("parentId", parentId);
            }
            return query.uniqueResultOptional();
        }
    }

    @Override
    public List<Category> findRootCategories() {
        try (org.hibernate.Session session = sessionFactory.openSession()) {
            return session.createQuery("from Category c where c.parent is null order by c.name", Category.class).list();
        }
    }

    @Override
    public List<Category> findAll() {
        try (org.hibernate.Session session = sessionFactory.openSession()) {
            return session.createQuery("from Category c order by c.name", Category.class).list();
        }
    }

    @Override
    public Category save(Category category) {
        try (org.hibernate.Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            Category merged = session.merge(category);
            session.getTransaction().commit();
            return merged;
        }
    }

    @Override
    public void delete(Category category) {
        try (org.hibernate.Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            session.remove(session.contains(category) ? category : session.merge(category));
            session.getTransaction().commit();
        }
    }
}
