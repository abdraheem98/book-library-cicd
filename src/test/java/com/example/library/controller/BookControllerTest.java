package com.example.library.controller;

import com.example.library.exception.BookNotFoundException;
import com.example.library.model.Book;
import com.example.library.service.BookService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Web layer tests for BookController using @WebMvcTest + @MockBean.
 *
 * Key concepts:
 *   - @WebMvcTest loads only the web layer (faster than full @SpringBootTest)
 *   - @MockBean replaces the real BookService with a mock
 *   - MockMvc simulates HTTP requests
 */
@WebMvcTest(BookController.class)
@DisplayName("BookController Web Layer Tests")
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookService bookService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("POST /api/books should create a book")
    void shouldCreateBook() throws Exception {
        Book input = new Book(null, "1984", "George Orwell", 5);
        Book saved = new Book(1L, "1984", "George Orwell", 5);
        when(bookService.createBook(any(Book.class))).thenReturn(saved);

        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("1984"));
    }

    @Test
    @DisplayName("POST /api/books should return 400 for missing title")
    void shouldRejectInvalidBook() throws Exception {
        Book invalid = new Book(null, "", "Author", 5);

        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/books should return all books")
    void shouldReturnAllBooks() throws Exception {
        when(bookService.getAllBooks()).thenReturn(Arrays.asList(
                new Book(1L, "Book 1", "Author 1", 4),
                new Book(2L, "Book 2", "Author 2", 5)
        ));

        mockMvc.perform(get("/api/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @DisplayName("GET /api/books/{id} should return book when found")
    void shouldReturnBookById() throws Exception {
        when(bookService.getBookById(1L)).thenReturn(new Book(1L, "Test", "Author", 4));

        mockMvc.perform(get("/api/books/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Test"));
    }

    @Test
    @DisplayName("GET /api/books/{id} should return 404 when not found")
    void shouldReturn404WhenNotFound() throws Exception {
        when(bookService.getBookById(999L)).thenThrow(new BookNotFoundException(999L));

        mockMvc.perform(get("/api/books/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @DisplayName("DELETE /api/books/{id} should return 204")
    void shouldDeleteBook() throws Exception {
        doNothing().when(bookService).deleteBook(1L);

        mockMvc.perform(delete("/api/books/1"))
                .andExpect(status().isNoContent());

        verify(bookService).deleteBook(1L);
    }

    @Test
    @DisplayName("PATCH /api/books/{id}/rating should update rating")
    void shouldUpdateRating() throws Exception {
        Book updated = new Book(1L, "Test", "Author", 3);
        when(bookService.updateRating(eq(1L), eq(3))).thenReturn(updated);

        mockMvc.perform(patch("/api/books/1/rating")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("rating", 3))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rating").value(3));
    }

    @Test
    @DisplayName("GET /api/books/stats/average-rating should return average")
    void shouldReturnAverageRating() throws Exception {
        when(bookService.getAverageRating()).thenReturn(4.2);

        mockMvc.perform(get("/api/books/stats/average-rating"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.averageRating").value(4.2));
    }
}
