package net.bluemix.iot.bussimulator.model;

import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

import net.bluemix.iot.bussimulator.BusSimulator;
import net.bluemix.iot.bussimulator.model.event.UserEvent;
import net.bluemix.iot.bussimulator.util.Util;

public class Bus {

	public enum Direction { FORWARD, BACKWARD };
	private int stoppedTimer = BusSimulator.BUS_STOP_DURATION;

	private String id;
	private BusRoute busRoute;
	private int routeLocation;
	private Direction direction;
	private List<User> passengers;

	public Bus(String id, BusRoute busRoute) {
		this.id = id;
		this.busRoute = busRoute;
		this.passengers = new CopyOnWriteArrayList<User>();

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

	public void tick(){
		boolean atLastStation = routeLocation == busRoute.length() - 1;
		boolean atFirstStation = routeLocation == 0;
		boolean outOfBoundsBackward = atFirstStation && direction == Direction.BACKWARD;
		boolean outOfBoundsForward = atLastStation && direction == Direction.FORWARD;

		// Ensure correct direction
		if (outOfBoundsBackward){
			direction = Direction.FORWARD;
		}
		else if (outOfBoundsForward){
			direction = Direction.BACKWARD;
		}

		if (getLocation().isStation()) {
			if (stoppedTimer == BusSimulator.BUS_STOP_DURATION) {
				System.out.printf("%s stopped at %d\n", id, routeLocation);
				openDoors();
			}
			if (stoppedTimer <= 0) {
				move();
				stoppedTimer = BusSimulator.BUS_STOP_DURATION;
			}
			else {
				stoppedTimer--;
			}
		}
		else {
			move();
		}
	}

	private void move(){
		if(direction == Direction.FORWARD) routeLocation ++;
		else if(direction == Direction.BACKWARD) routeLocation--;
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

	private void openDoors(){
		letPassengersOff();
		letPassengersIn();
	}

	private void letPassengersOff(){
		//		for (int i = passengers.size() - 1; i >= 0; i--) {
		//			if (Util.probabilityHit(BusSimulator.LEAVE_BUS_PROB)) {
		//				User passenger = passengers.get(i);
		//				passenger.setOnBus(null);
		//				passengers.remove(passenger);
		//			}
		//		}

		for (User user: passengers) {
			if (Util.probabilityHit(BusSimulator.LEAVE_BUS_PROB)) {
				user.setOnBus(null);
				passengers.remove(user);
				UserEvent event = new UserEvent(this);
				event.setId(user.getId());
				event.setTrip(UserEvent.END);
				BusSimulator.publishUserEvent(event);
			}
		}
	}

	private void letPassengersIn(){
		List<User> busLine = BusSimulator.getBusLine();
		for (User user: busLine) {
			user.setOnBus(this);
			passengers.add(user);
			UserEvent event = new UserEvent(this);
			event.setId(user.getId());
			event.setTrip(UserEvent.START);
			BusSimulator.publishUserEvent(event);
		}
	}
}