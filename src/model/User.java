package model;

import java.sql.Timestamp;

public class User {
    private int id;
    private String fullName;
    private String email;
    private String phone;
    private String password;
    private String gender;
    private String dob;
    private String emergencyContact;
    private Timestamp createdAt;
    private boolean active;

    public User() {}

    public User(int id, String fullName, String email, String phone,
                String gender, String dob, boolean active) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.gender = gender;
        this.dob = dob;
        this.active = active;
    }

    // ── Getters & Setters ────────────────────────────────────
    public int getId()                      { return id; }
    public void setId(int id)               { this.id = id; }

    public String getFullName()             { return fullName; }
    public void setFullName(String n)       { this.fullName = n; }

    public String getEmail()                { return email; }
    public void setEmail(String e)          { this.email = e; }

    public String getPhone()                { return phone; }
    public void setPhone(String p)          { this.phone = p; }

    public String getPassword()             { return password; }
    public void setPassword(String p)       { this.password = p; }

    public String getGender()               { return gender; }
    public void setGender(String g)         { this.gender = g; }

    public String getDob()                  { return dob; }
    public void setDob(String d)            { this.dob = d; }

    public String getEmergencyContact()     { return emergencyContact; }
    public void setEmergencyContact(String e){ this.emergencyContact = e; }

    public Timestamp getCreatedAt()         { return createdAt; }
    public void setCreatedAt(Timestamp t)   { this.createdAt = t; }

    public boolean isActive()               { return active; }
    public void setActive(boolean a)        { this.active = a; }

    @Override public String toString() {
        return "User{id=" + id + ", name=" + fullName + ", email=" + email + "}";
    }
}
