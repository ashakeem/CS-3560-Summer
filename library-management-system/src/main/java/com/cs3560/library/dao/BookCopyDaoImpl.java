package com.cs3560.library.dao;

import com.cs3560.library.model.BookCopy;
import com.cs3560.library.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import java.util.List;
import java.util.Optional;

public class BookCopyDaoImpl implements BookCopyDao {
    
    @Override
    public BookCopy save(BookCopy entity) {
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
            throw new RuntimeException("Failed to save book copy", e);
        }
    }

    @Override
    public Optional<BookCopy> findById(Object id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            BookCopy bookCopy = session.get(BookCopy.class, id);
            return Optional.ofNullable(bookCopy);
        }
    }

    @Override
    public List<BookCopy> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM BookCopy", BookCopy.class).list();
        }
    }

    @Override
    public BookCopy update(BookCopy entity) {
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
            throw new RuntimeException("Failed to update book copy", e);
        }
    }

    @Override
    public void delete(BookCopy entity) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.remove(session.contains(entity) ? entity : session.merge(entity));
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Failed to delete book copy", e);
        }
    }

    @Override
    public void deleteById(Object id) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            BookCopy bookCopy = session.get(BookCopy.class, id);
            if (bookCopy != null) {
                session.remove(bookCopy);
            }
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Failed to delete book copy by ID", e);
        }
    }

    @Override
    public Optional<BookCopy> findByBarcode(String barcode) {
        return findById(barcode);
    }

    @Override
    public List<BookCopy> findByIsbn(String isbn) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<BookCopy> query = session.createQuery(
                "FROM BookCopy WHERE book.isbn = :isbn", BookCopy.class);
            query.setParameter("isbn", isbn);
            return query.list();
        }
    }

    @Override
    public List<BookCopy> findAvailableCopiesByIsbn(String isbn) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<BookCopy> query = session.createQuery(
                "FROM BookCopy WHERE book.isbn = :isbn AND status = :status", 
                BookCopy.class);
            query.setParameter("isbn", isbn);
            query.setParameter("status", BookCopy.BookStatus.AVAILABLE);
            return query.list();
        }
    }

    @Override
    public List<BookCopy> findByLocation(String location) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<BookCopy> query = session.createQuery(
                "FROM BookCopy WHERE LOWER(location) LIKE LOWER(:location)", 
                BookCopy.class);
            query.setParameter("location", "%" + location + "%");
            return query.list();
        }
    }

    @Override
    public void updateStatus(String barcode, BookCopy.BookStatus status) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            BookCopy bookCopy = session.get(BookCopy.class, barcode);
            if (bookCopy != null) {
                bookCopy.setStatus(status);
                session.merge(bookCopy);
            }
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Failed to update book copy status", e);
        }
    }
} 