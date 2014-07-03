package model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Observable;
import java.util.Set;
import java.util.TreeSet;

/**
 * Represents the board of the calibration
 * 
 * @author Thomas Muehlematter
 * 
 */

public class Board extends Observable {

	// maximum number of lines and maximum line length in the layout file

	private static final int LimitLinesNumber = 20000;
	private static final int LimitLineLength = 1000;

	/*
	 * Length of the sleep in milliseconds must be at least 1 to avoid
	 * concurrency errors (used in the visualization).
	 */

	private static final long SleepLength = 1;

	
	/*
	 * Maximum number of simulation time steps
	 */
	
	private static final long MaxTimeSteps = 9999;
	
	/*
	 * Accepted amount of residual people remaining in the simulation to stop
	 * the simulation. It accounts for numerical roundings.
	 */

	private static final double endingSum = 0.01;

	/* decides if the software is going to output pictures or only text files */

	private boolean visualized;

	private Hashtable<String, Cell> cells;

	private Hashtable<String, Hashtable<String, Sensor>> sensors;

	private PhysicalGroup[] groups;
	private String[] sources;
	private String[] sinks;
	private float[] bounds;

	private int timestep;
	private int startingTime;

	private int numberCells;
	private int numberGroups;

	private boolean go;
	private boolean end;

	private Hashtable<String, PhysicalGroup> demand;

	private Hashtable<String, Hashtable<Integer, Hashtable<Double, Double>>> data;

	private ArrayList<String> textOutput;

	private Hashtable<String, ArrayList<String>> routeTextOutput;

	private String filenameLayout;

	private String filenameDemand;

	private double alpha; // decides the impact of the congestion on route
	// choice

	private double beta; // decides the impact of the congestion on route
							// choice

	private double timeInterval; // interval of time considered

	private double deltaL; // length of cells A= deltaL*deltaL

	/**
	 * constructor of the board
	 */

	public Board(boolean v) {

		this.visualized = v;

		this.timestep = 0;
		this.startingTime = 0;
		this.go = false;
		this.end = false;

		this.demand = new Hashtable<String, PhysicalGroup>();

		this.data = new Hashtable<String, Hashtable<Integer, Hashtable<Double, Double>>>();

		sensors = new Hashtable<String, Hashtable<String, Sensor>>();

	}

	/**
	 * 
	 * loads the file containing the layout
	 * 
	 * @param f
	 *            - file to be loaded
	 */

	public boolean loadLayout(File f) {

		String[] lines = new String[LimitLinesNumber];

		BufferedReader br = null;

		try {

			this.filenameLayout = f.getAbsolutePath();

			String currentLine;

			br = new BufferedReader(new FileReader(f));

			int i = 0;

			while ((currentLine = br.readLine()) != null) {

				lines[i] = currentLine;
				i++;
			}

		} catch (IOException e) {

			return false;

		} finally {

			try {

				if (br != null) {
					br.close();
				}

			} catch (IOException ex) {

				return false;
			}
		}

		String[] line = new String[LimitLineLength];

		int index = 0;

		String cid;
		String zone;
		double shape;
		double area;
		double delta;
		double kc;

		String[] neighbors;
		float[] coordinates = new float[8];

		boolean valid = true;

		try {

			while (!lines[index].equals("#End") && valid) {

				if (index == 0) {

					// Simulation loading part

					if (lines[0].equals("#Simulation")
							&& lines[2].equals("#Cells")) {

						line = lines[index + 1].split(" ");

						numberCells = Integer.parseInt(line[0]);
						deltaL = Double.parseDouble(line[1]);
						alpha = Double.parseDouble(line[2]);
						beta = Double.parseDouble(line[3]);

						if (line.length > 4) {

							for (int i = 4; i < line.length; i++) {
								sensors.put(line[i],
										new Hashtable<String, Sensor>());
							}
						}

						cells = new Hashtable<String, Cell>();

						this.timestep = 0;

						this.textOutput = new ArrayList<String>();
						this.routeTextOutput = new Hashtable<String, ArrayList<String>>();

						bounds = new float[4];
						bounds[0] = Float.MAX_VALUE;
						bounds[1] = Float.MAX_VALUE;
						bounds[2] = 0;
						bounds[3] = 0;

						index += 3;

					} else {
						valid = false;
					}

					// Cell loading part

				} else if (index > 2 && index < numberCells * 3 + 2) {

					line = lines[index].split(" ");

					cid = line[0];
					zone = line[1];
					shape = Double.parseDouble(line[2]);
					area = Double.parseDouble(line[3]);
					kc = Double.parseDouble(line[4]);
					delta = Double.parseDouble(line[5]);
					line = lines[index + 1].split(" ");

					// find the bounds of the simulation to correctly draw the
					// output

					for (int i = 0; i < 8; i++) {
						coordinates[i] = Float.parseFloat(line[i]);
						if (i % 2 == 0) {
							if (coordinates[i] < bounds[0]) {
								bounds[0] = coordinates[i];
							}
							if (coordinates[i] > bounds[2]) {
								bounds[2] = coordinates[i];
							}
						} else {
							if (coordinates[i] < bounds[1]) {
								bounds[1] = coordinates[i];
							}
							if (coordinates[i] > bounds[3]) {
								bounds[3] = coordinates[i];
							}
						}
					}

					line = lines[index + 2].split(" ");
					neighbors = line;

					cells.put(cid, new Cell(cid, zone, shape, area, kc, delta,
							coordinates, neighbors));

					index += 3;

				} else if (index == numberCells * 3 + 3) {

					if (!lines[index].equals("#End")) {
						valid = false;
					}

				}

			}

		} catch (Exception e) {

			e.printStackTrace();
			valid = false;

		}

		return valid;
	}

	/**
	 * loads the demand file
	 */

	public boolean loadDemand(File f) {

		boolean valid = true;

		BufferedReader CSVFile;

		LinkedList<String[]> list = new LinkedList<String[]>();

		try {

			this.filenameDemand = f.getAbsolutePath();

			CSVFile = new BufferedReader(new FileReader(filenameDemand));

			String row = CSVFile.readLine();

			while (row != null) {

				String[] data = row.split(",");
				list.add(data);

				row = CSVFile.readLine();

			}

			CSVFile.close();

			String id = "";
			String source = "";
			String sink = "";

			double weight = 0;
			int departuretime = 0;

			String z1 = "";
			String z2 = "";

			int nRoutes = 0;

			Set<String> routes = new TreeSet<String>();

			for (String[] el : list) {

				id = el[1];

				String[] s = el[1].split("-");

				z1 = el[1].split("-")[0];
				z2 = s[s.length - 1];

				weight = Double.valueOf(el[3]);
				departuretime = Integer.valueOf(el[0]);

				if (!routes.contains(el[1])) {

					routes.add(el[1]);
					nRoutes++;

				}

				for (String cid : cells.keySet()) {

					Cell c = cells.get(cid);

					if (c.getZone().equals(z1)) {

						source = c.getCid();

					}

					if (c.getZone().equals(z2)) {

						sink = c.getCid();

					}
				}

				if (departuretime > startingTime) {

					startingTime = departuretime;

				}

				if (demand.containsKey(id)) {

					PhysicalGroup g = demand.get(id);
					double[] tmp;

					if (departuretime > g.getStartRepartition().length - 1) {
						tmp = new double[departuretime + 1];
						System.arraycopy(g.getStartRepartition(), 0, tmp, 0,
								g.getStartRepartition().length);
					} else {
						tmp = g.getStartRepartition();
					}

					tmp[departuretime] = weight;
					g.setStartRepartition(tmp);
					demand.put(id, g);

				} else {

					double[] startrepartition = new double[departuretime + 1];
					startrepartition[departuretime] = weight;
					PhysicalGroup g = new PhysicalGroup(id, source, sink,
							id.split("-"), startrepartition);
					demand.put(id, g);
				}

			}

			this.numberGroups = nRoutes;

		} catch (Exception e) {

			e.printStackTrace();
			valid = false;

		}

		return valid;
	}

	/**
	 * computes the static potential field of every cell for each route
	 */

	public void computePotentialFields() {

		for (int i = 0; i < groups.length; i++) {

			String route = groups[i].getRoute();
			String[] routeArray = groups[i].getRouteArray();
			Set<String> routeSet = new TreeSet<String>();

			for (int j = 0; j < routeArray.length; j++) {
				routeSet.add(routeArray[j]);
			}

			Cell sink = cells.get(groups[i].getSink());

			LinkedList<String> next = new LinkedList<String>();

			LinkedList<String> nei = new LinkedList<String>();

			for (String s : sink.getNeighbors()) {

				Cell c = cells.get(s);

				if (routeSet.contains(c.getZone())) {

					next.add(s);
				}
			}

			sink.setF(1); // sink has a potential of 1 by definition of the
							// algorithm

			cells.put(groups[i].getSink(), sink);

			int k = 2;

			while (!next.isEmpty()) {

				for (String cell : next) {

					Cell current = cells.get(cell);

					if (routeSet.contains(current.getZone())) {

						if (current.getF() == 0.0) {

							current.setF(k);

							cells.put(cell, current);

							for (String el : current.getNeighbors()) {

								Cell n = cells.get(el);

								if (routeSet.contains(n.getZone())
										&& n.getF() == 0.0 && !nei.contains(el)) {

									nei.add(el);

								}
							}
						}

					} else {
						current.setF(-1); /*
										 * if the cell is not in the route, set
										 * its potential to -1
										 */
					}

				}

				next.clear();

				next.addAll(nei);

				nei.clear();

				k++;
			}

			// reset fields and compute final static potential

			for (String cell : cells.keySet()) {

				Cell c = cells.get(cell);
				double potential = c.getF();

				c.setD(potential);
				c.setF(0.0);
				cells.put(cell, c);

			}

			for (String cell : cells.keySet()) {

				Cell c = cells.get(cell);
				c.addSFF(route, c.getD());
				cells.put(cell, c);
				c.setD(0.0);

			}

		}
	}

	/**
	 * handles the thread of the simulation to have correct concurrency
	 */

	public void simulate() {

		while (go) {

			//System.out.println("\n\ntime step: " + timestep);
			
			//long a = System.currentTimeMillis();
			updateBoard();

			if (visualized == true) {
				updateGrid();
				updateText();
			} else {
				updateText();
			}
			// printTimestep();
			checkEnding();

			timestep++;

			if (timestep % 100 == 0) {
				System.out.print("tau = " + timestep + ", ");
				//", dt = " + (System.currentTimeMillis() - a) + "ms ");
			}

			try {

				Thread.sleep(SleepLength); /*
											 * slows down the simulation to be
											 * able to visualize it
											 */

			} catch (InterruptedException e) {
				// if the simulation is interrupted by the user, do nothing
			}

		}
	}

	/**
	 * updates the board
	 */

	public void updateBoard() {

		emptySinks();

		LinkedList<String> list = projection();
		// Precomputes the receiving capacities to avoid a clearing problem

		Cell c = new Cell();
		for (String cid : list) {

			c = cells.get(cid);

			//System.out.print("cell id: " + cid + " ");
			
			c.computeReceivingCapacity();
			cells.put(cid, c);

		}

		updateCells(list);

	}

	/**
	 * projects the numbers of people of each group in the cells
	 * 
	 * @return the list of the movements to be considered in the actual movement
	 */

	public LinkedList<String> projection() {

		LinkedList<String> list = new LinkedList<String>();

		LinkedList<String> neiList = new LinkedList<String>();

		Cell c = new Cell();

		Cell neighbor = new Cell();

		Cell n = new Cell();

		Hashtable<String, Double> neiPotentials = new Hashtable<String, Double>();

		for (String cid : cells.keySet()) {

			c = cells.get(cid);
			
			//System.out.println("Cell id: " + cid);

			for (int i = 0; i < numberGroups; i++) {

				String route = groups[i].getRoute();

				ArrayList<LogicalGroup> people = c.getPeople();

				for (LogicalGroup in : people) {

					if (in.route.equals(route) && in.numberPeople > 0.0) {

						double ml = in.numberPeople;

						double sumPeople = c.getNumberPeople();

						// if there are people of a certain group in the cell

						neiList = c.getNeighbors();

						double currentSFF = c.getSFF(route);

						double currentPot = alpha * currentSFF - beta
								* c.computeSpeed(); // potential of the
													// current cell

						double totalPotDif = 0.0;

						for (String ne : neiList) {

							neighbor = cells.get(ne);

							if (neighbor.getSFF(route) > 0.0) {

								double neiSFF = neighbor.getSFF(route);
								double neiDFF = neighbor.computeSpeed();
								double neiPot = alpha * neiSFF - beta * neiDFF;

								neiPotentials.put(ne, neiPot);

								//if (neiSFF <= currentSFF) { // to avoid
															// excessive
															// spreading
								// commented out as might be in conflict with choice theory.

									// computes the total difference of
									// potential

									totalPotDif += Math
											.exp(-(neiPot - currentPot)); // discrete
																			// choice
																			// approach

								//}
							}

						}

						for (String nei : neiPotentials.keySet()) {

							double D = 0.0;

							n = cells.get(nei);

							if (totalPotDif != 0.0
									//&& n.getSFF(route) <= currentSFF // to avoid excessive spreading
									// commented out as might be in conflict with choice theory.
									) {

								double neiPot = neiPotentials.get(nei);

								// turning proportion

								D = Math.exp(-(neiPot - currentPot))
										/ totalPotDif;

							}

							if (D > 0.0) {

								// computes the weighed sending flow
								double sendingFlow = 0;
								
								double tolerance = 1e-9;
								 
								 if (ml > sumPeople && //ml is exceeds sumPeople
										 sumPeople/ml <= 0.99 //error is larger than 1%
										 ){
									 if ( ! (-tolerance < ml && ml < tolerance)  && //ml is non-negligible
									 ! (-tolerance < sumPeople && ml < tolerance) ){ // sumPeople is non-negligible)
										 System.out.println("Warning: Some people got lost: ml = " + ml +
												 ", sumPeople = " + sumPeople);
									 }
									 sendingFlow = D * ml;
								 }
								else {
									sendingFlow = Math.min(D * ml, D
											* (ml / sumPeople) * c.getQs());
								}							
								
								if (sendingFlow >= 0){
									//System.out.println("Desired sending flow " + cid + " -> " + n.getCid() + " (on " + in.route + ", dep: "
									//		+ in.startInterval + "): " + sendingFlow);
								}
								else {
									System.out.println("\nWarning: Sending flow has value NaN or is negative.");
									System.out.println("Desired sending flow " + cid + " -> " + n.getCid() + " (on " + in.route + ", dep: "
											+ in.startInterval + "): " + sendingFlow);
									System.out.println("Min(" + D * ml + "," + D
										* (ml / sumPeople) * c.getQs() + ")");
									System.out.println("D = " + D + ", ml = " + ml + ", sumPeople = " +
										sumPeople + ", Qs = " + c.getQs() );
								}
								

								LogicalGroup tmp = new LogicalGroup(in.route,
										in.startInterval, sendingFlow);

								
								
								n.setReceiving(cid, tmp);
								n.addProjectedOccupancy(sendingFlow);				

								cells.put(nei, n);
								cells.put(cid, c);

								list.add(nei);

							}

						}

						neiPotentials.clear();

					}

				}
			}

		}

		return list;

	}

	/**
	 * update the effective people of each group in each cell
	 * 
	 * @param list
	 */

	private void updateCells(LinkedList<String> list) {

		Cell c = new Cell();

		Cell proj = new Cell();

		Cell n = new Cell();

		ArrayList<LogicalGroup> receiving = new ArrayList<LogicalGroup>();

		for (String cid : list) {

			c = cells.get(cid);
			
			if (c.getProjectedOccupancy() < 0.0) {
				System.out.println("Warning: Negative projected occupancy");
			}
			
			if (c.getProjectedOccupancy() == Double.NaN) {
				System.out.println("Warning: NaN projected occupancy");
			}
			
			//System.out.println("cell id: " + cid + " projected inflow: " + c.getProjectedOccupancy());
			
			
			if (c.getProjectedOccupancy() > 0.0) {

				double ss = c.getSumSending();

				//System.out.println("cell id: " + cid + " sending capacity: " + ss);
				
				// no congestion in the cell

				if (ss <= c.getRc()) {

					for (String neighbor : c.getNeighbors()) {

						n = cells.get(neighbor);
						boolean skip = false;
						try {
							receiving.clear();
							receiving.addAll(c.getReceiving(neighbor));
						} catch (NullPointerException e) {
							skip = true;
						}
						if (!skip) {

							for (LogicalGroup rec : receiving) {

								// if the cell is going to receive people

								if (rec.numberPeople > 0.0) {

									int start = rec.startInterval;
									String route = rec.route;
									double d = rec.numberPeople;

									n.subPeople(rec);
									c.addPeople(rec);

									c.receivingDone(n.getCid(), rec.route,
											rec.startInterval);

									// sensor part

									if (sensors.containsKey(cid)) {

										LogicalGroup in = new LogicalGroup(
												route, start, d);

										int interval = in.startInterval;
										double weight = in.numberPeople;
										String id = String.valueOf(interval)
												.concat(route.concat(String
														.valueOf(weight)));

										Hashtable<String, Sensor> sensorsTmp = sensors
												.get(cid);

										if (sensorsTmp.containsKey(id)) {
											Sensor sensor = sensorsTmp.get(id);
											sensor.addWeight(weight);
											sensorsTmp.put(id, sensor);
										} else {
											Sensor sensor = new Sensor(
													interval, route, timestep,
													weight);

											sensorsTmp.put(id, sensor);
										}

										sensors.put(cid, sensorsTmp);
									}

								}
							}

						}
						cells.put(neighbor, n);
						cells.put(cid, c);
					}
				}

				// congestion in the cell

				else {

					for (String neighbor : c.getNeighbors()) {

						n = cells.get(neighbor);
						boolean skip = false;
						try {
							receiving.clear();
							receiving.addAll(c.getReceiving(neighbor));
						} catch (NullPointerException e) {
							skip = true;
						}

						if (!skip) {

							for (LogicalGroup rec : receiving) {

								/*
								 * if the total number of people wanting to
								 * enter the cell is superior to the current
								 * capacity => congestion
								 */

								if (ss > 0.0) {

									// ratio of the group i that can enter the
									// cell

									double res = (rec.numberPeople / ss)
											* c.getRc();

									LogicalGroup tmp = new LogicalGroup(
											rec.route, rec.startInterval, res);

									if (rec.numberPeople > 0.0) {

										int start = tmp.startInterval;
										String route = tmp.route;
										double d = tmp.numberPeople;

										n.subPeople(tmp);

										c.addPeople(tmp);

										c.receivingDone(n.getCid(), tmp.route,
												tmp.startInterval);

										// sensor part

										if (sensors.containsKey(cid)) {

											LogicalGroup in = new LogicalGroup(
													route, start, d);

											int interval = in.startInterval;
											double weight = in.numberPeople;
											String id = String
													.valueOf(interval)
													.concat(route.concat(String
															.valueOf(weight)));

											Hashtable<String, Sensor> sensorsTmp = sensors
													.get(cid);

											if (sensorsTmp.containsKey(id)) {
												Sensor sensor = sensorsTmp
														.get(id);
												sensor.addWeight(weight);
												sensorsTmp.put(id, sensor);
											} else {
												Sensor sensor = new Sensor(
														interval, route,
														timestep, weight);

												sensorsTmp.put(id, sensor);
											}

											sensors.put(cid, sensorsTmp);
										}

									}
								}
							}
						}

						cells.put(neighbor, n);

					}
				}
			}

			cells.put(cid, c);

		}

		// reset all projections

		for (String cid : list) {

			proj = cells.get(cid);
			proj.reinitializeProjections();

			cells.put(cid, proj);

		}

		fillSources();

	}

	/**
	 * updates the text files when the visualization is not enabled.
	 */

	public void updateText() {

		String s = "";
		String cids ="";
		String[] sGroup = new String[groups.length];

		String[] SFF = new String[groups.length];

		for (int i = 0; i < sGroup.length; i++) {
			sGroup[i] = new String();

			if (timestep == 0) {
				SFF[i] = new String();
			}
		}

		Cell c = new Cell();

		for (String a : cells.keySet()) {

			c = cells.get(a);
			
			if (timestep == 0) {
				cids = cids.concat(c.getCid());
				cids = cids.concat(" ");
			}
			
			
			s = s.concat(String.valueOf(c.getNumberPeople()));
			s = s.concat(" ");

			for (int i = 0; i < groups.length; i++) {

				sGroup[i] = sGroup[i].concat(String.valueOf(c
						.getGroupPeople(groups[i].getRoute())));
				sGroup[i] = sGroup[i].concat(" ");
				if (timestep == 0) {
					SFF[i] = SFF[i].concat(String.valueOf(c.getSFF(groups[i]
							.getRoute())));
					SFF[i] = SFF[i].concat(" ");
				}
			}

		}

		for (int i = 0; i < groups.length; i++) {

			String route = groups[i].getRoute();
			ArrayList<String> tmp = routeTextOutput.get(route);
			if (timestep == 0) {

				tmp.add(SFF[i]);
			}

			tmp.add(sGroup[i]);
			routeTextOutput.put(route, tmp);

		}
		if(timestep==0){
			textOutput.add(cids);
		}
		textOutput.add(s);
	}

	/**
	 * 
	 * send the relevant informations for the output to the grid
	 */

	public ArrayList<float[]> updateGrid() {

		ArrayList<float[]> upList = new ArrayList<float[]>();

		double sum = 0.0;

		Cell c = new Cell();

		for (String cid : cells.keySet()) {

			c = cells.get(cid);

			sum += c.getNumberPeople();

			float[] tmp = new float[14 + groups.length];

			System.arraycopy(c.getCoordinates(), 0, tmp, 0, 8);

			tmp[8] = (float) c.getNumberPeople();
			tmp[9] = (float) c.getArea();
			tmp[10] = (float) c.computeSpeed();
			tmp[11] = (float) c.getFlow();
			tmp[12] = (float) c.getQmax();
			tmp[13] = (float) c.getKc();

			for (int i = 0; i < groups.length; i++) {

				if (c.getGroupPeople(groups[i].getRoute()) > 0.0) {
					tmp[14 + i] = (float) (c.getGroupPeople(groups[i]
							.getRoute()) / c.getArea());
				}

			}

			upList.add(tmp.clone());

		}

		// ending condition
		if ((this.timestep > this.startingTime && sum < endingSum) ||
				this.timestep >= MaxTimeSteps) {

			this.go = false;
			this.end = true;

		}

		this.setChanged();
		this.notifyObservers(upList);

		return upList;
	}

	/**
	 * fill the source cells with the needed number of people
	 */

	public void fillSources() {

		for (int i = 0; i < groups.length; i++) {
			PhysicalGroup g = groups[i];
			if (timestep >= g.getStartTime()
					&& timestep < g.getStartTime()
							+ g.getStartRepartition().length) {

				Cell c = cells.get(sources[i]);

				LogicalGroup in = new LogicalGroup(g.getRoute(), timestep,
						g.getStartRepartition()[timestep - g.getStartTime()]);

				c.addPeople(in);

				cells.put(sources[i], c);
			}
		}

	}

	/**
	 * empty the sink cells of the people of the relevant group in it
	 */

	public void emptySinks() {

		for (int i = 0; i < groups.length; i++) {

			ArrayList<LogicalGroup> intervals = new ArrayList<LogicalGroup>();
			Cell c = cells.get(sinks[i]);
			String route = groups[i].getRoute();
			boolean skip = false;
			try {
				intervals.addAll(c.getPeople());

			} catch (NullPointerException e) {
				skip = true;
			}
			if (!skip) {
				for (LogicalGroup pe : intervals) {
					if (pe.route.equals(route)) {

						double d = pe.numberPeople;
						LogicalGroup toSub = new LogicalGroup(pe.route,
								pe.startInterval, pe.numberPeople);
						c.subPeople(toSub);

						groups[i].addTravelTime(pe.startInterval, timestep
								- pe.startInterval, d);

					}
				}
			}

			cells.put(sinks[i], c);

			groups[i].setEndTime(timestep);

		}
	}

	/**
	 * checks if the simulation can be stopped if the number of people in the
	 * simulation is close to zero. (in other words if only the residues are
	 * still there)
	 */

	public void checkEnding() {

		double sum = 0.0; // total number of people in the simulation

		ArrayList<String> l = new ArrayList<String>(cells.keySet());
		Collections.sort(l, cidComparator);

		// computes sum

		for (String a : l) {

			Cell c = cells.get(a);
			sum += c.getNumberPeople();
		}

		if (this.timestep % 100 == 0 && this.timestep > 0){
			System.out.print("totP: "+ String.format("%.3g", sum) +" | ");
		}
		
		
		
		// ending condition

		if ((this.timestep > this.startingTime && sum < endingSum) ||
				this.timestep >= MaxTimeSteps) {
			
			System.out.println("Stopped at "+this.timestep);
			this.go = false;
			this.end = true;

		}

	}

	/**
	 * loads the data used in the evaluation
	 * 
	 * @return true - if everything went well
	 */

	public boolean loadData(String filename_demand) {

		boolean valid = true;

		BufferedReader CSVFile;

		LinkedList<String[]> list = new LinkedList<String[]>();

		try {

			CSVFile = new BufferedReader(new FileReader(new File(
					filename_demand)));

			String row = CSVFile.readLine();

			while (row != null) {

				String[] data = row.split(",");
				list.add(data);

				row = CSVFile.readLine();

			}

			CSVFile.close();

			for (String[] el : list) {

				if (data.containsKey(el[1])) {

					Hashtable<Integer, Hashtable<Double, Double>> tmp = data
							.get(el[1]);

					Hashtable<Double, Double> p;

					if (tmp.containsKey(el[0])) {

						p = tmp.get(el[0]);
						p.put(Double.valueOf(el[2]), Double.valueOf(el[3]));

					} else {

						p = new Hashtable<Double, Double>();
						p.put(Double.valueOf(el[2]), Double.valueOf(el[3]));
					}

					tmp.put(Integer.valueOf(el[0]), p);
					data.put(el[1], tmp);

				} else {

					Hashtable<Integer, Hashtable<Double, Double>> tmp = new Hashtable<Integer, Hashtable<Double, Double>>();

					Hashtable<Double, Double> p = new Hashtable<Double, Double>();
					p.put(Double.valueOf(el[2]), Double.valueOf(el[3]));

					tmp.put(Integer.valueOf(el[0]), p);
					data.put(el[1], tmp);
				}

			}

		} catch (Exception e) {

			valid = false;
		}

		return valid;
	}

	/**
	 * adds all the parameters that couldn't be determined before
	 */

	public void addMissing() {

		sources = new String[numberGroups];
		sinks = new String[numberGroups];

		groups = new PhysicalGroup[numberGroups];

		int index = 0;

		for (String id : demand.keySet()) {

			PhysicalGroup g = demand.get(id);
			String[] route = id.split("-");
			g.setRouteArray(route);

			groups[index] = g;
			sources[index] = groups[index].getSource();
			sinks[index] = groups[index].getSink();

			index++;
		}

		for (int i = 0; i < groups.length; i++) {
			String route = groups[i].getRoute();
			routeTextOutput.put(route, new ArrayList<String>());
		}

		this.go = true;
	}

	/**
	 * adapts the scenario to the new parameters ( used by the calibration )
	 */

	public void setParameters(double[] newX) {

		this.timeInterval = deltaL / newX[0];

		this.setAlpha(newX[3]);

		this.setBeta(newX[4]);

		Enumeration<String> e = cells.keys();

		// the order is vf, gamma, kc

		while (e.hasMoreElements()) {

			Cell ce = cells.get(e.nextElement());

			ce.setMaxCapacity(newX[2] * ce.getArea());
			ce.setShape(newX[1]);

			double x0 = (ce.getShape() * ce.getArea())
					/ (0.3 * ce.getMaxCapacity());

			// n_0 = 0.3 N is a heuristic choice
			double xN = (ce.getShape() * ce.getArea()) / ce.getMaxCapacity();

			double a = 0.5 * (1 - x0) * Math.exp(xN - x0);
			double b = Math.pow(x0, 2) * Math.exp(xN - x0);
			double c = 1
					- (1 + x0 + (0.5 * Math.pow(x0, 2)) + (0.5 * Math
							.pow(x0, 3))) * Math.exp(xN - x0);

			double xopt = (-b + Math.sqrt(Math.pow(b, 2) - (4 * a * c)))
					/ (2 * a);

			// neglecting negative root

			double nopt = (newX[1] * ce.getArea()) / xopt;

			double qopt = nopt
					* (1 - Math.exp(-ce.getShape() * ce.getArea()
							* ((1 / nopt) - (1 / ce.getMaxCapacity()))));

			ce.setNmax(nopt);
			ce.setQmax(qopt);

			cells.put(ce.getCid(), ce);
		}
	}

	/**
	 * Cell ID comparator. Needed to order the cells in the text output
	 */

	public static Comparator<String> cidComparator = new Comparator<String>() {

		public int compare(String cid1, String cid2) {

			String s1 = cid1.toUpperCase();
			String s2 = cid2.toUpperCase();

			if (s1.length() < s2.length()) {

				return -1;

			} else if (s1.length() > s2.length()) {

				return 1;

			} else {

				return s1.compareTo(s2);

			}
		}

	};

	public void printTimestep() {

		ArrayList<String> l = new ArrayList<String>(cells.keySet());
		double result = 0;
		Collections.sort(l, cidComparator);
		String s = "";

		for (String a : l) {

			Cell c = cells.get(a);
			s = s.concat(c.getCid() + " = ");
			s = s.concat(String.valueOf(c.getNumberPeople()));
			s = s.concat(" ");
			result += c.getNumberPeople();

		}

		System.out.println(s);
		System.out.println(result + " " + timestep);
	}

	/**
	 * write the output text file containing a snapshot of the density in every
	 * cell at each timestep
	 */

	public void writeSpaceTime() {

		try {

			File dir = new File("output/routes/");

			dir.mkdirs();

			PrintWriter out = new PrintWriter("output/spaceTime.txt");

			for (String s : textOutput) {
				out.println(s);
			}

			out.close();

			for (int i = 0; i < groups.length; i++) {

				String route = groups[i].getRoute();

				ArrayList<String> tmp = routeTextOutput.get(route);

				PrintWriter outGroup = new PrintWriter("output/routes/" + route
						+ ".txt");

				for (String s : tmp) {
					outGroup.println(s);

				}

				outGroup.close();
			}

		} catch (FileNotFoundException e) {

			e.printStackTrace();

		}
	}

	/**
	 * writes two files : a file with the mean travel time of each group and one
	 * with the distribution of the travel times with their weight
	 */
	public void writeSimulation() {

		ArrayList<String> listTT = new ArrayList<String>();

		ArrayList<String> listTTD = new ArrayList<String>();

		for (int i = 0; i < groups.length; i++) {

			Hashtable<Integer, ArrayList<String>> s = groups[i].getTT();

			String route = groups[i].getRoute();

			for (Integer interval : s.keySet()) {

				ArrayList<String> si = s.get(interval);

				double mean = 0.0;
				double coef = 0.0;

				for (String c : si) {

					double t = Double.valueOf(c.split("/")[0]);
					double w = Double.valueOf(c.split("/")[1]);

					String s1 = "";

					s1 = s1.concat(Integer.toString(interval) + ",");
					s1 = s1.concat(route + ",");
					s1 = s1.concat(Double.toString(t) + ",");
					s1 = s1.concat(Double.toString(w) + "\n");

					listTTD.add(s1);

					mean += w * t;
					coef += w;

				}

				mean /= coef;

				String s1 = "";

				s1 = s1.concat(Integer.toString(interval) + ",");
				s1 = s1.concat(route + ",");
				s1 = s1.concat(Double.toString(mean) + ",");
				s1 = s1.concat(Double.toString(coef) + "\n");

				listTT.add(s1);

			}

			BufferedWriter CSVFileTT;

			BufferedWriter CSVFileTTD;

			try {

				File dir = new File("output/");

				dir.mkdirs();

				CSVFileTT = new BufferedWriter(new FileWriter(new File(
						"output/TT_avg.txt")));

				CSVFileTTD = new BufferedWriter(new FileWriter(new File(
						"output/TT_dist.txt")));

				for (String si : listTT) {
					CSVFileTT.write(si);
				}

				for (String si : listTTD) {
					CSVFileTTD.write(si);
				}

				CSVFileTT.close();

				CSVFileTTD.close();

			} catch (Exception e) {

				e.printStackTrace();

			}

		}

		if (!sensors.isEmpty()) {

			for (String cid : sensors.keySet()) {

				ArrayList<String> listSensor = new ArrayList<String>();
				Hashtable<String, Sensor> tmp = sensors.get(cid);

				for (String sid : tmp.keySet()) {

					listSensor.add(tmp.get(sid).getString());

				}

				BufferedWriter CSVFileSensor;

				try {

					CSVFileSensor = new BufferedWriter(new FileWriter(new File(
							"output/" + cid + ".txt")));

					File dir = new File("output/");

					dir.mkdirs();

					for (String s : listSensor) {
						CSVFileSensor.write(s);
					}

					CSVFileSensor.close();

				} catch (Exception e) {

					e.printStackTrace();

				}

			}
		}
	}

	/* Getters and setters */

	public int getTimestep() {
		return this.timestep;
	}

	public boolean getGo() {
		return go;
	}

	public boolean getEnd() {
		return end;
	}

	public void setEnd(boolean b) {
		this.end = b;
	}

	public void setGo(boolean go) {
		this.go = go;
	}

	public double getTimeInterval() {
		return timeInterval;
	}

	public String[] getGroupIDs() {
		String[] tmp = new String[groups.length];
		for (int i = 0; i < groups.length; i++) {
			tmp[i] = groups[i].getRoute();
		}
		return tmp;
	}

	public PhysicalGroup[] getGroups() {
		return this.groups;
	}

	public Hashtable<String, Hashtable<Integer, Hashtable<Double, Double>>> getData() {
		return data;
	}

	public float[] getBounds() {
		return bounds.clone();
	}

	public String getFilenameLayout() {
		return this.filenameLayout;
	}

	public double getAlpha() {
		return alpha;
	}

	public void setAlpha(double alpha) {
		this.alpha = alpha;
	}

	public double getBeta() {
		return beta;
	}

	public void setBeta(double beta) {
		this.beta = beta;
	}

}
