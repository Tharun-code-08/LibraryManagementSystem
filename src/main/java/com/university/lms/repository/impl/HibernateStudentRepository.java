package com.university.lms.repository.impl;

import java.util.List;
import java.util.Optional;

import org.hibernate.SessionFactory;
import org.hibernate.query.Query;

import com.university.lms.dto.request.StudentSearchCriteria;
import com.university.lms.entity.Student;
import com.university.lms.entity.StudentStatus;
import com.university.lms.repository.StudentRepository;

public final class HibernateStudentRepository implements StudentRepository {

    private final SessionFactory sessionFactory;

    public HibernateStudentRepository(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public Optional<Student> findById(Long id) {
        try (org.hibernate.Session session = sessionFactory.openSession()) {
            return Optional.ofNullable(session.get(Student.class, id));
        }
    }

    @Override
    public Optional<Student> findByStudentId(String studentId) {
        try (org.hibernate.Session session = sessionFactory.openSession()) {
            Query<Student> query = session.createQuery("from Student s where s.studentId = :studentId", Student.class);
            query.setParameter("studentId", studentId);
            return query.uniqueResultOptional();
        }
    }

    @Override
    public Optional<Student> findByRollNumber(String rollNumber) {
        try (org.hibernate.Session session = sessionFactory.openSession()) {
            Query<Student> query = session.createQuery("from Student s where s.rollNumber = :rollNumber", Student.class);
            query.setParameter("rollNumber", rollNumber);
            return query.uniqueResultOptional();
        }
    }

    @Override
    public List<Student> search(StudentSearchCriteria criteria) {
        try (org.hibernate.Session session = sessionFactory.openSession()) {
            Query<Student> query = session.createQuery(
                    buildSearchHql(criteria, "select s") + " order by s.user.username", Student.class);
            bindSearchParameters(query, criteria);
            query.setFirstResult(criteria.getPageNumber() * criteria.getPageSize());
            query.setMaxResults(criteria.getPageSize());
            return query.list();
        }
    }

    @Override
    public long countSearchResults(StudentSearchCriteria criteria) {
        try (org.hibernate.Session session = sessionFactory.openSession()) {
            Query<Long> query = session.createQuery(buildSearchHql(criteria, "select count(s)"), Long.class);
            bindSearchParameters(query, criteria);
            return query.uniqueResult();
        }
    }

    private String buildSearchHql(StudentSearchCriteria criteria, String selectClause) {
        StringBuilder hql = new StringBuilder(selectClause).append(" from Student s where 1 = 1");
        if (criteria.getKeyword() != null && !criteria.getKeyword().isBlank()) {
            hql.append(" and (lower(s.user.username) like :keyword or lower(s.studentId) like :keyword")
                    .append(" or lower(s.rollNumber) like :keyword)");
        }
        if (criteria.getDepartment() != null && !criteria.getDepartment().isBlank()) {
            hql.append(" and s.department = :department");
        }
        if (criteria.getYear() != null) {
            hql.append(" and s.year = :year");
        }
        if (criteria.getStatus() != null && !criteria.getStatus().isBlank()) {
            hql.append(" and s.status = :status");
        }
        return hql.toString();
    }

    private void bindSearchParameters(Query<?> query, StudentSearchCriteria criteria) {
        if (criteria.getKeyword() != null && !criteria.getKeyword().isBlank()) {
            query.setParameter("keyword", "%" + criteria.getKeyword().toLowerCase() + "%");
        }
        if (criteria.getDepartment() != null && !criteria.getDepartment().isBlank()) {
            query.setParameter("department", criteria.getDepartment());
        }
        if (criteria.getYear() != null) {
            query.setParameter("year", criteria.getYear());
        }
        if (criteria.getStatus() != null && !criteria.getStatus().isBlank()) {
            query.setParameter("status", StudentStatus.valueOf(criteria.getStatus()));
        }
    }

    @Override
    public Student save(Student student) {
        try (org.hibernate.Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            Student merged = session.merge(student);
            session.getTransaction().commit();
            return merged;
        }
    }
}
