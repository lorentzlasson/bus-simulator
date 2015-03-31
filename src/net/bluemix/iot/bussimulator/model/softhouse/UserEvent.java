package net.bluemix.iot.bussimulator.model.softhouse;

import net.bluemix.iot.bussimulator.model.Bus;

public class UserEvent extends EventMessage {

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
