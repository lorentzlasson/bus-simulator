package net.bluemix.iot.bussimulator.model;

public class BusRoute {

	private String number;
	private Coordinate[] route;
	private boolean circular;
	
	public BusRoute(String number, Coordinate[] route, boolean circular) {
		super();
		this.route = route;
		this.circular = circular;
		this.number = number;
	}
	
	public int length() {
		return route.length;
	}
	
	public Coordinate location(int station) {
		return route[station];
	}

	public String getNumber() {
		return number;
	}

	public boolean isCircular() {
		return circular;
	}
	
	
}
