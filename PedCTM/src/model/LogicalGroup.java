package model;

/**
 * represents the people in a cell
 * 
 * @author Thomas Muehlematter
 * 
 */
public class LogicalGroup {

	// all variables are public to makes access faster
	public final String route;
	public final int startInterval;
	public double numberPeople;

	public LogicalGroup(String r, int interval, double numberPeople) {

		this.route = r;
		this.startInterval = interval;
		this.numberPeople = numberPeople;

	}

	public boolean sameAs(LogicalGroup in) {
		boolean result = false;
		if (in.route.equals(route) && in.startInterval == startInterval) {
			result = true;
		}

		return result;
	}

}
