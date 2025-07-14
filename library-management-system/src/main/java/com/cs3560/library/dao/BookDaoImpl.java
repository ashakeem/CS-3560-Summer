package com.cs3560.library.dao;

import com.cs3560.library.model.Book;
import com.cs3560.library.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import java.util.List;
import java.util.Optional;

public class BookDaoImpl implements BookDao {
    
    @Override
    public Book save(Book entity) {
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
            throw new RuntimeException("Failed to save book", e);
        }
    }

    @Override
    public Optional<Book> findById(Object id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Book book = session.get(Book.class, id);
            return Optional.ofNullable(book);
        }
    }

    @Override
    public List<Book> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM Book", Book.class).list();
        }
    }

    @Override
    public Book update(Book entity) {
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
            throw new RuntimeException("Failed to update book", e);
        }
    }

    @Override
    public void delete(Book entity) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.remove(session.contains(entity) ? entity : session.merge(entity));
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Failed to delete book", e);
        }
    }

    @Override
    public void deleteById(Object id) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            Book book = session.get(Book.class, id);
            if (book != null) {
                session.remove(book);
            }
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Failed to delete book by ID", e);
        }
    }

    @Override
    public Optional<Book> findByIsbn(String isbn) {
        return findById(isbn);
    }

    @Override
    public List<Book> searchByTitle(String title) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Book> query = session.createQuery(
                "FROM Book WHERE LOWER(title) LIKE LOWER(:title)", Book.class);
            query.setParameter("title", "%" + title + "%");
            return query.list();
        }
    }

    @Override
    public List<Book> searchByAuthor(String author) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Book> query = session.createQuery(
                "FROM Book WHERE LOWER(authors) LIKE LOWER(:author)", Book.class);
            query.setParameter("author", "%" + author + "%");
            return query.list();
        }
    }

    @Override
    public List<Book> searchByTitleOrAuthorOrIsbn(String searchTerm) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Book> query = session.createQuery(
                "FROM Book WHERE LOWER(title) LIKE LOWER(:term) " +
                "OR LOWER(authors) LIKE LOWER(:term) " +
                "OR LOWER(isbn) LIKE LOWER(:term)", Book.class);
            query.setParameter("term", "%" + searchTerm + "%");
            return query.list();
        }
    }
} 