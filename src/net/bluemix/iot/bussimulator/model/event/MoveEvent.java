package net.bluemix.iot.bussimulator.model.event;

import net.bluemix.iot.bussimulator.model.Bus;

@SuppressWarnings("unused")
public class MoveEvent {
	
	private transient String vehicleId;
	private double lat, lon;
	
	public MoveEvent(Bus bus) {
		vehicleId = bus.getId();
		lat = bus.getLocation().getLatitude();
		lon = bus.getLocation().getLongitude();
	}

	public String getVehicleId() {
		return vehicleId;
	}
}
