package com.cs3560.library.dao;

import com.cs3560.library.model.Student;
import com.cs3560.library.model.Loan;
import com.cs3560.library.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class StudentDaoImpl implements StudentDao {
    
    @Override
    public Student save(Student entity) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.persist(entity);
            transaction.commit();
            return entity;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Failed to save student", e);
        }
    }

    @Override
    public Optional<Student> findById(Object id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Student student = session.get(Student.class, id);
            return Optional.ofNullable(student);
        }
    }

    @Override
    public List<Student> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM Student", Student.class).list();
        }
    }

    @Override
    public Student update(Student entity) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.merge(entity);
            transaction.commit();
            return entity;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Failed to update student", e);
        }
    }

    @Override
    public void delete(Student entity) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.remove(session.contains(entity) ? entity : session.merge(entity));
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Failed to delete student", e);
        }
    }

    @Override
    public void deleteById(Object id) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            Student student = session.get(Student.class, id);
            if (student != null) {
                session.remove(student);
            }
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Failed to delete student by ID", e);
        }
    }

    @Override
    public Optional<Student> findByBroncoId(String broncoId) {
        return findById(broncoId);
    }

    @Override
    public List<Student> findByName(String name) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Student> query = session.createQuery(
                "FROM Student WHERE LOWER(name) LIKE LOWER(:name)", Student.class);
            query.setParameter("name", "%" + name + "%");
            return query.list();
        }
    }

    @Override
    public List<Loan> findActiveLoans(String broncoId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Loan> query = session.createQuery(
                "FROM Loan WHERE student.broncoId = :broncoId AND returnDate IS NULL", 
                Loan.class);
            query.setParameter("broncoId", broncoId);
            return query.list();
        }
    }

    @Override
    public List<Loan> findOverdueLoans(String broncoId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Loan> query = session.createQuery(
                "FROM Loan WHERE student.broncoId = :broncoId " +
                "AND returnDate IS NULL AND dueDate < :currentDate", 
                Loan.class);
            query.setParameter("broncoId", broncoId);
            query.setParameter("currentDate", LocalDate.now());
            return query.list();
        }
    }

    @Override
    public int countActiveLoans(String broncoId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Long> query = session.createQuery(
                "SELECT COUNT(l) FROM Loan l WHERE l.student.broncoId = :broncoId " +
                "AND l.returnDate IS NULL", 
                Long.class);
            query.setParameter("broncoId", broncoId);
            return query.getSingleResult().intValue();
        }
    }

    @Override
    public boolean hasOverdueLoans(String broncoId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Long> query = session.createQuery(
                "SELECT COUNT(l) FROM Loan l WHERE l.student.broncoId = :broncoId " +
                "AND l.returnDate IS NULL AND l.dueDate < :currentDate", 
                Long.class);
            query.setParameter("broncoId", broncoId);
            query.setParameter("currentDate", LocalDate.now());
            return query.getSingleResult() > 0;
        }
    }
} 