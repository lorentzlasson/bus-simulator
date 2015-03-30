package net.bluemix.iot.bussimulator;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CopyOnWriteArrayList;

import net.bluemix.iot.bussimulator.connect.MqttLayer;
import net.bluemix.iot.bussimulator.connect.RestLayer;
import net.bluemix.iot.bussimulator.data.DataLayer;
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
		this.mqtt = new MqttLayer(this);
		this.dataLayer = new DataLayer();
		this.restLayer = new RestLayer();
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

	public void addBus(String number) {
		String id = restLayer.registerNewBus(number);
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