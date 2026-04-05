package model;

public class Route {
    private int id;
    private String source;
    private String destination;
    private double distanceKm;
    private double durationHrs;

    public Route() {}
    public Route(int id, String source, String destination,
                 double distanceKm, double durationHrs) {
        this.id = id; this.source = source; this.destination = destination;
        this.distanceKm = distanceKm; this.durationHrs = durationHrs;
    }

    public int getId()                  { return id; }
    public void setId(int id)           { this.id = id; }
    public String getSource()           { return source; }
    public void setSource(String s)     { this.source = s; }
    public String getDestination()      { return destination; }
    public void setDestination(String d){ this.destination = d; }
    public double getDistanceKm()       { return distanceKm; }
    public void setDistanceKm(double d) { this.distanceKm = d; }
    public double getDurationHrs()      { return durationHrs; }
    public void setDurationHrs(double d){ this.durationHrs = d; }

    @Override public String toString() { return source + " → " + destination; }
}
