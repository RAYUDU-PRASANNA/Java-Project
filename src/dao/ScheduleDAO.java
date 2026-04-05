package dao;

import db.DBConnection;
import model.Schedule;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// ══════════════════════════════════════════════════════════════
//  ScheduleDAO
// ══════════════════════════════════════════════════════════════
public class ScheduleDAO {

    // ── Search schedules by source, destination, date, time pref ──
    /**
     * Returns available schedules for a route on a given date.
     * timePref: "AM" = departure before 12:00, "PM" = 12:00 onwards, "" = any
     */
    public List<Schedule> searchSchedules(String source, String destination,
                                          java.sql.Date journeyDate, String timePref) {
        List<Schedule> list = new ArrayList<>();

        // Build the time filter
        String timeClause = "";
        if ("AM".equalsIgnoreCase(timePref)) {
            timeClause = " AND s.departure_time < '12:00:00'";
        } else if ("PM".equalsIgnoreCase(timePref)) {
            timeClause = " AND s.departure_time >= '12:00:00'";
        }

        String sql = "SELECT s.*, b.bus_number, b.bus_name, b.bus_type, b.capacity, "
                   + "       r.source, r.destination, "
                   + "       (b.capacity - COALESCE(("
                   + "           SELECT SUM(bk.num_passengers) "
                   + "           FROM bookings bk "
                   + "           WHERE bk.schedule_id = s.id "
                   + "             AND bk.journey_date = ? "
                   + "             AND bk.status IN ('Confirmed','Pending')"
                   + "       ),0)) AS available_seats "
                   + "FROM schedules s "
                   + "JOIN buses  b ON s.bus_id   = b.id "
                   + "JOIN routes r ON s.route_id = r.id "
                   + "WHERE r.source = ? AND r.destination = ? "
                   + "  AND s.is_active = TRUE "
                   + "  AND b.status = 'Active' "
                   + timeClause
                   + " ORDER BY s.departure_time";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate  (1, journeyDate);
            ps.setString(2, source);
            ps.setString(3, destination);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("[ScheduleDAO.searchSchedules] " + e.getMessage());
        }
        return list;
    }

    // ── CRUD ──────────────────────────────────────────────────

    public int addSchedule(Schedule sch) {
        String sql = "INSERT INTO schedules (bus_id,route_id,departure_time,arrival_time,run_days,price) VALUES (?,?,?,?,?,?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt   (1, sch.getBusId());
            ps.setInt   (2, sch.getRouteId());
            ps.setString(3, sch.getDepartureTime());
            ps.setString(4, sch.getArrivalTime());
            ps.setString(5, sch.getRunDays());
            ps.setDouble(6, sch.getPrice());
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) return keys.getInt(1);
        } catch (SQLException e) {
            System.err.println("[ScheduleDAO.addSchedule] " + e.getMessage());
        }
        return -1;
    }

    public List<Schedule> getAllSchedules() {
        List<Schedule> list = new ArrayList<>();
        String sql = "SELECT s.*, b.bus_number, b.bus_name, b.bus_type, b.capacity, "
                   + "r.source, r.destination, "
                   + "(b.capacity - COALESCE(("
                   + "  SELECT SUM(bk.num_passengers) FROM bookings bk "
                   + "  WHERE bk.schedule_id = s.id AND bk.status = 'Confirmed'"
                   + "), 0)) AS available_seats "
                   + "FROM schedules s "
                   + "JOIN buses  b ON s.bus_id   = b.id "
                   + "JOIN routes r ON s.route_id = r.id "
                   + "WHERE s.is_active = true "
                   + "ORDER BY s.departure_time";
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("[ScheduleDAO.getAllSchedules] " + e.getMessage());
        }
        return list;
    }

    public Schedule getById(int id) {
        String sql = "SELECT s.*, b.bus_number, b.bus_name, b.bus_type, b.capacity, "
                   + "r.source, r.destination, 0 AS available_seats "
                   + "FROM schedules s "
                   + "JOIN buses b ON s.bus_id=b.id "
                   + "JOIN routes r ON s.route_id=r.id WHERE s.id=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) {
            System.err.println("[ScheduleDAO.getById] " + e.getMessage());
        }
        return null;
    }

    public boolean updateSchedule(Schedule sch) {
        String sql = "UPDATE schedules SET bus_id=?,route_id=?,departure_time=?,arrival_time=?,run_days=?,price=?,is_active=? WHERE id=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt    (1, sch.getBusId());
            ps.setInt    (2, sch.getRouteId());
            ps.setString (3, sch.getDepartureTime());
            ps.setString (4, sch.getArrivalTime());
            ps.setString (5, sch.getRunDays());
            ps.setDouble (6, sch.getPrice());
            ps.setBoolean(7, sch.isActive());
            ps.setInt    (8, sch.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[ScheduleDAO.updateSchedule] " + e.getMessage());
        }
        return false;
    }

    public boolean deleteSchedule(int id) {
        String sql = "DELETE FROM schedules WHERE id=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[ScheduleDAO.deleteSchedule] " + e.getMessage());
        }
        return false;
    }

    // ── Map row ───────────────────────────────────────────────
    private Schedule mapRow(ResultSet rs) throws SQLException {
        Schedule s = new Schedule();
        s.setId            (rs.getInt("id"));
        s.setBusId         (rs.getInt("bus_id"));
        s.setRouteId       (rs.getInt("route_id"));
        s.setDepartureTime (rs.getString("departure_time"));
        s.setArrivalTime   (rs.getString("arrival_time"));
        s.setRunDays       (rs.getString("run_days"));
        s.setPrice         (rs.getDouble("price"));
        s.setActive        (rs.getBoolean("is_active"));
        s.setBusNumber     (rs.getString("bus_number"));
        s.setBusName       (rs.getString("bus_name"));
        s.setBusType       (rs.getString("bus_type"));
        s.setCapacity      (rs.getInt("capacity"));
        s.setSource        (rs.getString("source"));
        s.setDestination   (rs.getString("destination"));
        s.setAvailableSeats(rs.getInt("available_seats"));
        return s;
    }

    // ── Get all unique cities from routes table ────────────────
    public List<String> getAllCities() {
        List<String> cities = new ArrayList<>();
        String sql = "SELECT DISTINCT city FROM ("
                   + "  SELECT source AS city FROM routes "
                   + "  UNION "
                   + "  SELECT destination AS city FROM routes"
                   + ") c ORDER BY city";
        try (Connection con = DBConnection.getConnection();
             java.sql.Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) cities.add(rs.getString("city"));
        } catch (SQLException e) {
            System.err.println("[ScheduleDAO.getAllCities] " + e.getMessage());
        }
        return cities;
    }
}