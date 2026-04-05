package model;

public class Schedule {
    private int id;
    private int busId;
    private int routeId;
    private String departureTime;
    private String arrivalTime;
    private String runDays;
    private double price;
    private boolean active;

    // Joined fields from view
    private String busNumber;
    private String busName;
    private String busType;
    private int capacity;
    private String source;
    private String destination;
    private int availableSeats;

    public Schedule() {}

    // ── Getters & Setters ─────────────────────────────────────
    public int getId()                      { return id; }
    public void setId(int id)               { this.id = id; }
    public int getBusId()                   { return busId; }
    public void setBusId(int b)             { this.busId = b; }
    public int getRouteId()                 { return routeId; }
    public void setRouteId(int r)           { this.routeId = r; }
    public String getDepartureTime()        { return departureTime; }
    public void setDepartureTime(String d)  { this.departureTime = d; }
    public String getArrivalTime()          { return arrivalTime; }
    public void setArrivalTime(String a)    { this.arrivalTime = a; }
    public String getRunDays()              { return runDays; }
    public void setRunDays(String r)        { this.runDays = r; }
    public double getPrice()                { return price; }
    public void setPrice(double p)          { this.price = p; }
    public boolean isActive()               { return active; }
    public void setActive(boolean a)        { this.active = a; }

    public String getBusNumber()            { return busNumber; }
    public void setBusNumber(String b)      { this.busNumber = b; }
    public String getBusName()              { return busName; }
    public void setBusName(String b)        { this.busName = b; }
    public String getBusType()              { return busType; }
    public void setBusType(String b)        { this.busType = b; }
    public int getCapacity()                { return capacity; }
    public void setCapacity(int c)          { this.capacity = c; }
    public String getSource()               { return source; }
    public void setSource(String s)         { this.source = s; }
    public String getDestination()          { return destination; }
    public void setDestination(String d)    { this.destination = d; }
    public int getAvailableSeats()          { return availableSeats; }
    public void setAvailableSeats(int a)    { this.availableSeats = a; }

    /** Returns formatted price string like ₹850 */
    public String getPriceFormatted() {
        return "₹" + String.format("%,.0f", price);
    }

    @Override public String toString() {
        return busName + " | " + source + "→" + destination
             + " | " + departureTime + " | " + getPriceFormatted();
    }
}
