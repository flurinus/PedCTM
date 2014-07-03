package view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import model.Zone;

import processing.core.PApplet;
import processing.core.PGraphics;

/**
 * Handles the visualization of the simulation
 * 
 * For more info, please see www.processing.org‎
 * 
 * @author Thomas Muehlematter
 * 
 */

public class Grid extends PApplet {

	private static final long serialVersionUID = 1L;

	private int bg = color(255, 255, 255);

	private volatile ArrayList<float[]> coordinates;

	private LinkedHashMap<Integer, ArrayList<float[]>> outputColors;

	private boolean initialized;

	private float[] bounds;

	private int time;

	/**
	 * initializes the grid
	 */
	public void setup() {

		initialized = false;

		bounds = new float[4];

		outputColors = new LinkedHashMap<Integer, ArrayList<float[]>>();

		this.size(1350, 800);

		colorMode(HSB, 360, 100, 100);

	}

	/**
	 * draws the simulation on the screen (executed asynchronous)
	 */
	public void draw() {

		background(bg);

		if (initialized) {

			for (int i = 0; i < coordinates.size(); i++) {

				// catch the concurrency exception when it occurs

				try {

					double density = coordinates.get(i)[8]
							/ coordinates.get(i)[9];

					String s = "";

					if (density > 1E-3) {

						s = String.valueOf(density);

					} else {

						s = String.valueOf(0.0);

					}

					if (s.length() > 4) {

						s = s.substring(0, 3);

					}

					fill(getBAW(density));

					quad(coordinates.get(i)[0], coordinates.get(i)[1],
							coordinates.get(i)[2], coordinates.get(i)[3],
							coordinates.get(i)[4], coordinates.get(i)[5],
							coordinates.get(i)[6], coordinates.get(i)[7]);

					g.fill(0xFFFFFFFF);
					text(s,
							(coordinates.get(i)[0] + (coordinates.get(i)[0] + coordinates
									.get(i)[2]) / 2) / 2,
							(coordinates.get(i)[1] + coordinates.get(i)[7]) / 2);

				} catch (IndexOutOfBoundsException e) {

				}
			}

		} else {

			// reinitializes screen
			background(bg);

		}

	}

	public void setInitialized(boolean b) {

		this.initialized = b;

	}

	/**
	 * returns the color of the cell according to the NCHRP standards
	 * 
	 * @param density
	 * @return the corresponding color in hexadecimal. the first two bytes
	 *         represent the alpha channel and the following four the actual
	 *         color.
	 */
	private int getColorNCHRP(double density) {

		if (density == -1){
			return 0xAFAFAFAA; 
		}
	else if (density < 0.179) {
			return 0xff0000ff;
		} else if (density < 0.270) {
			return 0xff00ffff;
		} else if (density < 0.455) {
			return 0xff09ff00;
		} else if (density < 0.714) {
			return 0xfffcff00;
		} else if (density < 1.333) {
			return 0xfff15000;
		} else {
			return 0xfffb0000;
		}

	}

	/**
	 * returns the color between 0 and 1
	 * 
	 * value gray is a value between 0 and 255 that represents
	 * the "intensity" of the gray (fill(0) => black, fill(255) => white)
	 */
	private float getContGrayscaleCol(double density) {
		//returning a float is important to use the right version of fill (fill(int) expect an rgb value)
			if (density > 0.3) {
				return 0;
			} else {
				float coeff = (float)((0.3-density)/0.3);
				return coeff*0xff;
			}
		}
	
	
	/**
	 * returns the color between 0 and 1
	 * 
	 * value gray is a value between 0 and 255 that represents
	 * the "intensity" of the gray (fill(0) => black, fill(255) => white)
	 */
	private float getAlpha(double density) {
		//returning a float is important to use the right version of fill (fill(int) expect an rgb value)
			if (density > 0.4) {
				return (float) 255;
			} else {
				float coeff = (float)(density/0.4);
				return coeff*255;
			}
		}
	
	
	/**
	 * returns the color between 0 and 1
	 * 
	 * @param density
	 * @return the corresponding color in hexadecimal. the first two bytes
	 *         represent the alpha channel and the following four the actual
	 *         color.
	 */
	private int getColor01(double density) {

		if (density < 0.05) {
			return 0xff0000ff;
		} else if (density < 0.1) {
			return 0xff00ffff;
		} else if (density < 0.2) {
			return 0xff09ff00;
		} else if (density < 0.3) {
			return 0xfffcff00;
		} else if (density < 0.4) {
			return 0xfff15000;
		} else {
			return 0xfffb0000;
		}

	}

	/**
	 * returns the value of the cell according to NCHRP standards
	 * 
	 * @param density
	 * @return the corresponding value in hexadecimal. the first two bytes
	 *         represent the alpha channel
	 */
	private int getBAW(double density) {

		if (density < 0.179) {
			return 0xffffffff;
		} else if (density < 0.270) {
			return 0xffcccccc;
		} else if (density < 0.455) {
			return 0xff999999;
		} else if (density < 0.714) {
			return 0xff666666;
		} else if (density < 1.333) {
			return 0xff333333;
		} else {
			return 0x00000000;
		}

	}

	/**
	 * loads the grid
	 * 
	 * @param arrayList
	 *            - data list
	 * @param timestep
	 *            - current timestep
	 * @param b
	 *            - bounds
	 * 
	 * @param onTheFly
	 *            - decides if the programs needs to output the pictures after a
	 *            few timesteps
	 */
	public void loadGrid(ArrayList<float[]> arrayList, int timestep, float[] b,
			boolean onTheFly) {

		background(bg);

		coordinates = new ArrayList<float[]>();
		coordinates.addAll(arrayList);

		if (timestep == 0) {

			bounds = b.clone();

		}

		ArrayList<float[]> tmp = new ArrayList<float[]>();
		tmp = new ArrayList<float[]>();

		for (float[] cell : arrayList) {

			tmp.add(cell.clone());

		}

		for (float[] cell : tmp) {

			for (int i = 1; i < 9; i += 2) {
				cell[i] += (bounds[3] - bounds[1]) * timestep;
			}
		}

		outputColors.put(timestep, tmp);

		time = timestep;

		initialized = true;

		if (time > 0 && time % 100 == 0 && onTheFly) {
			this.writeSimulationmaps();
		}
	}

	/**
	 * writes the output of the simulation
	 */
	public void writeSimulationmaps() {

		int width = (int) (bounds[2] + bounds[0]);

		int height = (int) ((bounds[3] - bounds[1]) + 4 * bounds[1]);

		if (bounds[0] < 0 && bounds[1] < 0) {

			g.translate((int) (-bounds[0]), (int) (-bounds[1]));

		} else if (bounds[0] < 0 && bounds[1] > 0) {

			g.translate((int) (-bounds[0]), 0);

		} else if (bounds[0] > 0 && bounds[1] < 0) {

			g.translate(0, (int) (-bounds[1]));

		} else if (bounds[0] > 0 && bounds[1] > 0) {

			g.translate(0, 0);

		}

		// saves a simulation map

		for (int step : outputColors.keySet()) {

			// density
			PGraphics h = this.createGraphics(width, height, PApplet.JAVA2D);

			// speed
			PGraphics j = this.createGraphics(width, height, PApplet.JAVA2D);

			// flow
			PGraphics k = this.createGraphics(width, height, PApplet.JAVA2D);

			// non dimensional density
			PGraphics m = this.createGraphics(width, height, PApplet.JAVA2D);

			h.beginDraw();
			j.beginDraw();
			k.beginDraw();
			m.beginDraw();

			h.background(bg);
			j.background(bg);
			k.background(bg);
			m.background(bg);

			double totOccupation = 0;

			for (float[] coo : outputColors.get(step)) {

				double occupation = coo[8];
				totOccupation = totOccupation + occupation;
				double density = coo[8] / coo[9];
				double speed = coo[10];
				double flow = coo[11] / coo[12];
				double nondimdensity = density / coo[13];

				String sDensity = getString(density);
				String sSpeed = getString(speed);
				String sFlow = getString(flow);
				String sNondimdensity = getString(nondimdensity);

				h.fill(getColorNCHRP(density));

				h.quad(coo[0], coo[1] - (bounds[3] - bounds[1]) * step, coo[2],
						coo[3] - (bounds[3] - bounds[1]) * step, coo[4], coo[5]
								- (bounds[3] - bounds[1]) * step, coo[6],
						coo[7] - (bounds[3] - bounds[1]) * step);

				h.fill(0xFFFFFFFF);
				h.text(sDensity, (coo[0] + (coo[0] + coo[2]) / 2) / 2,
						(coo[1] + coo[7]) / 2 - (bounds[3] - bounds[1]) * step);

				j.fill(getColor01(1.0 - speed));

				j.quad(coo[0], coo[1] - (bounds[3] - bounds[1]) * step, coo[2],
						coo[3] - (bounds[3] - bounds[1]) * step, coo[4], coo[5]
								- (bounds[3] - bounds[1]) * step, coo[6],
						coo[7] - (bounds[3] - bounds[1]) * step);

				j.fill(0xFFFFFFFF);
				j.text(sSpeed, (coo[0] + (coo[0] + coo[2]) / 2) / 2,
						(coo[1] + coo[7]) / 2 - (bounds[3] - bounds[1]) * step);

				k.fill(getColor01(flow));

				k.quad(coo[0], coo[1] - (bounds[3] - bounds[1]) * step, coo[2],
						coo[3] - (bounds[3] - bounds[1]) * step, coo[4], coo[5]
								- (bounds[3] - bounds[1]) * step, coo[6],
						coo[7] - (bounds[3] - bounds[1]) * step);

				k.fill(0xFFFFFFFF);
				k.text(sFlow, (coo[0] + (coo[0] + coo[2]) / 2) / 2,
						(coo[1] + coo[7]) / 2 - (bounds[3] - bounds[1]) * step);

				//m.fill(getColor01(nondimdensity));
				//m.fill(getContGrayscaleCol(nondimdensity));
				m.fill(0, 0, 255, getAlpha(nondimdensity)); //RGB ALPHA

				m.quad(coo[0], coo[1] - (bounds[3] - bounds[1]) * step, coo[2],
						coo[3] - (bounds[3] - bounds[1]) * step, coo[4], coo[5]
								- (bounds[3] - bounds[1]) * step, coo[6],
						coo[7] - (bounds[3] - bounds[1]) * step);

				// NO LABELS
				//m.fill(0x00000000);
				//m.text(sNondimdensity, (coo[0] + (coo[0] + coo[2]) / 2) / 2,
				//		(coo[1] + coo[7]) / 2 - (bounds[3] - bounds[1]) * step);

			}

			// time stamp currently hard-coded, could be moved into a separate
			// file
			double deltaTfake = 2.7;
			double StartTimeSecFake = 27640;

			int StampXCoordFake = 25;
			int StampYCoordFake = 100;

			int StartTimeYearFake = -1;
			int StartTimeMonthFake = 1;
			int StartTimeDayFake = 22;

			int StartTimeYear = StartTimeYearFake;
			int StartTimeMonth = StartTimeMonthFake;
			int StartTimeDay = StartTimeDayFake;
			double StartTimeSec = StartTimeSecFake;

			double deltaT = deltaTfake;

			int StampXCoord = StampXCoordFake;
			int StampYCoord = StampYCoordFake;

			if (StartTimeYear > 2000) {

				double timeSecCont = StartTimeSec + step * deltaT;

				int timeHour = (int) timeSecCont / 3600;
				int timeMin = (int) (timeSecCont % 3600) / 60;
				double timeSec = timeSecCont % 60;

				double timeSecRounded = Math.round(timeSec * 100.0) / 100.0;
				double totOccupationRounded = Math.round(totOccupation * 100.0) / 100.0;

				h.fill(0x00000000);
				h.text(StartTimeYear + "-" + StartTimeMonth + "-"
						+ StartTimeDay + ", " + Integer.toString(timeHour)
						+ ":" + Integer.toString(timeMin) + ":"
						+ Double.toString(timeSecRounded) + ", N="
						+ Double.toString(totOccupationRounded), StampXCoord,
						StampYCoord);
			}

			h.endDraw();

			h.save("output/density_" + step + ".png");

			j.endDraw();

			j.save("output/speed_nd_" + step + ".png");

			k.endDraw();

			k.save("output/flow_nd_" + step + ".png");

			m.endDraw();

			m.save("output/dens_nd_" + step + ".png");

		}

		outputColors.clear();

	}

	/**
	 * outputs the static heatmaps loaded from tracking data
	 * 
	 * @param firstInterval
	 *            - first interval to draw
	 * @param lastInterval
	 *            - last interval to draw
	 * @param zones
	 * @param bounds
	 */
	public void writeHeatmaps(int firstInterval, int lastInterval,
			HashMap<Integer, Zone> zones, int[] bounds) {
		if (bounds[0] < 0 && bounds[1] < 0) {

			g.translate((int) (-bounds[0] / 100), (int) (-bounds[1] / 100));

		} else if (bounds[0] < 0 && bounds[1] > 0) {

			g.translate((int) (-bounds[0] / 100), 0);

		} else if (bounds[0] > 0 && bounds[1] < 0) {

			g.translate(0, (int) (-bounds[1] / 100));

		} else if (bounds[0] > 0 && bounds[1] > 0) {

			g.translate(0, 0);

		}

		for (int i = firstInterval; i < lastInterval; i++) {
			
			// add margin of 10% (presumably not always necessary!)    
			PGraphics g = this.createGraphics((int) (bounds[2]*1.1 / 100),
					(int) (bounds[3]*1.1 / 100), PApplet.JAVA2D); 
			g.beginDraw();

			g.background(bg);

			// saves a heatmap

			for (int key : zones.keySet()) {
				Zone z = zones.get(key);
				int[] coo = z.getCoordinates();

				System.out.print("interval i: " + i);
				System.out.print(", zone z: " + z.getZid());
				System.out.print(", occupancy: " + z.getOccupancy()[i]);
				System.out.print(", area: " + z.getArea() + "\n");
				
				double density = z.getOccupancy()[i]; // "Occupancy" is already density / z.getArea();
				g.fill(getColorNCHRP(density));
				g.quad(coo[0] / 100, coo[1] / 100, coo[2] / 100, coo[3] / 100,
						coo[4] / 100, coo[5] / 100, coo[6] / 100, coo[7] / 100);
			}

			g.endDraw();

			g.save("heatmaps/heatmap" + i + ".png");
		}
	}

	/**
	 * factorization method to return the string value of a double in the wanted
	 * form
	 * 
	 * @param a
	 *            - double to convert
	 * @return - the converted String
	 */
	public String getString(double a) {

		String s = "";

		if (a > 1E-3) {

			s = String.valueOf(a);

		} else {

			s = String.valueOf(0.0);

		}

		if (s.length() > 4) {

			s = s.substring(0, 3);
		}

		return s;
	}

}