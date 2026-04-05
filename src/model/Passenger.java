package model;

public class Passenger {
    private int id;
    private int bookingId;
    private String name;
    private int age;
    private String gender;
    private String seatNumber;

    public Passenger() {}

    public Passenger(String name, int age, String gender, String seatNumber) {
        this.name = name;
        this.age = age;
        this.gender = gender;
        this.seatNumber = seatNumber;
    }

    public int getId()                  { return id; }
    public void setId(int id)           { this.id = id; }
    public int getBookingId()           { return bookingId; }
    public void setBookingId(int b)     { this.bookingId = b; }
    public String getName()             { return name; }
    public void setName(String n)       { this.name = n; }
    public int getAge()                 { return age; }
    public void setAge(int a)           { this.age = a; }
    public String getGender()           { return gender; }
    public void setGender(String g)     { this.gender = g; }
    public String getSeatNumber()       { return seatNumber; }
    public void setSeatNumber(String s) { this.seatNumber = s; }

    @Override public String toString() {
        return name + " (Age: " + age + ", " + gender + ", Seat: " + seatNumber + ")";
    }
}
