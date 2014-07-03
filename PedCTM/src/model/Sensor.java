package model;

/**
 * sensor class used to print the file containing the arrival times at the
 * sensors.
 * 
 * @author Thomas Muehlematter
 * 
 */
public class Sensor {

	private String id; // id of the sensor
	private int departureTime;
	private String route;
	private int arrivalTime;
	private double weight; // number of people

	/**
	 * constructor of the sensor-line
	 */
	public Sensor(int d, String r, int a, double w) {

		this.id = String.valueOf(d).concat(r.concat(String.valueOf(a)));
		this.departureTime = d;
		this.route = r;
		this.arrivalTime = a;
		this.weight = w;

	}

	public String getId() {
		return id;
	}

	public String getRoute() {
		return route;
	}

	public int getDepartureTime() {
		return departureTime;
	}

	public int getArrivalTime() {
		return arrivalTime;
	}

	public double getWeight() {
		return weight;
	}

	/**
	 * gets the string representation that will be printed from the sensor
	 */
	public String getString() {
		return String.valueOf(departureTime).concat(
				"," + route + "," + String.valueOf(arrivalTime) + "," + weight
						+ "\n");
	}

	/**
	 * adds a certain number of people to the existing line representation
	 */
	public void addWeight(double w) {
		this.weight += w;

	}

}
