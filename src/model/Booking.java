package model;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.List;
import java.util.ArrayList;

// ── Booking ───────────────────────────────────────────────────
public class Booking {
    private int id;
    private String bookingRef;
    private int userId;
    private int scheduleId;
    private Date journeyDate;
    private int numPassengers;
    private double subtotal;
    private double gstAmount;
    private double totalAmount;
    private String paymentMethod;
    private String status;
    private Timestamp bookedAt;
    private List<Passenger> passengers = new ArrayList<>();

    // Joined fields (for display)
    private String passengerName;
    private String email;
    private String phone;
    private String busName;
    private String source;
    private String destination;
    private String departureTime;
    private String arrivalTime;

    public Booking() {}

    // ── Getters & Setters ─────────────────────────────────────
    public int getId()                      { return id; }
    public void setId(int id)               { this.id = id; }
    public String getBookingRef()           { return bookingRef; }
    public void setBookingRef(String r)     { this.bookingRef = r; }
    public int getUserId()                  { return userId; }
    public void setUserId(int u)            { this.userId = u; }
    public int getScheduleId()              { return scheduleId; }
    public void setScheduleId(int s)        { this.scheduleId = s; }
    public Date getJourneyDate()            { return journeyDate; }
    public void setJourneyDate(Date d)      { this.journeyDate = d; }
    public int getNumPassengers()           { return numPassengers; }
    public void setNumPassengers(int n)     { this.numPassengers = n; }
    public double getSubtotal()             { return subtotal; }
    public void setSubtotal(double s)       { this.subtotal = s; }
    public double getGstAmount()            { return gstAmount; }
    public void setGstAmount(double g)      { this.gstAmount = g; }
    public double getTotalAmount()          { return totalAmount; }
    public void setTotalAmount(double t)    { this.totalAmount = t; }
    public String getPaymentMethod()        { return paymentMethod; }
    public void setPaymentMethod(String p)  { this.paymentMethod = p; }
    public String getStatus()               { return status; }
    public void setStatus(String s)         { this.status = s; }
    public Timestamp getBookedAt()          { return bookedAt; }
    public void setBookedAt(Timestamp t)    { this.bookedAt = t; }
    public List<Passenger> getPassengers()  { return passengers; }
    public void setPassengers(List<Passenger> p) { this.passengers = p; }
    public void addPassenger(Passenger p)   { this.passengers.add(p); }

    public String getPassengerName()        { return passengerName; }
    public void setPassengerName(String n)  { this.passengerName = n; }
    public String getEmail()                { return email; }
    public void setEmail(String e)          { this.email = e; }
    public String getPhone()                { return phone; }
    public void setPhone(String p)          { this.phone = p; }
    public String getBusName()              { return busName; }
    public void setBusName(String b)        { this.busName = b; }
    public String getSource()               { return source; }
    public void setSource(String s)         { this.source = s; }
    public String getDestination()          { return destination; }
    public void setDestination(String d)    { this.destination = d; }
    public String getDepartureTime()        { return departureTime; }
    public void setDepartureTime(String d)  { this.departureTime = d; }
    public String getArrivalTime()          { return arrivalTime; }
    public void setArrivalTime(String a)    { this.arrivalTime = a; }

    public String getTotalFormatted()       { return "₹" + String.format("%,.0f", totalAmount); }
}
