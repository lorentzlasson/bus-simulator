package net.bluemix.iot.bussimulator.model.event;

import net.bluemix.iot.bussimulator.model.Bus;
import net.bluemix.iot.bussimulator.util.Util;

public class SofthouseMessage {
	
	private String id;
	private MetaData metaData;
	
	public SofthouseMessage(Bus bus) {
		metaData = new MetaData();
		metaData.lat = String.valueOf(bus.getLocation().getLatitude());
		metaData.lon = String.valueOf(bus.getLocation().getLongitude());
		metaData.vehicleId = bus.getId();
		metaData.line = bus.getNumber();
		metaData.time = Util.getNowAsISO8601();
	}
	
	public String getId(){
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}

	public String getVehicleId() {
		return metaData.vehicleId;
	}

	@SuppressWarnings("unused")
	class MetaData {
		private String lat;
		private String lon;
		private String time;
		private String vehicleId;
		private String line;
	}
}