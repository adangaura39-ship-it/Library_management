package com.library.dao;

import com.library.exception.RecordNotFoundException;
import com.library.model.Member;
import com.library.util.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class MemberDAO {

    public Member create(Member member) throws SQLException {
        String sql = "INSERT INTO members (name, email, phone, join_date) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, member.getName());
            ps.setString(2, member.getEmail());
            ps.setString(3, member.getPhone());
            ps.setString(4, member.getJoinDate().toString());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    member.setId(keys.getInt(1));
                }
            }
            return member;
        }
    }

    public Member findById(int id) throws SQLException, RecordNotFoundException {
        String sql = "SELECT * FROM members WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        throw new RecordNotFoundException("No member found with id " + id);
    }

    public List<Member> findAll() throws SQLException {
        String sql = "SELECT * FROM members ORDER BY name";
        List<Member> members = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                members.add(mapRow(rs));
            }
        }
        return members;
    }

    public List<Member> search(String keyword) throws SQLException {
        String sql = "SELECT * FROM members WHERE LOWER(name) LIKE ? OR LOWER(email) LIKE ? ORDER BY name";
        List<Member> results = new ArrayList<>();
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

    public void update(Member member) throws SQLException, RecordNotFoundException {
        String sql = "UPDATE members SET name = ?, email = ?, phone = ? WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, member.getName());
            ps.setString(2, member.getEmail());
            ps.setString(3, member.getPhone());
            ps.setInt(4, member.getId());
            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new RecordNotFoundException("No member found with id " + member.getId());
            }
        }
    }

    public void delete(int id) throws SQLException, RecordNotFoundException {
        String sql = "DELETE FROM members WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new RecordNotFoundException("No member found with id " + id);
            }
        }
    }

    private Member mapRow(ResultSet rs) throws SQLException {
        return new Member(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("email"),
                rs.getString("phone"),
                LocalDate.parse(rs.getString("join_date"))
        );
    }
}
