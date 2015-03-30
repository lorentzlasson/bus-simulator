package net.bluemix.iot.bussimulator;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CopyOnWriteArrayList;

import net.bluemix.iot.bussimulator.connect.MqttLayer;
import net.bluemix.iot.bussimulator.connect.RestLayer;
import net.bluemix.iot.bussimulator.data.DataLayer;
import net.bluemix.iot.bussimulator.exception.BusSimulatorException;
import net.bluemix.iot.bussimulator.model.Bus;
import net.bluemix.iot.bussimulator.model.BusRoute;
import net.bluemix.iot.bussimulator.util.Util;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class BusSimulator{

	private static final long INTERVAL_UPDATE = 1000; // 1 second

	public static Properties iotfCredentials;
	public static Properties cloudantCredentials;

	private MqttLayer mqtt;
	private DataLayer dataLayer;
	private RestLayer restLayer;
	private List<Bus> buses;

	public BusSimulator() {
		loadCredentials();
		buses = new CopyOnWriteArrayList<Bus>();
		this.restLayer = new RestLayer();
//		restLayer.clearRegisterBuses();
		this.mqtt = new MqttLayer(this);
		this.dataLayer = new DataLayer();
		initializeBuses();
	}

	private void initializeBuses() {
		JsonArray registeredBuses = restLayer.getRegisteredBuses();
		for (int i = 0; i < registeredBuses.size(); i++) {
			JsonObject jsonBus = registeredBuses.get(i).getAsJsonObject();
			String id = jsonBus.get("id").getAsString();
			String number = id.split("bus")[1].split("-")[0]; // between "bus" and "-"
			
			try {
				BusRoute busRoute = dataLayer.getRoute(number);
				buses.add(new Bus(id, busRoute));
			} catch (BusSimulatorException e) {
				e.printStackTrace();
			}
		}
	}

	public void addBus(String number) {
		String id = restLayer.registerNewBus(number);
		BusRoute busRoute;
		try {
			busRoute = dataLayer.getRoute(number);
			Bus bus = new Bus(id, busRoute);
			buses.add(bus);
		} catch (BusSimulatorException e) {
			e.printStackTrace();
		}
	}

	public void startBuses() {
//		int count = 0;
		while (true) {
			// TODO for testing
//			count++;
//			if (count % 5 == 0) {
//				mqtt.addNewBus();
//			}

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
			mqtt.publishBusMovement(bus);
		}
	}

	private void loadCredentials() {
		String vCap = System.getenv("VCAP_SERVICES");
		if (vCap != null) {
			cloudantCredentials = loadVCapCredentials("cloudantNoSQLDB", "username", "password", "url");
			iotfCredentials = loadVCapCredentials("iotf-service", "org", "apiKey", "apiToken");
		}
		else {
			cloudantCredentials = loadProperties("cloudant");
			iotfCredentials = loadProperties("iotf");
		}
	}

	private Properties loadVCapCredentials(String service, String... propertyNames){
		JsonObject jsonObj = Util.credentialsFromVCap(service);
		Properties properties = new Properties();
		for (String prop : propertyNames) {
			properties.setProperty(prop, jsonObj.get(prop).getAsString());
		}
		return properties;
	}

	private Properties loadProperties(String name){
		Properties properties = new Properties();
		InputStream inputStream = getClass().getClassLoader().getResourceAsStream(name+".properties");
		try {
			properties.load(inputStream);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return properties;
	}
}