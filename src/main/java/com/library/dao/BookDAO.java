package com.library.dao;

import com.library.exception.RecordNotFoundException;
import com.library.model.Book;
import com.library.util.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Data-access object for the books table. Every query uses
 * PreparedStatement and try-with-resources for Connection/Statement/ResultSet.
 */
public class BookDAO {

    public Book create(Book book) throws SQLException {
        String sql = "INSERT INTO books (title, author, isbn, category, total_copies, available_copies) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, book.getTitle());
            ps.setString(2, book.getAuthor());
            ps.setString(3, book.getIsbn());
            ps.setString(4, book.getCategory());
            ps.setInt(5, book.getTotalCopies());
            ps.setInt(6, book.getAvailableCopies());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    book.setId(keys.getInt(1));
                }
            }
            return book;
        }
    }

    public Book findById(int id) throws SQLException, RecordNotFoundException {
        String sql = "SELECT * FROM books WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        throw new RecordNotFoundException("No book found with id " + id);
    }

    public List<Book> findAll() throws SQLException {
        String sql = "SELECT * FROM books ORDER BY title";
        List<Book> books = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                books.add(mapRow(rs));
            }
        }
        return books;
    }

    /** Search by title OR author (partial, case-insensitive). Overloaded below by field-specific search. */
    public List<Book> search(String keyword) throws SQLException {
        String sql = "SELECT * FROM books WHERE LOWER(title) LIKE ? OR LOWER(author) LIKE ? ORDER BY title";
        List<Book> results = new ArrayList<>();
        String pattern = "%" + keyword.toLowerCase() + "%";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(mapRow(rs));
                }
            }
        }
        return results;
    }

    /** Overload: search a specific field ("isbn" or "category") for an exact match. Demonstrates method overloading. */
    public List<Book> search(String field, String value) throws SQLException {
        String column = switch (field.toLowerCase()) {
            case "isbn" -> "isbn";
            case "category" -> "category";
            default -> throw new IllegalArgumentException("Unsupported search field: " + field);
        };
        String sql = "SELECT * FROM books WHERE " + column + " = ? ORDER BY title";
        List<Book> results = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, value);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(mapRow(rs));
                }
            }
        }
        return results;
    }

    public void update(Book book) throws SQLException, RecordNotFoundException {
        String sql = "UPDATE books SET title = ?, author = ?, isbn = ?, category = ?, "
                + "total_copies = ?, available_copies = ? WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, book.getTitle());
            ps.setString(2, book.getAuthor());
            ps.setString(3, book.getIsbn());
            ps.setString(4, book.getCategory());
            ps.setInt(5, book.getTotalCopies());
            ps.setInt(6, book.getAvailableCopies());
            ps.setInt(7, book.getId());
            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new RecordNotFoundException("No book found with id " + book.getId());
            }
        }
    }

    /** Used by LibraryService to atomically bump the available copy count up or down. */
    public void adjustAvailableCopies(int bookId, int delta) throws SQLException {
        String sql = "UPDATE books SET available_copies = available_copies + ? WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, delta);
            ps.setInt(2, bookId);
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException, RecordNotFoundException {
        String sql = "DELETE FROM books WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new RecordNotFoundException("No book found with id " + id);
            }
        }
    }

    private Book mapRow(ResultSet rs) throws SQLException {
        return new Book(
                rs.getInt("id"),
                rs.getString("title"),
                rs.getString("author"),
                rs.getString("isbn"),
                rs.getString("category"),
                rs.getInt("total_copies"),
                rs.getInt("available_copies")
        );
    }
}
