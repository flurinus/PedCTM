package model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.LinkedList;

public class Heatmap {

	private HashMap<Integer, Zone> zones = new HashMap<Integer, Zone>();

	private int firstInterval = 100000;

	private int lastInterval = 0;

	// min x min y max x max y
	private int[] bounds;

	public Heatmap() {

		bounds = new int[4];
		bounds[0] = 100000;
		bounds[1] = 100000;
		bounds[2] = 0;
		bounds[3] = 0;
	}

	/**
	 * Load the file of the zones
	 * 
	 * @param f
	 *            the parameters file
	 * 
	 * @return true if the parameter file was valid and if the simulation can
	 *         begin
	 */
	public boolean loadZones(File f) {

		boolean valid = true;

		BufferedReader CSVFile;
		try {
			CSVFile = new BufferedReader(new FileReader(f));

			String row = CSVFile.readLine();

			while (row != null) {
				String[] data = row.split(",");
				if (data.length == 9) {
					int[] coo = { (int) (Double.parseDouble(data[1])),
							(int) (Double.parseDouble(data[2])),
							(int) (Double.parseDouble(data[3])),
							(int) (Double.parseDouble(data[4])),
							(int) (Double.parseDouble(data[5])),
							(int) (Double.parseDouble(data[6])),
							(int) (Double.parseDouble(data[7])),
							(int) (Double.parseDouble(data[8])) };

					for (int j = 0; j < coo.length; j++) {
						if (j % 2 == 0) {
							if (coo[j] < bounds[0]) {
								bounds[0] = coo[j];
							}
							if (coo[j] > bounds[2]) {
								bounds[2] = coo[j];
							}
						} else {
							if (coo[j] < bounds[1]) {
								bounds[1] = coo[j];
							}
							if (coo[j] > bounds[3]) {
								bounds[3] = coo[j];
							}
						}
					}
					double area = computeArea(coo);
					Zone newZone = new Zone(Integer.parseInt(data[0]), coo,
							area);

					zones.put(Integer.parseInt(data[0]), newZone);

				}
				row = CSVFile.readLine();

			}

			CSVFile.close();

		} catch (Exception e) {
			valid = false;
		}

		return valid;

	}

	/**
	 * Load the file of the occupations
	 * 
	 * @param f
	 *            the parameters file
	 * 
	 * @return true if the parameter file was valid and if the simulation can
	 *         begin
	 */
	public boolean loadOccupations(File f) {

		boolean valid = true;

		BufferedReader CSVFile;

		LinkedList<String[]> list = new LinkedList<String[]>();

		try {
			// define number of intervals

			CSVFile = new BufferedReader(new FileReader(f));

			String row = CSVFile.readLine();

			while (row != null) {
				String[] data = row.split(",");
				list.add(data);
				int interval = Integer.parseInt(data[1]);

				if (interval + 1 > lastInterval) {
					lastInterval = interval + 1;
				}

				if (interval < firstInterval) {
					firstInterval = interval;
				}

				row = CSVFile.readLine();

			}

			CSVFile.close();

			while (!list.isEmpty()) {
				String[] data = list.remove();
				Zone tmp = zones.get(Integer.parseInt(data[0]));
				if (Integer.parseInt(data[1]) == firstInterval) {
					tmp.initializeOccupancy(lastInterval);
				}
				tmp.setOccupancy(Integer.parseInt(data[1]),
						Double.parseDouble(data[2]));
				zones.put(Integer.parseInt(data[0]), tmp);

			}

		} catch (Exception e) {

			valid = false;
		}

		return valid;

	}

	/**
	 * return the area of the zone. The zone must be convex and must have
	 * ordered vertices
	 * 
	 */
	public double computeArea(int[] coo) {
		double area = 0.0;
		double a = getLength(coo[0] / 55, coo[1] / 55, coo[2] / 55, coo[3] / 55);
		double b = getLength(coo[2] / 55, coo[3] / 55, coo[4] / 55, coo[5] / 55);
		double c = getLength(coo[4] / 55, coo[5] / 55, coo[6] / 55, coo[7] / 55);
		double d = getLength(coo[6] / 55, coo[7] / 55, coo[0] / 55, coo[1] / 55);

		double p = getLength(coo[0] / 55, coo[1] / 55, coo[4] / 55, coo[5] / 55);
		double q = getLength(coo[2] / 55, coo[3] / 55, coo[6] / 55, coo[7] / 55);

		area = 0.25 * Math.sqrt((4 * p * p * q * q)
				- (a * a - b * b + c * c - d * d));
		area /= 1000;
		return area;
	}

	public double getLength(int x1, int y1, int x2, int y2) {
		return Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
	}

	public HashMap<Integer, Zone> getZones() {
		return zones;
	}

	public int getFirstInterval() {
		return firstInterval;
	}

	public int getLastInterval() {
		return lastInterval;
	}

	public int[] getBounds() {
		return bounds;
	}
}
