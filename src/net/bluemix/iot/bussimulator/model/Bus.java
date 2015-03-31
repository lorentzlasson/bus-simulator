package net.bluemix.iot.bussimulator.model;

import java.util.Random;

public class Bus {
	
	public enum Direction { FORWARD, BACKWARD };
	
	private String id;
	private BusRoute busRoute;
	private int routeLocation;
	private Direction direction;
	
	public Bus(String id, BusRoute busRoute) {
		this.id = id;
		this.busRoute = busRoute;
		
		Random random = new Random();		
		direction = Direction.values()[random.nextInt(Direction.values().length)];
		routeLocation = random.nextInt(busRoute.length() - 1);
	}
	
	public void setDirection(Direction direction){
		this.direction = direction;
	}
	
	public void setRouteLocation(int routeLocation){
		this.routeLocation = routeLocation;
	}
	
	public void move(){
		boolean atLastStation = routeLocation == busRoute.length() - 1;
		boolean atFirstStation = routeLocation == 0;
		boolean outOfBoundsBackward = atFirstStation && direction == Direction.BACKWARD;
		boolean outOfBoundsForward = atLastStation && direction == Direction.FORWARD;
		
		// Ensure correct direction
		if (!busRoute.isCircular()) {
			if (outOfBoundsBackward){
				direction = Direction.FORWARD;
			}
			else if (outOfBoundsForward){
				direction = Direction.BACKWARD;
			}

			if(direction == Direction.FORWARD) routeLocation ++;
			else if(direction == Direction.BACKWARD) routeLocation--;	
		}
		else {
			// offsets station by 1
			if (outOfBoundsForward) {
				routeLocation = 0;
			}
			else if (outOfBoundsBackward) {
				routeLocation = busRoute.length() - 1;
			}
			else {
				if(direction == Direction.FORWARD) routeLocation ++;
				else if(direction == Direction.BACKWARD) routeLocation--;					
			}
		}
	}
	
	public Coordinate getLocation(){
		return busRoute.location(routeLocation);
	}
	
	public String getNumber(){
		return busRoute.getNumber();
	}
	
	public String getId() {
		return id;
	}
	
	public String toString() {
		String representaton = String.format("[Number: %s, Direction: %s]", busRoute.getNumber(), direction);
		return representaton;
	}
	
	public String asLocation() {
		Coordinate location = getLocation();
		String representaton = String.format("[%s:%s]: (%f,%f)", busRoute.getNumber(), id, location.getLatitude(), location.getLongitude());
		return representaton;
	}
}
