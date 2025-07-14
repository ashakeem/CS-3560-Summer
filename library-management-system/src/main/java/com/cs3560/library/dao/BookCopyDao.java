package com.cs3560.library.dao;

import com.cs3560.library.model.BookCopy;
import java.util.List;
import java.util.Optional;

public interface BookCopyDao extends CrudDao<BookCopy> {
    Optional<BookCopy> findByBarcode(String barcode);
    List<BookCopy> findByIsbn(String isbn);
    List<BookCopy> findAvailableCopiesByIsbn(String isbn);
    List<BookCopy> findByLocation(String location);
    void updateStatus(String barcode, BookCopy.BookStatus status);
} 