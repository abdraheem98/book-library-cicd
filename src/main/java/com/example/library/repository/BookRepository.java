package com.example.library.repository;

import com.example.library.model.Book;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Book entity.
 * In a real app, this would extend JpaRepository; for the demo, we use a simple in-memory map.
 */
public interface BookRepository {

    Book save(Book book);

    Optional<Book> findById(Long id);

    List<Book> findAll();

    List<Book> findByAuthor(String author);

    void deleteById(Long id);

    boolean existsById(Long id);
}
