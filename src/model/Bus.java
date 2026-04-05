package model;

// ── Bus ───────────────────────────────────────────────────────
public class Bus {
    private int id;
    private String busNumber;
    private String busName;
    private String busType;
    private int capacity;
    private String status;

    public Bus() {}
    public Bus(int id, String busNumber, String busName,
               String busType, int capacity, String status) {
        this.id = id; this.busNumber = busNumber; this.busName = busName;
        this.busType = busType; this.capacity = capacity; this.status = status;
    }

    public int getId()              { return id; }
    public void setId(int id)       { this.id = id; }
    public String getBusNumber()    { return busNumber; }
    public void setBusNumber(String b) { this.busNumber = b; }
    public String getBusName()      { return busName; }
    public void setBusName(String b){ this.busName = b; }
    public String getBusType()      { return busType; }
    public void setBusType(String b){ this.busType = b; }
    public int getCapacity()        { return capacity; }
    public void setCapacity(int c)  { this.capacity = c; }
    public String getStatus()       { return status; }
    public void setStatus(String s) { this.status = s; }

    @Override public String toString() { return busNumber + " — " + busName; }
}
