package net.bluemix.iot.bussimulator;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

import net.bluemix.iot.bussimulator.connection.MqttLayer;
import net.bluemix.iot.bussimulator.connection.RestLayer;
import net.bluemix.iot.bussimulator.controller.UserController;
import net.bluemix.iot.bussimulator.data.DataLayer;
import net.bluemix.iot.bussimulator.exception.BusSimulatorException;
import net.bluemix.iot.bussimulator.model.Bus;
import net.bluemix.iot.bussimulator.model.BusRoute;
import net.bluemix.iot.bussimulator.model.softhouse.SensorEvent;
import net.bluemix.iot.bussimulator.model.softhouse.UserEvent;
import net.bluemix.iot.bussimulator.util.Util;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class BusSimulator{

	private static final long INTERVAL_THROTTLE = 1000;	// 1  sec
	private static final long INTERVAL_POSITION = 1000;	// 1  sec
	private static final long INTERVAL_SENSOR	= 10000;// 10 sec
	private static final long INTERVAL_USER		= 30000;// 30 sec
	private static final long INTERVAL_HEARTBEAT= 60000;// 60 sec
	private long lastUserEvent					= 0;
	private long lastSensorEvent				= 0;
	private long lastHeartbeatEvent				= 0;
	
	public static final String TYPE_ID 			= "vehicle";
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
		startBuses();
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
		System.out.println("New bus added: " + number);
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

	private void startBuses() {
		System.out.println("Starting busses");
		String[] userIds = restLayer.getUserIds();
		UserController userController = new UserController(userIds);
		while (true) {
			long now = System.currentTimeMillis();
			moveBuses();			
			publishBusPositions(now);
			publishBusSensors(now);
			publishBusUsers(now, userController);
			publishBusHeartbeats(now);
			
			try {
				Thread.sleep(INTERVAL_THROTTLE);
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

	private void publishBusPositions(long now){
		if (now - lastUserEvent > INTERVAL_POSITION) {
			for (Bus bus : buses) {
				mqtt.publishPosition(bus);
			}
		}
	}
	
	private void publishBusUsers(long now, UserController userController){
		if (now - lastUserEvent > INTERVAL_USER) {
			List<UserEvent> userEvents = userController.busStop(buses);
			for (UserEvent event : userEvents) {
				mqtt.publishUser(event);
			}
			lastUserEvent = now;
		}
	}
	
	private void publishBusSensors(long now){
		if (now - lastSensorEvent > INTERVAL_SENSOR) {
			Random random = new Random();
			for (Bus bus : buses) {
				SensorEvent event = new SensorEvent(bus);
				event.setId("5");
				double temprature = 20 + random.nextDouble() * 5;
				event.setValue(String.format("%.2f", temprature));
				mqtt.publishSensor(event);
			}
			lastSensorEvent = now;
		}
	}

	private void publishBusHeartbeats(long now) {
		if (now - lastHeartbeatEvent > INTERVAL_HEARTBEAT) {
			for (Bus bus : buses) {
				mqtt.publishActive(bus);
			}
			lastHeartbeatEvent = now;
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
	
	public static void main(String[] args) {
		new BusSimulator();
	}
}