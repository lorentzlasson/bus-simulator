package net.bluemix.iot.bussimulator.model;

import java.util.Random;
import net.bluemix.iot.bussimulator.MockData;

public class Bus {
	
	private enum Direction { FORWARD, BACKWARD };
	
	private static int counter = 1;
	private String id;
	private String number;
	private BusRoute route;
	private int station;
	private Direction direction;
	
	public Bus(String number) {
		id = String.valueOf(counter++);
		this.number = number;
		route = MockData.getRoute(number);
		// Set random route of number does match
		if (route == null) route = MockData.getRandomRoute(); 
		Random random = new Random();
		
		// TODO unless circular
		direction = Direction.values()[random.nextInt(Direction.values().length)];
		station = random.nextInt(route.length() - 1);
	}
	
	public void move(){
		boolean atLastStation = station == route.length() - 1;
		boolean atFirstStation = station == 0;
		boolean outOfBoundsBackward = atFirstStation && direction == Direction.BACKWARD;
		boolean outOfBoundsForward = atLastStation && direction == Direction.FORWARD;
		
		// Ensure correct direction
		if (!route.isCircular()) {
			if (outOfBoundsBackward){
				direction = Direction.FORWARD;
			}
			else if (outOfBoundsForward){
				direction = Direction.BACKWARD;
			}

			if(direction == Direction.FORWARD) station ++;
			else if(direction == Direction.BACKWARD) station--;	
		}
		else {
			// offsets station by 1
			if (outOfBoundsForward) {
				station = 0;
			}
			else if (outOfBoundsBackward) {
				station = route.length() - 1;
			}
			else {
				if(direction == Direction.FORWARD) station ++;
				else if(direction == Direction.BACKWARD) station--;					
			}
		}
	}
	
	public Coordinate getLocation(){
		return route.location(station);
	}
	
	public String getId() {
		return id;
	}
	
	public String toString() {
		String representaton = String.format("[Number: %s, Direction: %s]", number, direction);
		return representaton;
	}
	
	public String asLocation() {
		Coordinate location = getLocation();
		String representaton = String.format("[%s:%s]: (%f,%f)", number, id, location.getLatitude(), location.getLongitude());
		return representaton;
	}
	
	public Bus.BusLight getBusLight() {
		return new BusLight(this);
	}
	
	@SuppressWarnings("unused")
	public class BusLight {
		
		private String id, line;
		private double latitude, longitude;
		
		public BusLight(Bus bus) {
			this.id = bus.id;
			this.line = bus.number;
			Coordinate coord = bus.getLocation();
			this.latitude = coord.getLatitude();
			this.longitude = coord.getLongitude();
		}
		
		public String getId(){
			return id;
		}
	}
}
