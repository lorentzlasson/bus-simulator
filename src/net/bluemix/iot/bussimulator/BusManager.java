package net.bluemix.iot.bussimulator;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import net.bluemix.iot.bussimulator.data.DataLayer;
import net.bluemix.iot.bussimulator.model.Bus;
import net.bluemix.iot.bussimulator.model.BusRoute;
import net.bluemix.iot.bussimulator.mqtt.MqttLayer;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class BusManager{

	private static final long INTERVAL_UPDATE = 1000; // 1 second
	private MqttLayer mqtt;
	private DataLayer dataLayer;
	private List<Bus> buses;

	public BusManager() {
		buses = new CopyOnWriteArrayList<Bus>();
		this.mqtt = new MqttLayer(this);
		this.dataLayer = new DataLayer();
		initializeBuses();
	}

	private void initializeBuses() {
		JsonArray busCounts = dataLayer.getBusRegister();
		for (int i = 0; i < busCounts.size(); i++) {
			JsonObject entry = busCounts.get(i).getAsJsonObject();
			String number = entry.get("number").getAsString();
			BusRoute busRoute = dataLayer.getRoute(number);
			int count = entry.get("count").getAsInt();
			for (int j = 0; j < count; j++) {
				String id = String.format("bus%s-%d", number, (j+1));
				buses.add(new Bus(id, busRoute));
			}
		}
	}

	private int getBusIndex(String number){
		JsonArray busCounts = dataLayer.getBusRegister();
		for (int i = 0; i < busCounts.size(); i++) {
			JsonObject entry = busCounts.get(i).getAsJsonObject();
			if(entry.get("number").getAsString().equals(number)){
				return entry.get("count").getAsInt();
			}
		}
		return 0;
	}

	public void addBus(String number) {
		int busIndex = getBusIndex(number);
		String id = String.format("bus%s-%d", number, busIndex);
		BusRoute busRoute = dataLayer.getRoute(number);
		Bus bus = new Bus(id, busRoute);
		buses.add(bus);
	}

	public void startBuses() {
		int count = 0;
		while (true) {
			count++;

			moveBuses();			
			publishBuses();

			if (count% 5 == 0) {
				mqtt.addNewBus();
			}

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
			mqtt.publishBusMovement(bus);
		}
	}
}