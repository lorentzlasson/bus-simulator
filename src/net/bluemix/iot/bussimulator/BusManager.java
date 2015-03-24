package net.bluemix.iot.bussimulator;

import java.util.ArrayList;
import java.util.List;

import net.bluemix.iot.bussimulator.model.Bus;
import net.bluemix.iot.bussimulator.mqtt.MqttLayer;

public class BusManager{

	private static final long INTERVAL_UPDATE = 1000; // 1 second
	private MqttLayer mqtt;
	private List<Bus> buses = new ArrayList<Bus>();

	public BusManager() {
		this.mqtt = new MqttLayer(this);
	}
	
	public void addBus(Bus bus) {
		buses.add(bus);
		System.out.println(bus);
	}

	public void startBuses() {
		while (true) {			
			moveBuses();			
			publishBuses();
			
			try {
				Thread.sleep(INTERVAL_UPDATE);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void moveBuses(){
		for (Bus bus : buses) {
			bus.move();
		}
	}
	
	private void publishBuses(){
		for (Bus bus : buses) {
//			System.out.println(bus.asLocation());
			mqtt.publishBusMovement(bus);
		}
	}
}
