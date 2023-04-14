/*
Kevin Baron
6/25/13
CXCC 2013 Triangles Project
Waypoint
*/

public class Waypoint {
	
	private final double EARTH_MAJOR_AXIS = 3963.1905919;
	private final double EARTH_MINOR_AXIS = 3949.90276423189;
	
	public String abbrev;
	public double latitude;
	public double longitude;
	public double radius;
	public String name;
	
	public Waypoint(String abbrev, double latitude, double longitude, double radius, String name) {
		this.abbrev = abbrev;
		this.latitude = latitude;
		this.longitude = longitude;
		this.radius = radius;
		this.name = name;
	}//eo Waypoint constructor
	
	public double distanceTo(Waypoint other) {
		double lat1 = Math.toRadians(latitude);
		double lon1 = Math.toRadians(longitude);
		double lat2 = Math.toRadians(other.latitude);
		double lon2 = Math.toRadians(other.longitude);
		double dLat = lat2 - lat1;
		double dLon = lon2 - lon1;
		double meanLat = (lat1 + lat2) / 2;
		double r = EARTH_MAJOR_AXIS * EARTH_MINOR_AXIS * Math.pow(Math.hypot(EARTH_MINOR_AXIS * Math.cos(meanLat), EARTH_MAJOR_AXIS * Math.sin(meanLat)), -1);
		return 2 * r * Math.asin(Math.sqrt(Math.pow(Math.sin(dLat / 2), 2) + Math.cos(lat1) * Math.cos(lat2) * Math.pow(Math.sin(dLon / 2), 2)));
	}//eo distanceTo
	
	public String toString() {
		return abbrev + " " + latitude + " " + longitude + " " + radius + " " + name;
	}//eo toString
	
}//eo Waypoint class