package net.bluemix.iot.bussimulator.model.event;

import net.bluemix.iot.bussimulator.model.Bus;

public class SensorEvent extends SofthouseMessage {
	
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
