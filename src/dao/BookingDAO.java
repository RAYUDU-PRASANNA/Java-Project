package dao;

import db.DBConnection;
import model.Booking;
import model.Passenger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BookingDAO {

    // ── CREATE BOOKING ────────────────────────────────────────
    public int createBooking(Booking booking) {
        // Single connection for the entire transaction
        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                // Step 1: Generate booking ref using same connection
                String ref = generateBookingRef(con);

                // Step 2: Insert booking row
                String bkSql = "INSERT INTO bookings "
                             + "(booking_ref, user_id, schedule_id, journey_date, "
                             + " num_passengers, subtotal, gst_amount, total_amount, "
                             + " payment_method, status) "
                             + "VALUES (?,?,?,?,?,?,?,?,?,?)";

                int bookingId = -1;
                try (PreparedStatement ps = con.prepareStatement(
                        bkSql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, ref);
                    ps.setInt   (2, booking.getUserId());
                    ps.setInt   (3, booking.getScheduleId());
                    ps.setDate  (4, booking.getJourneyDate());
                    ps.setInt   (5, booking.getNumPassengers());
                    ps.setDouble(6, booking.getSubtotal());
                    ps.setDouble(7, booking.getGstAmount());
                    ps.setDouble(8, booking.getTotalAmount());
                    ps.setString(9, booking.getPaymentMethod() != null
                                    ? booking.getPaymentMethod() : "Card");
                    ps.setString(10, "Confirmed");
                    ps.executeUpdate();
                    ResultSet keys = ps.getGeneratedKeys();
                    if (keys.next()) bookingId = keys.getInt(1);
                }

                if (bookingId == -1) {
                    con.rollback();
                    System.err.println("[BookingDAO] Could not get generated booking ID.");
                    return -1;
                }

                booking.setId(bookingId);
                booking.setBookingRef(ref);

                // Step 3: Insert passengers
                List<Passenger> passengers = booking.getPassengers();
                if (passengers != null && !passengers.isEmpty()) {
                    String pSql = "INSERT INTO passengers "
                                + "(booking_id, name, age, gender, seat_number) "
                                + "VALUES (?,?,?,?,?)";
                    try (PreparedStatement ps = con.prepareStatement(pSql)) {
                        for (Passenger p : passengers) {
                            ps.setInt   (1, bookingId);
                            ps.setString(2, p.getName()       != null ? p.getName()       : "Passenger");
                            ps.setInt   (3, p.getAge()        >  0    ? p.getAge()        : 25);
                            ps.setString(4, p.getGender()     != null ? p.getGender()     : "Male");
                            ps.setString(5, p.getSeatNumber() != null ? p.getSeatNumber() : "A1");
                            ps.addBatch();
                        }
                        ps.executeBatch();
                    }
                }

                con.commit();
                System.out.println("[BookingDAO] Booking created: " + ref);
                return bookingId;

            } catch (SQLException e) {
                con.rollback();
                System.err.println("[BookingDAO.createBooking] " + e.getMessage());
                e.printStackTrace();
                return -1;
            }
        } catch (SQLException e) {
            System.err.println("[BookingDAO.createBooking] Connection error: " + e.getMessage());
            return -1;
        }
    }

    // ── GET BOOKINGS FOR A USER ───────────────────────────────
    public List<Booking> getBookingsByUser(int userId) {
        List<Booking> list = new ArrayList<>();
        String sql = "SELECT bk.*, u.full_name AS passenger_name, u.email, u.phone, "
                   + "b.bus_name, r.source, r.destination, "
                   + "s.departure_time, s.arrival_time "
                   + "FROM bookings bk "
                   + "JOIN users     u ON bk.user_id     = u.id "
                   + "JOIN schedules s ON bk.schedule_id = s.id "
                   + "JOIN buses     b ON s.bus_id        = b.id "
                   + "JOIN routes    r ON s.route_id      = r.id "
                   + "WHERE bk.user_id = ? ORDER BY bk.booked_at DESC";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("[BookingDAO.getBookingsByUser] " + e.getMessage());
        }
        return list;
    }

    // ── GET ALL BOOKINGS (Admin) ──────────────────────────────
    public List<Booking> getAllBookings() {
        List<Booking> list = new ArrayList<>();
        String sql = "SELECT bk.*, u.full_name AS passenger_name, u.email, u.phone, "
                   + "b.bus_name, r.source, r.destination, "
                   + "s.departure_time, s.arrival_time "
                   + "FROM bookings bk "
                   + "JOIN users     u ON bk.user_id     = u.id "
                   + "JOIN schedules s ON bk.schedule_id = s.id "
                   + "JOIN buses     b ON s.bus_id        = b.id "
                   + "JOIN routes    r ON s.route_id      = r.id "
                   + "ORDER BY bk.booked_at DESC";
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("[BookingDAO.getAllBookings] " + e.getMessage());
        }
        return list;
    }

    // ── GET BY ID ─────────────────────────────────────────────
    public Booking getById(int id) {
        String sql = "SELECT bk.*, u.full_name AS passenger_name, u.email, u.phone, "
                   + "b.bus_name, r.source, r.destination, s.departure_time, s.arrival_time "
                   + "FROM bookings bk "
                   + "JOIN users u ON bk.user_id=u.id "
                   + "JOIN schedules s ON bk.schedule_id=s.id "
                   + "JOIN buses b ON s.bus_id=b.id "
                   + "JOIN routes r ON s.route_id=r.id "
                   + "WHERE bk.id=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) {
            System.err.println("[BookingDAO.getById] " + e.getMessage());
        }
        return null;
    }

    // ── CANCEL BOOKING ────────────────────────────────────────
    public boolean cancelBooking(int bookingId, String reason) {
        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                double totalAmount = 0;
                try (PreparedStatement ps = con.prepareStatement(
                        "SELECT total_amount, status FROM bookings WHERE id=?")) {
                    ps.setInt(1, bookingId);
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) {
                        if (!"Confirmed".equals(rs.getString("status"))) {
                            con.rollback(); return false;
                        }
                        totalAmount = rs.getDouble("total_amount");
                    }
                }
                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE bookings SET status='Cancelled' WHERE id=?")) {
                    ps.setInt(1, bookingId); ps.executeUpdate();
                }
                try (PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO cancellations (booking_id, reason, refund_amount) VALUES (?,?,?)")) {
                    ps.setInt   (1, bookingId);
                    ps.setString(2, reason != null ? reason : "Cancelled by passenger");
                    ps.setDouble(3, totalAmount * 0.80);
                    ps.executeUpdate();
                }
                con.commit();
                return true;
            } catch (SQLException e) {
                con.rollback();
                System.err.println("[BookingDAO.cancelBooking] " + e.getMessage());
                return false;
            }
        } catch (SQLException e) {
            System.err.println("[BookingDAO.cancelBooking] Connection error: " + e.getMessage());
            return false;
        }
    }

    // ── BOOKED SEATS ──────────────────────────────────────────
    public List<String> getBookedSeats(int scheduleId, java.sql.Date journeyDate) {
        List<String> seats = new ArrayList<>();
        String sql = "SELECT p.seat_number FROM passengers p "
                   + "JOIN bookings bk ON p.booking_id = bk.id "
                   + "WHERE bk.schedule_id=? AND bk.journey_date=? "
                   + "AND bk.status IN ('Confirmed','Pending')";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt (1, scheduleId);
            ps.setDate(2, journeyDate);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) seats.add(rs.getString("seat_number"));
        } catch (SQLException e) {
            System.err.println("[BookingDAO.getBookedSeats] " + e.getMessage());
        }
        return seats;
    }

    // ── TODAY ANALYTICS ───────────────────────────────────────
    public int[] getTodayAnalytics() {
        int[] data = {0, 0, 0, 0};
        String sql = "SELECT COUNT(*) AS total_bookings, "
                   + "COALESCE(SUM(total_amount),0) AS total_revenue, "
                   + "SUM(CASE WHEN status='Cancelled' THEN 1 ELSE 0 END) AS cancellations "
                   + "FROM bookings WHERE DATE(booked_at)=CURDATE()";
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                data[0] = rs.getInt("total_bookings");
                data[1] = (int) rs.getDouble("total_revenue");
                data[2] = rs.getInt("cancellations");
            }
        } catch (SQLException e) {
            System.err.println("[BookingDAO.getTodayAnalytics] " + e.getMessage());
        }
        data[3] = new BusDAO().countActiveBuses();
        return data;
    }

    // ── WEEKLY BOOKINGS ───────────────────────────────────────
    public int[] getWeeklyBookings() {
        int[] weekly = new int[7];
        String sql = "SELECT DATE(booked_at) AS bdate, COUNT(*) AS cnt "
                   + "FROM bookings "
                   + "WHERE booked_at >= DATE_SUB(CURDATE(), INTERVAL 6 DAY) "
                   + "GROUP BY DATE(booked_at)";
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                java.sql.Date d = rs.getDate("bdate");
                long diff = (System.currentTimeMillis() - d.getTime()) / (1000L * 60 * 60 * 24);
                int idx = 6 - (int) diff;
                if (idx >= 0 && idx < 7) weekly[idx] = rs.getInt("cnt");
            }
        } catch (SQLException e) {
            System.err.println("[BookingDAO.getWeeklyBookings] " + e.getMessage());
        }
        return weekly;
    }

    // ── generateBookingRef (uses existing connection) ─────────
    private String generateBookingRef(Connection con) {
        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(
                     "SELECT COALESCE(MAX(id),0)+1 AS next_id FROM bookings")) {
            if (rs.next()) return String.format("BK-%05d", rs.getInt("next_id"));
        } catch (SQLException e) {
            System.err.println("[BookingDAO.generateBookingRef] " + e.getMessage());
        }
        return "BK-" + System.currentTimeMillis();
    }

    // ── Map ResultSet → Booking ───────────────────────────────
    private Booking mapRow(ResultSet rs) throws SQLException {
        Booking b = new Booking();
        b.setId            (rs.getInt("id"));
        b.setBookingRef    (rs.getString("booking_ref"));
        b.setUserId        (rs.getInt("user_id"));
        b.setScheduleId    (rs.getInt("schedule_id"));
        b.setJourneyDate   (rs.getDate("journey_date"));
        b.setNumPassengers (rs.getInt("num_passengers"));
        b.setSubtotal      (rs.getDouble("subtotal"));
        b.setGstAmount     (rs.getDouble("gst_amount"));
        b.setTotalAmount   (rs.getDouble("total_amount"));
        b.setPaymentMethod (rs.getString("payment_method"));
        b.setStatus        (rs.getString("status"));
        b.setBookedAt      (rs.getTimestamp("booked_at"));
        b.setPassengerName (rs.getString("passenger_name"));
        b.setEmail         (rs.getString("email"));
        b.setPhone         (rs.getString("phone"));
        b.setBusName       (rs.getString("bus_name"));
        b.setSource        (rs.getString("source"));
        b.setDestination   (rs.getString("destination"));
        b.setDepartureTime (rs.getString("departure_time"));
        b.setArrivalTime   (rs.getString("arrival_time"));
        return b;
    }
}