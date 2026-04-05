package model;

public class SeatLayout {
    private int id;
    private int busId;
    private String seatNumber;
    private String seatType; // Standard / Window / Elder / Disabled

    public SeatLayout() {}

    public SeatLayout(int busId, String seatNumber, String seatType) {
        this.busId = busId;
        this.seatNumber = seatNumber;
        this.seatType = seatType;
    }

    public int getId()                  { return id; }
    public void setId(int id)           { this.id = id; }
    public int getBusId()               { return busId; }
    public void setBusId(int b)         { this.busId = b; }
    public String getSeatNumber()       { return seatNumber; }
    public void setSeatNumber(String s) { this.seatNumber = s; }
    public String getSeatType()         { return seatType; }
    public void setSeatType(String s)   { this.seatType = s; }

    @Override public String toString() {
        return seatNumber + " [" + seatType + "]";
    }
}
