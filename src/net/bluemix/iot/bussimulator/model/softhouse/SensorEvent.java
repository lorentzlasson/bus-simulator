package net.bluemix.iot.bussimulator.model.softhouse;

import net.bluemix.iot.bussimulator.model.Bus;

public class SensorEvent extends EventMessage {
	
	private String value;
	
	public SensorEvent(Bus bus) {
		super(bus);
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
}
