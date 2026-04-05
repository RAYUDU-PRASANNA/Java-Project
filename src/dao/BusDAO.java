package dao;

import db.DBConnection;
import model.Bus;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BusDAO {

    // ── CREATE ────────────────────────────────────────────────
    public int addBus(Bus bus) {
        String sql = "INSERT INTO buses (bus_number, bus_name, bus_type, capacity, status) VALUES (?,?,?,?,?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, bus.getBusNumber());
            ps.setString(2, bus.getBusName());
            ps.setString(3, bus.getBusType());
            ps.setInt   (4, bus.getCapacity());
            ps.setString(5, bus.getStatus());
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) return keys.getInt(1);
        } catch (SQLException e) {
            System.err.println("[BusDAO.addBus] " + e.getMessage());
        }
        return -1;
    }

    // ── READ ALL ──────────────────────────────────────────────
    public List<Bus> getAllBuses() {
        List<Bus> list = new ArrayList<>();
        String sql = "SELECT * FROM buses ORDER BY bus_name";
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("[BusDAO.getAllBuses] " + e.getMessage());
        }
        return list;
    }

    // ── READ ACTIVE ───────────────────────────────────────────
    public List<Bus> getActiveBuses() {
        List<Bus> list = new ArrayList<>();
        String sql = "SELECT * FROM buses WHERE status='Active' ORDER BY bus_name";
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("[BusDAO.getActiveBuses] " + e.getMessage());
        }
        return list;
    }

    // ── READ BY ID ────────────────────────────────────────────
    public Bus getById(int id) {
        String sql = "SELECT * FROM buses WHERE id=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) {
            System.err.println("[BusDAO.getById] " + e.getMessage());
        }
        return null;
    }

    // ── UPDATE ────────────────────────────────────────────────
    public boolean updateBus(Bus bus) {
        String sql = "UPDATE buses SET bus_number=?, bus_name=?, bus_type=?, capacity=?, status=? WHERE id=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, bus.getBusNumber());
            ps.setString(2, bus.getBusName());
            ps.setString(3, bus.getBusType());
            ps.setInt   (4, bus.getCapacity());
            ps.setString(5, bus.getStatus());
            ps.setInt   (6, bus.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[BusDAO.updateBus] " + e.getMessage());
        }
        return false;
    }

    // ── DELETE ────────────────────────────────────────────────
    public boolean deleteBus(int id) {
        String sql = "DELETE FROM buses WHERE id=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[BusDAO.deleteBus] " + e.getMessage());
        }
        return false;
    }

    // ── COUNT ACTIVE ──────────────────────────────────────────
    public int countActiveBuses() {
        String sql = "SELECT COUNT(*) FROM buses WHERE status='Active'";
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("[BusDAO.countActiveBuses] " + e.getMessage());
        }
        return 0;
    }

    // ── Map row ───────────────────────────────────────────────
    private Bus mapRow(ResultSet rs) throws SQLException {
        return new Bus(
            rs.getInt("id"),
            rs.getString("bus_number"),
            rs.getString("bus_name"),
            rs.getString("bus_type"),
            rs.getInt("capacity"),
            rs.getString("status")
        );
    }
}
