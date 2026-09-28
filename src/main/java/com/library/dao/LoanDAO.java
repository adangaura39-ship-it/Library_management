package com.library.dao;

import com.library.exception.RecordNotFoundException;
import com.library.model.Loan;
import com.library.util.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class LoanDAO {

    public Loan create(Loan loan) throws SQLException {
        String sql = "INSERT INTO loans (book_id, member_id, issue_date, due_date, return_date) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, loan.getBookId());
            ps.setInt(2, loan.getMemberId());
            ps.setString(3, loan.getIssueDate().toString());
            ps.setString(4, loan.getDueDate().toString());
            ps.setString(5, loan.getReturnDate() == null ? null : loan.getReturnDate().toString());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    loan.setId(keys.getInt(1));
                }
            }
            return loan;
        }
    }

    public Loan findById(int id) throws SQLException, RecordNotFoundException {
        String sql = "SELECT l.*, b.title AS book_title, m.name AS member_name " +
                "FROM loans l JOIN books b ON l.book_id = b.id JOIN members m ON l.member_id = m.id " +
                "WHERE l.id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        throw new RecordNotFoundException("No loan found with id " + id);
    }

    /** All loans currently out (not yet returned), most recently issued first. */
    public List<Loan> findActiveLoans() throws SQLException {
        String sql = "SELECT l.*, b.title AS book_title, m.name AS member_name " +
                "FROM loans l JOIN books b ON l.book_id = b.id JOIN members m ON l.member_id = m.id " +
                "WHERE l.return_date IS NULL ORDER BY l.due_date";
        return runQuery(sql);
    }

    /** Loans that are active AND past their due date. */
    public List<Loan> findOverdueLoans() throws SQLException {
        String sql = "SELECT l.*, b.title AS book_title, m.name AS member_name " +
                "FROM loans l JOIN books b ON l.book_id = b.id JOIN members m ON l.member_id = m.id " +
                "WHERE l.return_date IS NULL AND l.due_date < ? ORDER BY l.due_date";
        List<Loan> results = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, LocalDate.now().toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(mapRow(rs));
                }
            }
        }
        return results;
    }

    public List<Loan> findByMember(int memberId) throws SQLException {
        String sql = "SELECT l.*, b.title AS book_title, m.name AS member_name " +
                "FROM loans l JOIN books b ON l.book_id = b.id JOIN members m ON l.member_id = m.id " +
                "WHERE l.member_id = ? ORDER BY l.issue_date DESC";
        List<Loan> results = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, memberId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(mapRow(rs));
                }
            }
        }
        return results;
    }

    /** True if this member currently has this exact book out and unreturned. */
    public boolean hasActiveLoan(int bookId, int memberId) throws SQLException {
        String sql = "SELECT 1 FROM loans WHERE book_id = ? AND member_id = ? AND return_date IS NULL";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookId);
            ps.setInt(2, memberId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public void markReturned(int loanId, LocalDate returnDate) throws SQLException, RecordNotFoundException {
        String sql = "UPDATE loans SET return_date = ? WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, returnDate.toString());
            ps.setInt(2, loanId);
            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new RecordNotFoundException("No loan found with id " + loanId);
            }
        }
    }

    private List<Loan> runQuery(String sql) throws SQLException {
        List<Loan> results = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                results.add(mapRow(rs));
            }
        }
        return results;
    }

    private Loan mapRow(ResultSet rs) throws SQLException {
        String returnDateStr = rs.getString("return_date");
        Loan loan = new Loan(
                rs.getInt("id"),
                rs.getInt("book_id"),
                rs.getInt("member_id"),
                LocalDate.parse(rs.getString("issue_date")),
                LocalDate.parse(rs.getString("due_date")),
                returnDateStr == null ? null : LocalDate.parse(returnDateStr)
        );
        loan.setBookTitle(rs.getString("book_title"));
        loan.setMemberName(rs.getString("member_name"));
        return loan;
    }
}
