package com.example.library.controller;

import com.example.library.model.Book;
import com.example.library.service.BookService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/books")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @PostMapping
    public ResponseEntity<Book> createBook(@Valid @RequestBody Book book) {
        Book created = bookService.createBook(book);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<Book>> getAllBooks(@RequestParam(required = false) String author) {
        List<Book> books = (author != null)
                ? bookService.getBooksByAuthor(author)
                : bookService.getAllBooks();
        return ResponseEntity.ok(books);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Book> getBookById(@PathVariable Long id) {
        return ResponseEntity.ok(bookService.getBookById(id));
    }

    @PatchMapping("/{id}/rating")
    public ResponseEntity<Book> updateRating(@PathVariable Long id, @RequestBody Map<String, Integer> request) {
        Integer rating = request.get("rating");
        if (rating == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(bookService.updateRating(id, rating));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable Long id) {
        bookService.deleteBook(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/stats/average-rating")
    public ResponseEntity<Map<String, Double>> getAverageRating() {
        return ResponseEntity.ok(Map.of("averageRating", bookService.getAverageRating()));
    }
}
