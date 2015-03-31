package net.bluemix.iot.bussimulator.model.softhouse;

import java.util.Date;

import net.bluemix.iot.bussimulator.model.Bus;
import net.bluemix.iot.bussimulator.util.Util;

public class EventMessage {
	
	private String id;
	private MetaData metaData;
	
	public EventMessage(Bus bus) {
		metaData = new MetaData();
		metaData.lat = bus.getLocation().getLatitude();
		metaData.lon = bus.getLocation().getLongitude();
		metaData.vehicleId = bus.getId();
		metaData.line = bus.getNumber();
		metaData.time = Util.getNowAsISO8601();
		System.out.println(metaData.time);
	}
	
	public void setId(String id) {
		this.id = id;
	}

	public String getVehicleId() {
		return metaData.vehicleId;
	}

	class MetaData {
		private double lat;
		private double lon;
		private String time;
		private String vehicleId;
		private String line;
	}
}