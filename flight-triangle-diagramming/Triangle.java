/*
Kevin Baron
6/30/13
CXCC 2013 Triangles Project
Triangle
*/

import java.util.List;
import java.util.ArrayList;

public class Triangle implements Comparable<Triangle> {
	
	public List<Waypoint> waypoints;
	
	public Triangle(List<Waypoint> waypoints) {
		if (waypoints.size() != 4)
			throw new IllegalArgumentException("All Triangle objects must have exactly 4 waypoints, given: " + waypoints.size());
		this.waypoints = new ArrayList<Waypoint>(waypoints);
	}//eo Triangle(List<Waypoint>) constructor
	
	public Triangle(Waypoint waypoint1, Waypoint waypoint2, Waypoint waypoint3, Waypoint waypoint4) {
		waypoints = new ArrayList<Waypoint>();
		waypoints.add(waypoint1);
		waypoints.add(waypoint2);
		waypoints.add(waypoint3);
		waypoints.add(waypoint4);
	}//eo Triangle(Waypoint, Waypoint, Waypoint, Waypoint)
	
	public List<Double> getLegs() {
		List<Double> legs = new ArrayList<Double>();
		for (int i = 1; i < waypoints.size(); i++)
			legs.add(waypoints.get(i - 1).distanceTo(waypoints.get(i)));
		return legs;
	}//eo legsConstructor
	
	public double totalDistance() {
		double toReturn = 0;
		for (Double leg : getLegs())
			toReturn += leg;
		return toReturn;
	}//eo totalDistance
	
	public int score() {
		return (int) (totalDistance() * 15 + 0.5);
	}//eo score
	
	public double smallestLegPercent() {
		double smallest = getLegs().get(0);
		List<Double> legs = getLegs();
		for (int i = 1; i < legs.size(); i++)
			smallest = Math.min(smallest, legs.get(i));
		return smallest / totalDistance() * 100.0;
	}//eo smallestLegPercent
	
	public double maxAngle() {
		List<Double> angles = getAngles();
		double max = angles.get(0);
		for (int i = 1; i < angles.size(); i++)
			max = Math.max(max, angles.get(i));
		return max;
	}//eo maxAngle
	
	public List<Double> getAngles() {
		List<Double> angles = new ArrayList<Double>();
		angles.add(180 / Math.PI * getAngle(waypoints.get(2), waypoints.get(0), waypoints.get(1)));
		angles.add(180 / Math.PI * getAngle(waypoints.get(0), waypoints.get(1), waypoints.get(2)));
		angles.add(180 / Math.PI * getAngle(waypoints.get(1), waypoints.get(2), waypoints.get(3)));
		angles.add(180 / Math.PI * getAngle(waypoints.get(2), waypoints.get(3), waypoints.get(1)));
		return angles;
	}//eo getAngles
	
	public double getAngle(Waypoint waypointA, Waypoint waypointB, Waypoint waypointC) {
		double legA = waypointB.distanceTo(waypointC);
		double legB = waypointA.distanceTo(waypointC);
		double legC = waypointA.distanceTo(waypointB);
		return Math.acos((Math.pow(legA, 2) + Math.pow(legC, 2) - Math.pow(legB, 2)) / (2 * legA * legC));
	}//eo getAngle
	
	public double standardDeviation(List<Double> list) {
		List<Double> deviations = new ArrayList<Double>(list);
		double average = average(list);
		for (int i = 0; i < deviations.size(); i++)
			deviations.set(i, Math.abs(deviations.get(i) - average));
		return average(deviations) / average;
	}//eo standardDeviation
	
	public double average(List<Double> list) {
		double sum = 0;
		for (double d : list)
			sum += d;
		return sum / list.size();
	}//eo average
	
	public String toString() {
		String toReturn = "";
		for (Waypoint waypoint : waypoints)
			toReturn += waypoint.abbrev + " ";
		return toReturn.trim() + ": " + TriangleFinder.round(totalDistance(), 1) + " miles. " + score() + " points.";
	}//eo toString
	
	public int compareTo(Triangle other) {
		return (int) (100000 * (other.smallestLegPercent() - smallestLegPercent()) + 0.5);
	}//eo compareTo()
	
	public String legsToString() {
		String toReturn = "";
		List<Double> legs = getLegs();
		for (int i = 1; i < waypoints.size(); i++)
			toReturn += waypoints.get(i - 1).abbrev + " to " + waypoints.get(i).abbrev + ": " + legs.get(i - 1) + " miles, ";
		return toReturn.substring(0, toReturn.length() - 2) + ".";
	}//eo legsToString
	
}//eo Triangle class