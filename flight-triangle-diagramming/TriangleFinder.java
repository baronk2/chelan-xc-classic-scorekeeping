/*
Kevin Baron
6/25/13
CXCC 2013 Triangles Project
TriangleFinder
*/

//Omitted waypoints
//AIRP13 47.866345 119.943258 0.50 Chelan Airport (LZ)
//EPHR13 47.306303 119.521764 0.25 Ephrata Airport

import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.ArrayList;
import java.awt.Graphics;
import java.awt.Color;
import java.util.Queue;
import java.util.PriorityQueue;
import java.util.LinkedList;
import java.io.PrintStream;
import javax.swing.KeyStroke;

public class TriangleFinder {
	
	private static final String INPUT_FILE_NAME = "WaypointCoordinates";
	private static final String LAUNCH = "BUTT37";
	private static final String[] LZS = {"JYLZ12", "SOCR07"};
	private static final int DISPLAY_SIZE = 400;
	private static final int DISPLAY_MARGIN = 60;
	private static final int POINT_SIZE = 4;
	private static final int DOT_SIZE = 10;
	//used to convert degrees to miles, centered at the latitude of the Butte
	private static final double LAT_DEGREE_LENGTH = 69.08797116454625;
	private static final double LON_DEGREE_LENGTH = 46.542509265327965;
	private static final int INFO_TEXT_MARGIN = 15;
	private static final int INFO_TEXT_LINE_SPACE = 15;
	private static final double MAX_ACCEPTABLE_ANGLE = 100.0;
	
	private List<Waypoint> waypoints;
	private List<Waypoint> turnpoints;
	private PriorityQueue<Triangle> goodAngleTriangles;
	private PriorityQueue<Triangle> badAngleTriangles;
	private List<Triangle> acceptedTriangles;
	private List<Triangle> rejectedTriangles;
	private List<Triangle> undecidedTriangles;
	
	public TriangleFinder(String inputFileName) throws FileNotFoundException {
		Scanner input = new Scanner(new File(inputFileName));
		waypoints = new ArrayList<Waypoint>();
		while (input.hasNextLine())
			waypoints.add(new Waypoint(input.next(), input.nextDouble(), input.nextDouble(), input.nextDouble(), input.nextLine().trim()));
		turnpoints = new ArrayList<Waypoint>(waypoints);
		turnpoints.remove(getWaypoint(LAUNCH));
		for (int i = 0; i < LZS.length; i++)
			turnpoints.remove(getWaypoint(LZS[i]));
		List<Triangle> allTriangles = new ArrayList<Triangle>();
		Waypoint butte = getWaypoint("BUTT37");
		for (int i = 0; i < turnpoints.size() - 1; i++) {
			Waypoint turnpoint1 = turnpoints.get(i);
			for (int j = i + 1; j < turnpoints.size(); j++) {
				Waypoint turnpoint2 = turnpoints.get(j);
				allTriangles.add(new Triangle(butte, turnpoint1, turnpoint2, butte));
			}//eo for
		}//eo for
		goodBadAngle(allTriangles);
		acceptedTriangles = new ArrayList<Triangle>();
		rejectedTriangles = new ArrayList<Triangle>();
		undecidedTriangles = new ArrayList<Triangle>();
	}//eo TriangleFinder constructor
	
	public void goodBadAngle(List<Triangle> triangles) {
		goodAngleTriangles = new PriorityQueue<Triangle>();
		badAngleTriangles = new PriorityQueue<Triangle>();
		for (int i = 0; i < triangles.size(); i++) {
			Triangle triangle = triangles.get(i);
			if (triangle.maxAngle() <= MAX_ACCEPTABLE_ANGLE)
				goodAngleTriangles.add(triangle);
			else
				badAngleTriangles.add(triangle);
		}//eo while
	}//eo goodBadAngle
	
	public List<Waypoint> getWaypoints() {
		return waypoints;
	}//eo getWaypoints
	
	public List<Waypoint> getTurnpoints() {
		return turnpoints;
	}//eo getTurnpoints
	
	public Waypoint getWaypoint(String abbrev) {
		for (int i = 0; i < waypoints.size(); i++)
			if (abbrev.equals(waypoints.get(i).abbrev))
				return waypoints.get(i);
		throw new IllegalArgumentException("waypoints list does not contain Waypoint: " + abbrev);
	}//eo getWaypoint
	
	public void display(Triangle triangle) {
		int[] screenDimensions = screenDimensions();
		DrawingPanel panel = new DrawingPanel(screenDimensions[0], screenDimensions[1]);
		Graphics g = panel.getGraphics();
		for (Waypoint waypoint : waypoints) {
			drawWaypoint(g, waypoint, POINT_SIZE, false);
		}//eo for each
		g.setColor(Color.BLUE);
		Waypoint launchpoint = triangle.waypoints.get(0);
		drawWaypoint(g, launchpoint, DOT_SIZE, true);
		for (int i = 1; i < triangle.waypoints.size(); i++) {
			connectWaypoints(g, triangle.waypoints.get(i - 1), triangle.waypoints.get(i), true);
			drawWaypoint(g, triangle.waypoints.get(i), DOT_SIZE, true);
		}//eo for
		g.drawString(triangle.toString(), INFO_TEXT_MARGIN, INFO_TEXT_LINE_SPACE);
		g.drawString("Angles: " + round(triangle.getAngles(), 1), 2 * INFO_TEXT_MARGIN, 2 * INFO_TEXT_LINE_SPACE);
		g.drawString("Standard Deviation of Angles: " + round(triangle.standardDeviation(triangle.getAngles()), 4), 3 * INFO_TEXT_MARGIN, 3 * INFO_TEXT_LINE_SPACE);
		g.drawString("Largest Angle: " + round(triangle.maxAngle(), 1), 3 * INFO_TEXT_MARGIN, 4 * INFO_TEXT_LINE_SPACE);
		g.drawString("Legs: " + round(triangle.getLegs(), 1), 2 * INFO_TEXT_MARGIN, 5 * INFO_TEXT_LINE_SPACE);
		g.drawString("Standard Deviation of Legs: " + round(triangle.standardDeviation(triangle.getLegs()), 4), 3 * INFO_TEXT_MARGIN, 6 * INFO_TEXT_LINE_SPACE);
		g.drawString("Smallest Leg Percentage: " + round(triangle.smallestLegPercent(), 1), 3 * INFO_TEXT_MARGIN, 7 * INFO_TEXT_LINE_SPACE);
	}//eo display
	
	public void connectWaypoints(Graphics g, Waypoint waypoint1, Waypoint waypoint2, boolean displayDistance) {
		int[] pixels1 = coordinateConversion(waypoint1.longitude, waypoint1.latitude);
		int[] pixels2 = coordinateConversion(waypoint2.longitude, waypoint2.latitude);
		g.drawLine(pixels1[0], pixels1[1], pixels2[0], pixels2[1]);
		if (displayDistance)
			g.drawString("" + round(waypoint1.distanceTo(waypoint2), 1), (pixels1[0] + pixels2[0]) / 2 + DOT_SIZE, (pixels1[1] + pixels2[1]) / 2 + DOT_SIZE / 2);
	}//eo connectWaypoints
	
	public static double round(double d, int places) {
		double power = Math.pow(10, places);
		return (int) (d * power + 0.5) / power;
	}//eo round10s(double, int)
	
	public static List<Double> round(List<Double> list, int places) {
		List<Double> toReturn = new ArrayList<Double>(list);
		for (int i = 0; i < toReturn.size(); i++)
			toReturn.set(i, round(toReturn.get(i), places));
		return toReturn;
	}//eo round(List<Double>, int)
	
	public void drawWaypoint(Graphics g, Waypoint waypoint, int size, boolean displayName) {
		int[] pixels = coordinateConversion(waypoint.longitude, waypoint.latitude);
		g.fillOval(pixels[0] - size / 2, pixels[1] - size / 2, size, size);
		if (displayName)
			g.drawString(waypoint.abbrev, pixels[0] + size / 2 + 1, pixels[1] + size / 2);
	}//eo drawWaypoint
	
	public int[] coordinateConversion(double lon, double lat) {
		double[] extremes = extremes();
		int[] toReturn = {(int) ((lon - extremes[0]) * DISPLAY_SIZE * LON_DEGREE_LENGTH / LAT_DEGREE_LENGTH) + DISPLAY_MARGIN, (int) ((extremes[3] - lat) * DISPLAY_SIZE) + DISPLAY_MARGIN};
		return toReturn;
	}//eo coordinateConversion
	
	public int[] screenDimensions() {
		double[] extremes = extremes();
		double dLon = extremes[1] - extremes[0];
		double dLat = extremes[3] - extremes[2];
		int[] toReturn = {(int) (dLon * DISPLAY_SIZE * LON_DEGREE_LENGTH / LAT_DEGREE_LENGTH) + 2 * DISPLAY_MARGIN, (int) (dLat * DISPLAY_SIZE) + 2 * DISPLAY_MARGIN};
		return toReturn;
	}//eo screenDimensions
	
	public double[] extremes() {
		double[] extremes = {waypoints.get(0).longitude, waypoints.get(0).longitude, waypoints.get(0).latitude, waypoints.get(0).latitude};
		for (int i = 1; i < waypoints.size(); i++) {
			Waypoint waypoint = waypoints.get(i);
			extremes[0] = Math.min(extremes[0], waypoint.longitude);
			extremes[1] = Math.max(extremes[1], waypoint.longitude);
			extremes[2] = Math.min(extremes[2], waypoint.latitude);
			extremes[3] = Math.max(extremes[3], waypoint.latitude);
		}//eo for
		return extremes;
	}//eo extremes
	
	public void acceptReject(PriorityQueue<Triangle> triangles) {
		Scanner console = new Scanner(System.in);
		while (!triangles.isEmpty()) {
			System.out.println(acceptedTriangles.size() + " accepted. " + rejectedTriangles.size() + " rejected.\n" + triangles.size() + " undecided in this group.");
			Triangle triangle = triangles.remove();
			display(triangle);
			System.out.println(triangle);
			System.out.println("A to ACCEPT. R to REJECT. Q to QUIT.");
			String response = console.nextLine();
			if (response.equalsIgnoreCase("A"))
				acceptedTriangles.add(triangle);
			else if (response.equalsIgnoreCase("R"))
				rejectedTriangles.add(triangle);
			else if (response.equalsIgnoreCase("Q")) {
				undecidedTriangles.add(triangle);
				break;
			}//eo if else block
			else
				triangles.add(triangle);
		}//eo while
		while (!triangles.isEmpty())
			undecidedTriangles.add(triangles.remove());
	}//eo acceptReject
	
	public PriorityQueue<Triangle> getGoodAngleTriangles() {
		return goodAngleTriangles;
	}//eo getGoodAngleTriangles
	
	public PriorityQueue<Triangle> getBadAngleTriangles() {
		return badAngleTriangles;
	}//eo getGoodAngleTriangles
	
	public List<Triangle> getAcceptedTriangles() {
		return acceptedTriangles;
	}//eo getAcceptedTriangles
	
	public List<Triangle> getRejectedTriangles() {
		return rejectedTriangles;
	}//eo getRejectedTriangles
	
	public List<Triangle> getUndecidedTriangles() {
		return undecidedTriangles;
	}//eo getRejectedTriangles
	
	public void read() throws FileNotFoundException {
		File accepted = new File("accepted");
		File rejected = new File("rejected");
		File undecided = new File("undecided");
		acceptedTriangles = readFile(accepted, new ArrayList<Triangle>());		
		rejectedTriangles = readFile(rejected, new ArrayList<Triangle>());
		undecidedTriangles = readFile(undecided, new ArrayList<Triangle>());
		goodBadAngle(undecidedTriangles);
	}//eo askLoad
	
	public List<Triangle> readFile(File f, List<Triangle> triangles) throws FileNotFoundException {
		Scanner input = new Scanner(f);
		while (input.hasNextLine()) {
			String w1 = input.next();
			String w2 = input.next();
			input.nextLine();
			Waypoint butte = getWaypoint("BUTT37");
			triangles.add(new Triangle(butte, getWaypoint(w1), getWaypoint(w2), butte));
		}//eo while
		return triangles;
	}//eo readFile
	
	public static void main(String[] args) throws FileNotFoundException {
		TriangleFinder finder = new TriangleFinder(INPUT_FILE_NAME);
		//System.out.println(finder.getWaypoint("17H220"));
		//System.out.println(finder.getWaypoint("BUTT37").distanceTo(finder.getWaypoint("MANS23")));
		/*
		for (Waypoint w : finder.getTurnpoints())
			System.out.println(w);
		*/
		/*
		List<Waypoint> trianglePoints = new ArrayList<Waypoint>();
		trianglePoints.add(finder.getWaypoint("BUTT37"));
		trianglePoints.add(finder.getWaypoint("FARM24"));
		trianglePoints.add(finder.getWaypoint("SIMS22"));
		trianglePoints.add(finder.getWaypoint("BUTT37"));
		Triangle bFSB = new Triangle(trianglePoints);
		System.out.println(bFSB);
		*/
		/*
		Triangle bFSB = new Triangle(finder.getWaypoint("BUTT37"), finder.getWaypoint("WITH25"), finder.getWaypoint("SIMS22"), finder.getWaypoint("BUTT37"));
		System.out.println("bFSB: " + bFSB);
		System.out.println("bFSB.smallestLegPercent(): " + bFSB.smallestLegPercent());
		System.out.println("bFSB.getAngles(): " + bFSB.getAngles());
		System.out.println("bFSB.getLegs(): " + bFSB.getLegs());
		System.out.println("bFSB.standardDeviation(bFSB.getAngles().subList(0, 3)): " + bFSB.standardDeviation(bFSB.getAngles().subList(0, 3)));
		System.out.println("bFSB.standardDeviation(bFSB.getLegs()): " + bFSB.standardDeviation(bFSB.getLegs()));
		finder.display(bFSB);
		*/
		Scanner console = new Scanner(System.in);
		System.out.println("Would you like to load the previous accepted/rejected/undecided triangles? (y/n)");
		if (console.nextLine().equalsIgnoreCase("y")){
			finder.read();
		}//eo if
		finder.acceptReject(finder.getGoodAngleTriangles());
		finder.acceptReject(finder.getBadAngleTriangles());
		File accepted = new File("accepted");
		File rejected = new File("rejected");
		File undecided = new File("undecided");
		write(accepted, finder.getAcceptedTriangles());
		write(rejected, finder.getRejectedTriangles());
		write(undecided, finder.getUndecidedTriangles());
	}//eo main
	
	public static void write(File outputFile, List<Triangle> triangles) throws FileNotFoundException {
		PrintStream output = new PrintStream(outputFile);
		for (Triangle triangle : triangles)
			output.println(triangle.waypoints.get(1).abbrev + " " + triangle.waypoints.get(2).abbrev);
	}//eo write
	
}//eo TriangleFinder class