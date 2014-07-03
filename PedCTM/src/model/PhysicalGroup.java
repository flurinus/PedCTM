/**
 * Class representing a group of pedestrians
 * 
 * @author Thomas Muehlematter
 * 
 */
package model;

import java.util.ArrayList;
import java.util.Hashtable;

public class PhysicalGroup {

	private String route; // id of the group (unique in the simulation)

	private int startTime; // starting time when people of the group appear in
							// the simulation for the first time
	private int endTime; // idem for end time

	private String source; // source cell
	private String sink; // sink cell

	private String[] routeArray; // sequence of zones traversed by the group
	private double[] startRepartition; // repartition of the number of people
										// appearing for each interval
	private Hashtable<Integer, ArrayList<String>> TT; // travel times table

	public PhysicalGroup(String id, String so, String si, String[] route, double[] r) {

		this.startTime = 0;
		this.source = so;
		this.sink = si;
		this.startRepartition = r;
		this.TT = new Hashtable<Integer, ArrayList<String>>();
		this.route = id;
		this.routeArray = route;

	}

	public int getStartTime() {

		return startTime;

	}

	public int getEndTime() {

		return endTime;

	}

	public void setEndTime(int endTime) {

		this.endTime = endTime;

	}

	public String[] getRouteArray() {

		return this.routeArray;

	}

	public void setRouteArray(String[] r) {

		this.routeArray = r;

	}

	public String getSource() {

		return source;

	}

	public String getSink() {

		return sink;

	}

	public double[] getStartRepartition() {

		return startRepartition;

	}

	public void setStartRepartition(double[] startRepartition) {

		this.startRepartition = startRepartition;

	}

	public Hashtable<Integer, ArrayList<String>> getTT() {

		return this.TT;

	}

	public String getRoute() {

		return route;

	}

	/**
	 * add a certain travel time to the table when a group exits the simulation
	 */
	public void addTravelTime(Integer start, Integer tt, double n) {

		String value = Integer.toString(tt).concat("/" + Double.toString(n));

		ArrayList<String> tmp;

		if (TT.containsKey(start)) {

			tmp = TT.get(start);

		} else {

			tmp = new ArrayList<String>();

		}

		tmp.add(value);

		TT.put(start, tmp);

	}

}
