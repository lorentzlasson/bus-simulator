package net.bluemix.iot.bussimulator.model;

public class Coordinate {
	
	private double latitude, longitude;
	private transient boolean station;
	
	public Coordinate(double latitude, double longitude) {
		this.latitude = latitude;
		this.longitude = longitude;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(long latitude) {
		this.latitude = latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(long longitude) {
		this.longitude = longitude;
	}
	
	public boolean isStation() {
		return station;
	}

	public void setStation(boolean station) {
		this.station = station;
	}

	public String toString() {
		return String.format("[%f; %f]", latitude, longitude);
	}
	
	public static double distance(Coordinate coord1, Coordinate coord2){
		double a = coord1.getLatitude() - coord2.getLatitude();
		double b = coord1.getLongitude() - coord2.getLongitude();
		return Math.sqrt(Math.pow(a, 2) +Math.pow(b, 2));
	}
}