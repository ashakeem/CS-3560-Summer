package com.cs3560.library.dao;

import com.cs3560.library.model.Loan;
import com.cs3560.library.model.BookCopy;
import com.cs3560.library.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class LoanDaoImpl implements LoanDao {
    
    @Override
    public Loan save(Loan entity) {
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
            throw new RuntimeException("Failed to save loan", e);
        }
    }

    @Override
    public Optional<Loan> findById(Object id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Loan loan = session.get(Loan.class, id);
            return Optional.ofNullable(loan);
        }
    }

    @Override
    public List<Loan> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM Loan", Loan.class).list();
        }
    }

    @Override
    public Loan update(Loan entity) {
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
            throw new RuntimeException("Failed to update loan", e);
        }
    }

    @Override
    public void delete(Loan entity) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.remove(session.contains(entity) ? entity : session.merge(entity));
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Failed to delete loan", e);
        }
    }

    @Override
    public void deleteById(Object id) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            Loan loan = session.get(Loan.class, id);
            if (loan != null) {
                session.remove(loan);
            }
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Failed to delete loan by ID", e);
        }
    }

    @Override
    public List<Loan> findByStudentId(String broncoId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Loan> query = session.createQuery(
                "FROM Loan WHERE student.broncoId = :broncoId", Loan.class);
            query.setParameter("broncoId", broncoId);
            return query.list();
        }
    }

    @Override
    public List<Loan> findActiveLoans() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Loan> query = session.createQuery(
                "FROM Loan WHERE returnDate IS NULL", Loan.class);
            return query.list();
        }
    }

    @Override
    public List<Loan> findOverdueLoans() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Loan> query = session.createQuery(
                "FROM Loan WHERE returnDate IS NULL AND dueDate < :currentDate", 
                Loan.class);
            query.setParameter("currentDate", LocalDate.now());
            return query.list();
        }
    }

    @Override
    public List<Loan> findLoansByDateRange(LocalDate startDate, LocalDate endDate) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Loan> query = session.createQuery(
                "FROM Loan WHERE borrowDate >= :startDate AND borrowDate <= :endDate", 
                Loan.class);
            query.setParameter("startDate", startDate);
            query.setParameter("endDate", endDate);
            return query.list();
        }
    }

    @Override
    public List<Loan> findLoansByStudentAndDateRange(String broncoId, LocalDate startDate, LocalDate endDate) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Loan> query = session.createQuery(
                "FROM Loan WHERE student.broncoId = :broncoId " +
                "AND borrowDate >= :startDate AND borrowDate <= :endDate", 
                Loan.class);
            query.setParameter("broncoId", broncoId);
            query.setParameter("startDate", startDate);
            query.setParameter("endDate", endDate);
            return query.list();
        }
    }

    @Override
    public Loan returnLoan(Long loanId) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            
            Loan loan = session.get(Loan.class, loanId);
            if (loan == null) {
                throw new IllegalArgumentException("Loan not found with ID: " + loanId);
            }
            
            if (loan.isReturned()) {
                throw new IllegalStateException("Loan is already returned");
            }
            
            // Set return date
            loan.setReturnDate(LocalDate.now());
            
            // Update status of all book copies to available
            for (BookCopy copy : loan.getBookCopies()) {
                copy.setStatus(BookCopy.BookStatus.AVAILABLE);
                copy.setCurrentLoan(null);
                session.merge(copy);
            }
            
            session.merge(loan);
            transaction.commit();
            return loan;
            
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Failed to return loan", e);
        }
    }
} 