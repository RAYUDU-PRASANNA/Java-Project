package dao;

import db.DBConnection;
import model.Route;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RouteDAO {

    public int addRoute(Route route) {
        String sql = "INSERT INTO routes (source, destination, distance_km, duration_hrs) VALUES (?,?,?,?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, route.getSource());
            ps.setString(2, route.getDestination());
            ps.setDouble(3, route.getDistanceKm());
            ps.setDouble(4, route.getDurationHrs());
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) return keys.getInt(1);
        } catch (SQLException e) {
            System.err.println("[RouteDAO.addRoute] " + e.getMessage());
        }
        return -1;
    }

    public List<Route> getAllRoutes() {
        List<Route> list = new ArrayList<>();
        String sql = "SELECT * FROM routes ORDER BY source";
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("[RouteDAO.getAllRoutes] " + e.getMessage());
        }
        return list;
    }

    public Route getById(int id) {
        String sql = "SELECT * FROM routes WHERE id=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) {
            System.err.println("[RouteDAO.getById] " + e.getMessage());
        }
        return null;
    }

    public boolean updateRoute(Route route) {
        String sql = "UPDATE routes SET source=?, destination=?, distance_km=?, duration_hrs=? WHERE id=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, route.getSource());
            ps.setString(2, route.getDestination());
            ps.setDouble(3, route.getDistanceKm());
            ps.setDouble(4, route.getDurationHrs());
            ps.setInt   (5, route.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[RouteDAO.updateRoute] " + e.getMessage());
        }
        return false;
    }

    public boolean deleteRoute(int id) {
        String sql = "DELETE FROM routes WHERE id=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[RouteDAO.deleteRoute] " + e.getMessage());
        }
        return false;
    }

    /** Returns distinct source cities for the search combo box */
    public List<String> getAllSources() {
        List<String> list = new ArrayList<>();
        String sql = "SELECT DISTINCT source FROM routes ORDER BY source";
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(rs.getString("source"));
        } catch (SQLException e) {
            System.err.println("[RouteDAO.getAllSources] " + e.getMessage());
        }
        return list;
    }

    /** Returns distinct destination cities for a given source */
    public List<String> getDestinationsForSource(String source) {
        List<String> list = new ArrayList<>();
        String sql = "SELECT DISTINCT destination FROM routes WHERE source=? ORDER BY destination";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, source);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(rs.getString("destination"));
        } catch (SQLException e) {
            System.err.println("[RouteDAO.getDestinations] " + e.getMessage());
        }
        return list;
    }

    /**
     * Live autocomplete — returns all city names (source OR destination)
     * that contain the typed text, case-insensitive. Used by UserDashboard.
     */
    public List<String> searchCities(String typed) {
        List<String> list = new ArrayList<>();
        String sql = "SELECT DISTINCT city FROM ("
                   + "  SELECT source AS city FROM routes "
                   + "  UNION "
                   + "  SELECT destination AS city FROM routes"
                   + ") c WHERE LOWER(city) LIKE ? ORDER BY city LIMIT 8";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, "%" + typed.toLowerCase() + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(rs.getString("city"));
        } catch (SQLException e) {
            System.err.println("[RouteDAO.searchCities] " + e.getMessage());
        }
        return list;
    }

    private Route mapRow(ResultSet rs) throws SQLException {
        return new Route(
            rs.getInt("id"),
            rs.getString("source"),
            rs.getString("destination"),
            rs.getDouble("distance_km"),
            rs.getDouble("duration_hrs")
        );
    }
}