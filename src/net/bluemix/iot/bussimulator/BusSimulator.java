package net.bluemix.iot.bussimulator;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

import net.bluemix.iot.bussimulator.connection.MqttLayer;
import net.bluemix.iot.bussimulator.connection.RestLayer;
import net.bluemix.iot.bussimulator.data.DataLayer;
import net.bluemix.iot.bussimulator.exception.BusSimulatorException;
import net.bluemix.iot.bussimulator.model.Bus;
import net.bluemix.iot.bussimulator.model.BusRoute;
import net.bluemix.iot.bussimulator.model.User;
import net.bluemix.iot.bussimulator.model.event.MoveEvent;
import net.bluemix.iot.bussimulator.model.event.SensorEvent;
import net.bluemix.iot.bussimulator.model.event.UserEvent;
import net.bluemix.iot.bussimulator.util.Util;

import com.google.gson.JsonObject;

public class BusSimulator{

	private static final long INTERVAL_THROTTLE 	= 1  * 1000;
	private static final long INTERVAL_POSITION 	= 1  * 1000;
	private static final long INTERVAL_SENSOR		= 10 * 1000;
	private static final long INTERVAL_HEARTBEAT	= 60 * 1000;
	private long lastUserEvent						= 0;
	private long lastSensorEvent					= 0;
	private long lastHeartbeatEvent					= 0;

	public static final String TYPE_ID 				= "vehicle";
	public static final int BUS_STOP_DURATION 		= 10;	// ticks
	public static final int LEAVE_BUS_PROB	 		= 20;	// percent
	public static final int ENTER_BUS_PROB	 		= 5;	// percent

	public static Properties iotfCredentials;
	public static Properties cloudantCredentials;

	private static MqttLayer mqtt;
	private static DataLayer dataLayer;
	private static RestLayer restLayer;
	private static List<Bus> buses;
	private static List<User> users;

	public BusSimulator() {
		loadCredentials();
		buses = new CopyOnWriteArrayList<Bus>();
		users = new CopyOnWriteArrayList<User>();
		restLayer = new RestLayer();
		mqtt = new MqttLayer();
		dataLayer = new DataLayer();
		initializeBuses();
		initializeUsers();
		startBuses();
	}

	private void initializeBuses() {
		String[] busIds = restLayer.getRegisteredBuses();
		for (String id : busIds) {
			String number = id.split("bus")[1].split("-")[0]; // between "bus" and "-"
			try {
				BusRoute busRoute = dataLayer.getRoute(number);
				buses.add(new Bus(id, busRoute));
			} catch (BusSimulatorException e) {
				e.printStackTrace();
			}
		}
	}

	private void initializeUsers(){
		String[] userIds = restLayer.getUserIds();
		for (String id: userIds) {
			users.add(new User(id));
		}
	}

	private void startBuses() {
		System.out.println("Starting busses");
		while (true) {
			long now = System.currentTimeMillis();
			tickBuses();			
			publishBusPositions(now);
			publishBusSensors(now);
			publishBusHeartbeats(now);

			try {
				Thread.sleep(INTERVAL_THROTTLE);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private void tickBuses(){
		for (Bus bus : buses) {
			bus.tick();
		}
	}

	private void publishBusPositions(long now){
		if (now - lastUserEvent > INTERVAL_POSITION) {
			for (Bus bus : buses) {
				MoveEvent event = new MoveEvent(bus);
				mqtt.publishPosition(event);
				//				System.out.println(bus.asLocation());
			}
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

	public static void addBus(String number) {
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

	public static List<User> getBusLine(){
		List<User> busLine = new CopyOnWriteArrayList<User>();
		for (User user : users) {
			boolean notOnBus = user.getOnBus() == null;
			if (notOnBus && Util.probabilityHit(ENTER_BUS_PROB)) {
				busLine.add(user);
			}
		}
		return busLine;
	}

	public static void publishUserEvent(UserEvent userEvent){
		System.out.printf("User %s %s bus %s\n", userEvent.getId(), userEvent.getTrip(), userEvent.getVehicleId());
		mqtt.publishUser(userEvent);
	}

	public static void main(String[] args) {
		new BusSimulator();
	}
}