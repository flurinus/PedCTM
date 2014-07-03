package model;

public class Zone {

	private int zid;
	private int[] coordinates;
	private double[] occupancy;
	private double area;

	public Zone(int zid, int[] coordinates, double area) {
		
		this.zid = zid;
		this.coordinates = coordinates;
		this.area=area;
		
	}

	public Integer getZid() {
		return this.zid;
	}
	
	public void setOccupancy(int interval, double d){
		this.occupancy[interval]=d;
		
	}
	
	public void initializeOccupancy(int length){
		this.occupancy= new double[length];
	}
	
	public int[] getCoordinates(){
		return this.coordinates;
	}

	public double[] getOccupancy() {
		
		return occupancy;
	}
	public double getArea() {
		
		return area;
	}


}
