package model;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;

/**
 * Represents a cell
 * 
 * @author Thomas Muehlematter
 * 
 */
public class Cell {

	private String cid; // cell id (unique for a certain layout)
	private String zone; // zone in which the cell is placed
	private double shape; // gamma
	private double area; // area = deltaL*deltaL
	private double kc; // critical density
	private double delta; // delta parameter
	private double maxCapacity; // maximum number of people in the cell
	private double Qmax; // maximum flow in the cell
	private double nmax; // number of people in the cell corresponding to the
							// maximum flow

	private float[] coordinates; // coordinates of the cell in the visual
									// representation
	private LinkedList<String> neighbors; // list of the neighbors of the cell

	private double numberPeople; // total number of people in the cell

	ArrayList<LogicalGroup> people;
	Hashtable<String, Double> sending;
	Hashtable<String, ArrayList<LogicalGroup>> receiving;

	// potentials parameters

	private Hashtable<String, Double> staticPotentials;

	private double f; // temporary value of the static potential for a certain
						// route
	private double d; // temporary value of the total potential for a certain
						// route

	private double projectedOccupancy;

	private double Rc; // receiving capacity of the cell

	/**
	 * constructor of the cell
	 */
	public Cell(String cid, String z, double shape, double area, double kc,
			double delta, float[] coo, String[] n) {

		this.cid = cid;
		this.zone = z;
		this.shape = shape;
		this.area = area;
		this.kc = kc;
		this.delta = delta;
		this.maxCapacity = area * kc;

		this.sending = new Hashtable<String, Double>();
		this.receiving = new Hashtable<String, ArrayList<LogicalGroup>>();
		this.coordinates = coo.clone();
		this.neighbors = new LinkedList<String>();

		for (int i = 0; i < n.length; i++) {

			this.neighbors.add(n[i]);
			sending.put(n[i], 0.0);

		}

		this.numberPeople = 0.0;

		this.people = new ArrayList<LogicalGroup>();

		this.projectedOccupancy = 0.0;

		this.Rc = 0.0;

		this.staticPotentials = new Hashtable<String, Double>();

		this.f = 0.0;

		double relStartPoint = 0.3; // default = 0.3; n_0 = 0.3 N is a heuristic choice
		
		double x0 = (this.shape * this.area) / (relStartPoint * this.maxCapacity);

		

		double xN = (this.shape * this.area) / this.maxCapacity;

		double a = 0.5 * (1 - x0) * Math.exp(xN - x0);
		double b = Math.pow(x0, 2) * Math.exp(xN - x0);
		double c = 1
				- (1 + x0 + (0.5 * Math.pow(x0, 2)) + (0.5 * Math.pow(x0, 3)))
				* Math.exp(xN - x0);

		double xopt = (-b + Math.sqrt(Math.pow(b, 2) - (4 * a * c))) / (2 * a);

		// neglecting negative root

		this.nmax = (this.shape * this.area) / xopt;

		this.Qmax = nmax
				* (1 - Math.exp(-this.shape * this.area
						* ((1 / nmax) - (1 / this.maxCapacity))));
		
		if ( !(this.Qmax > 0 && (! (Double.isInfinite(this.Qmax)) ) ) || !(this.nmax > 0)){
			System.out.println("Error: Qmax or nmax of cell " + this.cid + " is negative, infinite or NaN.\n" +
					"Currently nmax = " + this.nmax + ", Qmax = " + this.Qmax + "\n" +
					"Choose an appropriate initial guess for nmax (see relStartPoint). \n" +
					"Currently relStartPoint = " + relStartPoint + " (default 0.3)");
					
		}
		else {
			//System.out.println("Cell: " + this.cid + " Qmax: " + Qmax + ", nmax: " + nmax);
		}
	}

	/**
	 * default constructor, used to preallocate memory.
	 */

	public Cell() {

	}

	public String getCid() {
		return cid;
	}

	public double getMaxCapacity() {

		return maxCapacity;

	}

	public double getShape() {

		return shape;

	}

	public void setShape(double d) {

		this.shape = d;

	}

	public void setMaxCapacity(double d) {

		this.maxCapacity = d;

	}

	public double getArea() {

		return area;

	}

	public double getQmax() {

		return Qmax;

	}

	public void setQmax(double qmax) {

		Qmax = qmax;

	}

	public double getNmax() {

		return nmax;

	}

	public void setNmax(double nmax) {

		this.nmax = nmax;

	}

	public float[] getCoordinates() {

		return coordinates;

	}

	public LinkedList<String> getNeighbors() {

		return neighbors;
	}

	public double getNumberPeople() {

		return numberPeople;

	}

	/**
	 * 
	 * returns the number of people of a certain group that are in the cell
	 * 
	 * @param gid
	 *            - group id
	 */
	public double getGroupPeople(String gid) {

		double result = 0;
		for (LogicalGroup in : people) {
			if (in.route.equals(gid)) {
				result += in.numberPeople;
			}
		}

		return result;
	}

	/**
	 * 
	 * returns the corresponding receiving table
	 * 
	 * @param cid
	 *            - cell id of the considered neighbor
	 */
	public ArrayList<LogicalGroup> getReceiving(String cid) {
		return receiving.get(cid);

	}

	/**
	 * indicates to the cell that the receiving of the particular group has
	 * already been received
	 * 
	 * @param cid
	 *            - id of the sending cell
	 * @param group
	 *            - group position
	 */

	public void receivingDone(String cid, String gid, int interval) {

		ArrayList<LogicalGroup> tmp = receiving.get(cid);
		LogicalGroup toRemove = null;
		for (LogicalGroup in : tmp) {
			if (in.route.equals(gid) && in.startInterval == interval) {
				toRemove = in;
			}
		}
		tmp.remove(toRemove);

		receiving.put(cid, tmp);

	}

	public void setReceiving(String cid, LogicalGroup in) {

		ArrayList<LogicalGroup> intervals = receiving.get(cid);
		if (intervals == null) {
			intervals = new ArrayList<LogicalGroup>();
		}
		intervals.add(in);
		receiving.put(cid, intervals);

	}

	public double getProjectedOccupancy() {

		return projectedOccupancy;

	}

	public void addProjectedOccupancy(double projectedOccupancy) {

		this.projectedOccupancy += projectedOccupancy;
		
		//System.out.println("added projected inflow of " + projectedOccupancy + 
		//" to cell " + this.cid + " totaling now in " + this.projectedOccupancy);

	}

	//public void setProjectedOccupancy(double projectedOccupancy) {

	//	this.projectedOccupancy = projectedOccupancy;

	//}

	/**
	 * add people to the cell
	 * 
	 * @param intervals
	 *            - list of the intervals to add
	 */

	public void addPeople(LogicalGroup rec) {

		boolean found = false;
		for (LogicalGroup pe : people) {

			if (pe.sameAs(rec)) {

				pe.numberPeople += rec.numberPeople;
				this.numberPeople += rec.numberPeople;

				found = true;
				break;
			}
		}

		if (found == false) {
			this.people.add(rec);
			this.numberPeople += rec.numberPeople;

		}

	}

	/**
	 * remove people from the cell from a certain group
	 * 
	 * @param route
	 *            - group id
	 * @param number
	 *            - number of people to be subtracted
	 * @param result
	 *            . list containing the resulting subtraction (given for
	 *            optimization reasons)
	 * 
	 * @param toRemove
	 *            - list of the elements to be removed (given for optimization
	 *            reasons)
	 */

	public void subPeople(LogicalGroup rec) {

		LogicalGroup toRemove = null;

		for (LogicalGroup in : people) {

			if (in.route.equals(rec.route)
					&& in.startInterval == rec.startInterval) {

				in.numberPeople -= rec.numberPeople;
				this.numberPeople -= rec.numberPeople;

				if (in.numberPeople <= 0.0) {
					toRemove = in;
				}
				break;
			}
		}

		if (toRemove != null) {

			people.remove(toRemove);

		}

	}

	/**
	 * reinitializes the projections of the cell
	 */

	public void reinitializeProjections() {

		this.projectedOccupancy = 0.0;

		sending.clear();
		receiving.clear();

	}

	/**
	 * returns the flow of the cell
	 */

	public double getFlow() {

		if (numberPeople > 0 && numberPeople < maxCapacity) {

			return numberPeople
					* (1 - Math.exp(-shape * area
							* ((1 / numberPeople) - (1 / maxCapacity))));
		} else {

			return 0.0;

		}
	}

	/**
	 * returns the hydrodynamic outflow capacity of the cell
	 */

	public double getQs() {

		if (numberPeople < nmax) {

			return this.getFlow();

		} else {

			return Qmax;

		}

	}

	/**
	 * computes the receiving capacity of the cell
	 */

	public void computeReceivingCapacity() {

		double flow = 0.0;

		if ( ! (nmax > 0) ) {
			System.out.println("Warning: Negative nmax of value " + nmax);
		}
		
		if (numberPeople > nmax) {

			flow = getFlow();

		} else {

			flow = Qmax;

		}

		Rc = Math.min(flow, delta * (maxCapacity - numberPeople));
		
		//if (true){ //DEBUG
		//	System.out.println("Receiving capacity: " + Rc);
		//	System.out.println("flow: " + flow + ", delta: " + delta + ", maxCapacity: " +
		//			maxCapacity + ", numberPeople: " + numberPeople);
		//}
		
		
		if (!(Rc >= 0)) {
			System.out.println("\nWarning: Capacity exceeded in cell "+ this.cid + " (" +
					delta * (maxCapacity - numberPeople) + ")");
			//System.out.println("Negative receiving capacity"
			//		+ " hydrodynamic inflow = " + flow + 
			//		", remaining capacity = " + delta * (maxCapacity - numberPeople) 
			//		+ "\n delta = " + delta + ", maxCapacity = " + maxCapacity +
			//		", numberPeople = " + numberPeople + ", nmax = " + nmax +
			//		"\n Qmax = " + Qmax);
			Rc = 0;
		}
		
	}

	/**
	 * returns the receiving capacity of the cell
	 * 
	 */
	public double getRc() {

		return this.Rc;

	}

	/**
	 * returns the speed in the cell
	 */
	public double computeSpeed() {

		if (this.numberPeople < 0.0) {

			return 1.0;

		} else {

			return (1 - Math.exp(-shape * area
					* ((1 / this.numberPeople) - (1 / this.maxCapacity))));
		}

	}

	/**
	 * computes the sum of all the sending to the current cell
	 */
	public double getSumSending() {

		Enumeration<String> e = receiving.keys();
		double result = 0.0;

		while (e.hasMoreElements()) {

			String next = e.nextElement();

			result += getSending(next);

		}

		return result;

	}

	/**
	 * returns the sum of sending projected
	 * 
	 * @param cid
	 *            - id of the neighbor considered
	 */
	public double getSending(String cid) {

		double result = 0.0;
		ArrayList<LogicalGroup> tmp = receiving.get(cid);

		for (LogicalGroup in : tmp) {

			result += in.numberPeople;

		}

		return result;

	}

	public double getF() {

		return f;

	}

	public void setF(double f) {

		this.f = f;

	}

	public String getZone() {

		return this.zone;

	}

	public void setArea(double d) {

		this.area = d;

	}

	/**
	 * returns the static potential of the corresponding route
	 */
	public double getSFF(String route) {

		double pot = this.staticPotentials.get(route);

		if (this.staticPotentials.containsKey(route)) {

			return pot;

		} else {

			return -1.0;

		}
	}

	/**
	 * puts the static potential for a certain route
	 */
	public void addSFF(String route, double potential) {

		this.staticPotentials.put(route, potential);

	}

	public double getD() {

		return d;

	}

	public void setD(double d) {

		this.d = d;

	}

	/**
	 * returns the critical density
	 */
	public double getKc() {

		return kc;

	}

	public ArrayList<LogicalGroup> getPeople() {
		return people;
	}

}
