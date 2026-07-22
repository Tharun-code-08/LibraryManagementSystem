package com.university.lms.repository.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.SessionFactory;
import org.hibernate.query.Query;

import com.university.lms.dto.response.CategoryDistributionDTO;
import com.university.lms.dto.response.MonthlyActivityDTO;
import com.university.lms.dto.response.PopularBookDTO;
import com.university.lms.entity.BookCopyStatus;
import com.university.lms.entity.IssueStatus;
import com.university.lms.entity.ReservationStatus;
import com.university.lms.repository.DashboardRepository;

public final class HibernateDashboardRepository implements DashboardRepository {

    private final SessionFactory sessionFactory;

    public HibernateDashboardRepository(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public long countActiveBooks() {
        try (org.hibernate.Session session = sessionFactory.openSession()) {
            return session.createQuery("select count(b) from Book b where b.deleted = false", Long.class)
                    .uniqueResult();
        }
    }

    @Override
    public long countCopiesByStatus(BookCopyStatus status) {
        try (org.hibernate.Session session = sessionFactory.openSession()) {
            Query<Long> query = session.createQuery(
                    "select count(c) from BookCopy c where c.status = :status", Long.class);
            query.setParameter("status", status);
            return query.uniqueResult();
        }
    }

    @Override
    public long countOpenOverdueIssues(LocalDateTime asOf) {
        try (org.hibernate.Session session = sessionFactory.openSession()) {
            Query<Long> query = session.createQuery(
                    "select count(i) from Issue i where i.status in (:issued, :overdue) and i.dueDate < :asOf",
                    Long.class);
            query.setParameter("issued", IssueStatus.ISSUED);
            query.setParameter("overdue", IssueStatus.OVERDUE);
            query.setParameter("asOf", asOf);
            return query.uniqueResult();
        }
    }

    @Override
    public long countActiveReservations() {
        try (org.hibernate.Session session = sessionFactory.openSession()) {
            Query<Long> query = session.createQuery(
                    "select count(r) from Reservation r where r.status in (:waiting, :ready)", Long.class);
            query.setParameter("waiting", ReservationStatus.WAITING);
            query.setParameter("ready", ReservationStatus.READY);
            return query.uniqueResult();
        }
    }

    @Override
    public long countStudents() {
        try (org.hibernate.Session session = sessionFactory.openSession()) {
            return session.createQuery("select count(s) from Student s", Long.class).uniqueResult();
        }
    }

    @Override
    public long countFaculty() {
        try (org.hibernate.Session session = sessionFactory.openSession()) {
            return session.createQuery("select count(f) from Faculty f", Long.class).uniqueResult();
        }
    }

    @Override
    public List<MonthlyActivityDTO> getMonthlyIssuesAndReturns(LocalDate since) {
        try (org.hibernate.Session session = sessionFactory.openSession()) {
            LocalDateTime sinceStart = since.atStartOfDay();

            Map<String, Long> issuedByMonth = groupCountsByMonth(session,
                    "select year(i.issueDate), month(i.issueDate), count(i) from Issue i where i.issueDate >= :since "
                            + "group by year(i.issueDate), month(i.issueDate)",
                    sinceStart);
            Map<String, Long> returnedByMonth = groupCountsByMonth(session,
                    "select year(r.returnDate), month(r.returnDate), count(r) from Return r where r.returnDate >= :since "
                            + "group by year(r.returnDate), month(r.returnDate)",
                    sinceStart);

            List<MonthlyActivityDTO> result = new ArrayList<>();
            YearMonth cursor = YearMonth.from(since);
            YearMonth end = YearMonth.now();
            while (!cursor.isAfter(end)) {
                String key = cursor.toString();
                result.add(new MonthlyActivityDTO(key, issuedByMonth.getOrDefault(key, 0L), returnedByMonth.getOrDefault(key, 0L)));
                cursor = cursor.plusMonths(1);
            }
            return result;
        }
    }

    private Map<String, Long> groupCountsByMonth(org.hibernate.Session session, String hql, LocalDateTime since) {
        Query<Object[]> query = session.createQuery(hql, Object[].class);
        query.setParameter("since", since);
        Map<String, Long> map = new HashMap<>();
        for (Object[] row : query.list()) {
            int year = ((Number) row[0]).intValue();
            int month = ((Number) row[1]).intValue();
            long count = ((Number) row[2]).longValue();
            map.put(YearMonth.of(year, month).toString(), count);
        }
        return map;
    }

    @Override
    public List<CategoryDistributionDTO> getCategoryDistribution() {
        try (org.hibernate.Session session = sessionFactory.openSession()) {
            Query<Object[]> query = session.createQuery(
                    "select b.category.name, count(b) from Book b where b.deleted = false and b.category is not null "
                            + "group by b.category.name order by count(b) desc",
                    Object[].class);
            List<CategoryDistributionDTO> result = new ArrayList<>();
            for (Object[] row : query.list()) {
                result.add(new CategoryDistributionDTO((String) row[0], ((Number) row[1]).longValue()));
            }
            return result;
        }
    }

    @Override
    public List<PopularBookDTO> getPopularBooks(int limit) {
        try (org.hibernate.Session session = sessionFactory.openSession()) {
            Query<Object[]> query = session.createQuery(
                    "select i.bookCopy.book.title, count(i) from Issue i group by i.bookCopy.book.title "
                            + "order by count(i) desc",
                    Object[].class);
            query.setMaxResults(limit);
            List<PopularBookDTO> result = new ArrayList<>();
            for (Object[] row : query.list()) {
                result.add(new PopularBookDTO((String) row[0], ((Number) row[1]).longValue()));
            }
            return result;
        }
    }
}
