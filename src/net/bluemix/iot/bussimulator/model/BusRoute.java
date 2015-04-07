package net.bluemix.iot.bussimulator.model;

public class BusRoute {

	private String number;
	private Coordinate[] coordinates;
	
	public BusRoute(String number, Coordinate[] route) {
		this.coordinates = route;
		this.number = number;
	}
	
	public int length() {
		return coordinates.length;
	}
	
	public Coordinate location(int station) {
		return coordinates[station];
	}

	public String getNumber() {
		return number;
	}

	public Coordinate[] getCoordinates() {
		return coordinates;
	}

	public void setCoordinates(Coordinate[] coordinates) {
		this.coordinates = coordinates;
	}
}