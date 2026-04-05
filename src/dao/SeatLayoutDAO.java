package dao;

import db.DBConnection;
import model.SeatLayout;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SeatLayoutDAO {

    // ── Get all seats for a bus ───────────────────────────────
    public List<SeatLayout> getSeatsByBus(int busId) {
        List<SeatLayout> list = new ArrayList<>();
        String sql = "SELECT * FROM seat_layouts WHERE bus_id=? ORDER BY seat_number";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, busId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                SeatLayout s = new SeatLayout();
                s.setId        (rs.getInt("id"));
                s.setBusId     (rs.getInt("bus_id"));
                s.setSeatNumber(rs.getString("seat_number"));
                s.setSeatType  (rs.getString("seat_type"));
                list.add(s);
            }
        } catch (SQLException e) {
            System.err.println("[SeatLayoutDAO.getSeatsByBus] " + e.getMessage());
        }
        return list;
    }

    // ── Get seat type for a specific seat ────────────────────
    public String getSeatType(int busId, String seatNumber) {
        String sql = "SELECT seat_type FROM seat_layouts WHERE bus_id=? AND seat_number=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt   (1, busId);
            ps.setString(2, seatNumber);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getString("seat_type");
        } catch (SQLException e) {
            System.err.println("[SeatLayoutDAO.getSeatType] " + e.getMessage());
        }
        return "Standard";
    }

    // ── Save / update entire layout for a bus ────────────────
    /**
     * Replaces the entire seat layout for a bus.
     * Called from Admin seat layout editor when admin saves.
     */
    public boolean saveSeatLayout(int busId, List<SeatLayout> seats) {
        Connection con = null;
        try {
            con = DBConnection.getConnection();
            con.setAutoCommit(false);

            // Delete old layout
            String del = "DELETE FROM seat_layouts WHERE bus_id=?";
            try (PreparedStatement ps = con.prepareStatement(del)) {
                ps.setInt(1, busId); ps.executeUpdate();
            }

            // Insert new layout
            String ins = "INSERT INTO seat_layouts (bus_id, seat_number, seat_type) VALUES (?,?,?)";
            try (PreparedStatement ps = con.prepareStatement(ins)) {
                for (SeatLayout s : seats) {
                    ps.setInt   (1, busId);
                    ps.setString(2, s.getSeatNumber());
                    ps.setString(3, s.getSeatType());
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            con.commit();
            return true;
        } catch (SQLException e) {
            System.err.println("[SeatLayoutDAO.saveSeatLayout] " + e.getMessage());
            try { if (con!=null) con.rollback(); } catch (SQLException ignored) {}
            return false;
        } finally {
            try { if (con!=null) con.setAutoCommit(true); } catch (SQLException ignored) {}
        }
    }

    // ── Update a single seat type ─────────────────────────────
    public boolean updateSeatType(int busId, String seatNumber, String seatType) {
        String sql = "INSERT INTO seat_layouts (bus_id, seat_number, seat_type) VALUES (?,?,?) "
                   + "ON DUPLICATE KEY UPDATE seat_type=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt   (1, busId);
            ps.setString(2, seatNumber);
            ps.setString(3, seatType);
            ps.setString(4, seatType);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[SeatLayoutDAO.updateSeatType] " + e.getMessage());
        }
        return false;
    }
}
