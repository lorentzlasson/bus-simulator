package net.bluemix.iot.bussimulator.model.event;

import net.bluemix.iot.bussimulator.model.Bus;

public class UserEvent extends SofthouseMessage {

	public static final String START = "start", END = "end";
	private String trip;
	
	public UserEvent(Bus bus) {
		super(bus);
	}

	public String getTrip() {
		return trip;
	}

	public void setTrip(String trip) {
		this.trip = trip;
	}
}
