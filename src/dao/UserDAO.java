package dao;

import db.DBConnection;
import model.User;
import util.PasswordUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * UserDAO — All database operations for users.
 */
public class UserDAO {

    // ── REGISTER ─────────────────────────────────────────────

    /**
     * Registers a new user. Returns the generated user ID, or -1 on failure.
     */
    public int register(User user) {
        String sql = "INSERT INTO users (full_name, email, phone, password, gender, dob, emergency_contact) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, user.getFullName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPhone());
            ps.setString(4, PasswordUtil.hash(user.getPassword())); // hash before storing
            ps.setString(5, user.getGender());
            ps.setString(6, user.getDob());
            ps.setString(7, user.getEmergencyContact());

            int rows = ps.executeUpdate();
            if (rows > 0) {
                ResultSet keys = ps.getGeneratedKeys();
                if (keys.next()) return keys.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("[UserDAO.register] " + e.getMessage());
        }
        return -1;
    }

    // ── LOGIN ─────────────────────────────────────────────────

    /**
     * Authenticates a user by email + password.
     * Returns the User object if successful, null otherwise.
     */
    public User login(String email, String plainPassword) {
        String sql = "SELECT * FROM users WHERE email = ? AND is_active = TRUE";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String storedHash = rs.getString("password");
                if (PasswordUtil.verify(plainPassword, storedHash)) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("[UserDAO.login] " + e.getMessage());
        }
        return null; // login failed
    }

    // ── GET BY ID ─────────────────────────────────────────────

    public User getById(int id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) {
            System.err.println("[UserDAO.getById] " + e.getMessage());
        }
        return null;
    }

    // ── CHECK EMAIL EXISTS ────────────────────────────────────

    public boolean emailExists(String email) {
        String sql = "SELECT COUNT(*) FROM users WHERE email = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (SQLException e) {
            System.err.println("[UserDAO.emailExists] " + e.getMessage());
        }
        return false;
    }

    // ── UPDATE PROFILE ────────────────────────────────────────

    /**
     * Updates a user's profile fields (does NOT update password here).
     */
    public boolean updateProfile(User user) {
        String sql = "UPDATE users SET full_name=?, phone=?, gender=?, dob=?, emergency_contact=? WHERE id=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, user.getFullName());
            ps.setString(2, user.getPhone());
            ps.setString(3, user.getGender());
            if (user.getDob() == null || user.getDob().isEmpty())
                ps.setNull(4, java.sql.Types.DATE);
            else
                ps.setString(4, user.getDob());
            if (user.getEmergencyContact() == null || user.getEmergencyContact().isEmpty())
                ps.setNull(5, java.sql.Types.VARCHAR);
            else
                ps.setString(5, user.getEmergencyContact());
            ps.setInt(6, user.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[UserDAO.updateProfile] " + e.getMessage());
        }
        return false;
    }

    // ── CHANGE PASSWORD ───────────────────────────────────────

    public boolean changePassword(int userId, String oldPass, String newPass) {
        User u = getById(userId);
        if (u == null) return false;
        // Verify old password first
        String sql = "SELECT password FROM users WHERE id=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                if (!PasswordUtil.verify(oldPass, rs.getString("password"))) return false;
            }
        } catch (SQLException e) {
            System.err.println("[UserDAO.changePassword] " + e.getMessage());
            return false;
        }
        // Update with new hash
        String upd = "UPDATE users SET password=? WHERE id=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(upd)) {
            ps.setString(1, PasswordUtil.hash(newPass));
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[UserDAO.changePassword-update] " + e.getMessage());
        }
        return false;
    }

    // ── GET USER BY EMAIL ─────────────────────────────────────

    public User getByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) {
            System.err.println("[UserDAO.getByEmail] " + e.getMessage());
        }
        return null;
    }

    // ── RESET PASSWORD (Forgot Password flow) ─────────────────

    public boolean resetPassword(String email, String newPassword) {
        String sql = "UPDATE users SET password=? WHERE email=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, util.PasswordUtil.hash(newPassword));
            ps.setString(2, email);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[UserDAO.resetPassword] " + e.getMessage());
        }
        return false;
    }

    // ── GET ALL USERS (Admin) ─────────────────────────────────

    public List<User> getAllUsers() {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY created_at DESC";
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("[UserDAO.getAllUsers] " + e.getMessage());
        }
        return list;
    }

    // ── ADMIN LOGIN ───────────────────────────────────────────

    /**
     * Authenticates an admin by username + password.
     * Returns admin's full name on success, null on failure.
     */
    public String adminLogin(String username, String password) {
        // Default hard-coded admin check (no DB hash for demo simplicity)
        // In production replace with: SELECT + PasswordUtil.verify()
        String sql = "SELECT * FROM admins WHERE username = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String stored = rs.getString("password");
                // Support both plain-text seed ("admin123") and hashed
                if (password.equals("admin123") || PasswordUtil.verify(password, stored)) {
                    return rs.getString("full_name");
                }
            }
        } catch (SQLException e) {
            System.err.println("[UserDAO.adminLogin] " + e.getMessage());
        }
        return null;
    }

    // ── Map ResultSet row → User ──────────────────────────────

    private User mapRow(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getInt("id"));
        u.setFullName(rs.getString("full_name"));
        u.setEmail(rs.getString("email"));
        u.setPhone(rs.getString("phone"));
        u.setPassword(rs.getString("password"));
        u.setGender(rs.getString("gender"));
        u.setDob(rs.getString("dob"));
        u.setEmergencyContact(rs.getString("emergency_contact"));
        u.setCreatedAt(rs.getTimestamp("created_at"));
        u.setActive(rs.getBoolean("is_active"));
        return u;
    }
}