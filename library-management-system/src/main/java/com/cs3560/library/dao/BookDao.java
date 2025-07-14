package com.cs3560.library.dao;

import com.cs3560.library.model.Book;
import java.util.List;
import java.util.Optional;

public interface BookDao extends CrudDao<Book> {
    Optional<Book> findByIsbn(String isbn);
    List<Book> searchByTitle(String title);
    List<Book> searchByAuthor(String author);
    List<Book> searchByTitleOrAuthorOrIsbn(String searchTerm);
} 