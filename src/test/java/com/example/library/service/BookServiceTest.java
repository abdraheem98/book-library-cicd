package com.example.library.service;

import com.example.library.exception.BookNotFoundException;
import com.example.library.model.Book;
import com.example.library.repository.BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Unit tests for BookService using JUnit 5 + Mockito.
 *
 * Key concepts demonstrated:
 *   - @Mock creates a mock repository (no real database)
 *   - @InjectMocks injects mocks into the service
 *   - when().thenReturn() defines mock behavior
 *   - verify() confirms methods were called
 *   - assertThrows() tests exception scenarios
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("BookService Unit Tests")
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private BookService bookService;

    private Book sampleBook;

    @BeforeEach
    void setUp() {
        sampleBook = new Book(1L, "Clean Code", "Robert Martin", 5);
    }

    @Nested
    @DisplayName("createBook")
    class CreateBookTests {

        @Test
        @DisplayName("should save book and return saved entity")
        void shouldCreateBook() {
            // Arrange
            Book newBook = new Book(null, "Effective Java", "Joshua Bloch", 5);
            Book savedBook = new Book(1L, "Effective Java", "Joshua Bloch", 5);
            when(bookRepository.save(any(Book.class))).thenReturn(savedBook);

            // Act
            Book result = bookService.createBook(newBook);

            // Assert
            assertNotNull(result.getId());
            assertEquals("Effective Java", result.getTitle());
            verify(bookRepository, times(1)).save(newBook);
        }
    }

    @Nested
    @DisplayName("getBookById")
    class GetBookByIdTests {

        @Test
        @DisplayName("should return book when found")
        void shouldReturnBookWhenFound() {
            when(bookRepository.findById(1L)).thenReturn(Optional.of(sampleBook));

            Book result = bookService.getBookById(1L);

            assertEquals("Clean Code", result.getTitle());
            verify(bookRepository).findById(1L);
        }

        @Test
        @DisplayName("should throw BookNotFoundException when book does not exist")
        void shouldThrowWhenNotFound() {
            when(bookRepository.findById(999L)).thenReturn(Optional.empty());

            BookNotFoundException exception = assertThrows(
                    BookNotFoundException.class,
                    () -> bookService.getBookById(999L)
            );
            assertTrue(exception.getMessage().contains("999"));
        }
    }

    @Nested
    @DisplayName("getAllBooks")
    class GetAllBooksTests {

        @Test
        @DisplayName("should return all books from repository")
        void shouldReturnAllBooks() {
            Book book2 = new Book(2L, "Refactoring", "Martin Fowler", 4);
            when(bookRepository.findAll()).thenReturn(Arrays.asList(sampleBook, book2));

            List<Book> books = bookService.getAllBooks();

            assertEquals(2, books.size());
            verify(bookRepository, times(1)).findAll();
        }

        @Test
        @DisplayName("should return empty list when no books exist")
        void shouldReturnEmptyList() {
            when(bookRepository.findAll()).thenReturn(Collections.emptyList());

            List<Book> books = bookService.getAllBooks();

            assertTrue(books.isEmpty());
        }
    }

    @Nested
    @DisplayName("getBooksByAuthor")
    class GetBooksByAuthorTests {

        @Test
        @DisplayName("should return books filtered by author")
        void shouldReturnBooksByAuthor() {
            when(bookRepository.findByAuthor("Robert Martin"))
                    .thenReturn(List.of(sampleBook));

            List<Book> books = bookService.getBooksByAuthor("Robert Martin");

            assertEquals(1, books.size());
            assertEquals("Robert Martin", books.get(0).getAuthor());
        }
    }

    @Nested
    @DisplayName("updateRating")
    class UpdateRatingTests {

        @Test
        @DisplayName("should update rating when valid")
        void shouldUpdateRating() {
            when(bookRepository.findById(1L)).thenReturn(Optional.of(sampleBook));
            when(bookRepository.save(any(Book.class))).thenAnswer(i -> i.getArgument(0));

            Book updated = bookService.updateRating(1L, 3);

            assertEquals(3, updated.getRating());
            verify(bookRepository).save(sampleBook);
        }

        @Test
        @DisplayName("should throw exception when rating is negative")
        void shouldThrowWhenRatingNegative() {
            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> bookService.updateRating(1L, -1)
            );
            assertTrue(ex.getMessage().contains("0 and 5"));
            verify(bookRepository, never()).findById(anyLong());
        }

        @Test
        @DisplayName("should throw exception when rating exceeds 5")
        void shouldThrowWhenRatingTooHigh() {
            assertThrows(
                    IllegalArgumentException.class,
                    () -> bookService.updateRating(1L, 6)
            );
        }
    }

    @Nested
    @DisplayName("deleteBook")
    class DeleteBookTests {

        @Test
        @DisplayName("should delete book when it exists")
        void shouldDeleteBook() {
            when(bookRepository.existsById(1L)).thenReturn(true);

            bookService.deleteBook(1L);

            verify(bookRepository, times(1)).deleteById(1L);
        }

        @Test
        @DisplayName("should throw exception when book does not exist")
        void shouldThrowWhenDeletingNonExistent() {
            when(bookRepository.existsById(999L)).thenReturn(false);

            assertThrows(
                    BookNotFoundException.class,
                    () -> bookService.deleteBook(999L)
            );
            verify(bookRepository, never()).deleteById(anyLong());
        }
    }

    @Nested
    @DisplayName("getAverageRating")
    class GetAverageRatingTests {

        @Test
        @DisplayName("should calculate average correctly")
        void shouldCalculateAverage() {
            List<Book> books = Arrays.asList(
                    new Book(1L, "Book 1", "Author", 5),
                    new Book(2L, "Book 2", "Author", 3),
                    new Book(3L, "Book 3", "Author", 4)
            );
            when(bookRepository.findAll()).thenReturn(books);

            double avg = bookService.getAverageRating();

            assertEquals(4.0, avg, 0.01);
        }

        @Test
        @DisplayName("should return 0 when no books exist")
        void shouldReturnZeroWhenEmpty() {
            when(bookRepository.findAll()).thenReturn(Collections.emptyList());

            double avg = bookService.getAverageRating();

            assertEquals(0.0, avg);
        }
    }
}
