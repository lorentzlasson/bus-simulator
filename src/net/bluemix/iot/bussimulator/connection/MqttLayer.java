package net.bluemix.iot.bussimulator.connection;

import net.bluemix.iot.bussimulator.BusSimulator;
import net.bluemix.iot.bussimulator.model.Bus;
import net.bluemix.iot.bussimulator.model.softhouse.SensorEvent;
import net.bluemix.iot.bussimulator.model.softhouse.UserEvent;
import net.bluemix.iot.bussimulator.util.Util;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import com.google.gson.Gson;

public class MqttLayer implements MqttCallback {
	
	
	private static final String pubTopic		=	"iot-2/type/%s/id/%s/evt/%s/fmt/json";
	private static final String allCmdTopic		=	"iot-2/type/+/id/+/cmd/+/fmt/json";
	
	private MqttClient client;
	private BusSimulator busManager;

	public MqttLayer(BusSimulator busManager) {
		
		String org = BusSimulator.iotfCredentials.getProperty("org");
		String apiKey = BusSimulator.iotfCredentials.getProperty("apiKey");
		String apiToken = BusSimulator.iotfCredentials.getProperty("apiToken");
		
		String broker = "tcp://"+org+".messaging.internetofthings.ibmcloud.com:1883";
		String clientId = "a:"+org+":bussimulator";
		
		this.busManager = busManager;
		try {
			client = new MqttClient(broker, clientId, new MemoryPersistence());
			MqttConnectOptions options = new MqttConnectOptions();
			options.setUserName(apiKey);
			options.setPassword(apiToken.toCharArray());
			client.connect(options);
			client.setCallback(this);
			client.subscribe(allCmdTopic);
//			client.subscribe(allEvtTopic);
		} catch (MqttException e) {
			e.printStackTrace();
		}
	}

	public void messageArrived(String topic, MqttMessage message) throws Exception {		
		String stringPayload = new String(message.getPayload());
		String[] topicParts = topic.split("/");
		String part5 = topicParts[5];
		if (part5.equals("evt")) {
			String event = topicParts[6];
			if (event.equals("position")) {
				System.out.printf("Position: %s\n", stringPayload);
			}
		}
		else if (part5.equals("cmd")) {
			if (topicParts[6].equalsIgnoreCase("new_bus")) {
				String number = Util.jsonValueFromAttribute(stringPayload, "number");
				System.out.printf("Command: %s ", topicParts[6]);
				System.out.printf("route: %s\n", number);
				busManager.addBus(number);
			}
		}
	}
	
	public void publishPosition(Bus bus) {
		String jsonMessage = new Gson().toJson(bus.getLocation());
		MqttMessage message = new MqttMessage(Util.toDeviceFormat(jsonMessage).getBytes());
		message.setQos(0);
		String topic = String.format(pubTopic, BusSimulator.TYPE_ID, bus.getId(), "position");
		
		try {
			client.publish(topic, message);
		} catch (MqttException e) {
			e.printStackTrace();
		}
	}
	
	public void publishUser(UserEvent event) {
		String jsonMessage = new Gson().toJson(event);
		MqttMessage message = new MqttMessage(Util.toDeviceFormat(jsonMessage).getBytes());
		message.setQos(0);
		String topic = String.format(pubTopic, BusSimulator.TYPE_ID, event.getVehicleId(), "user");
		
		try {
			client.publish(topic, message);
		} catch (MqttException e) {
			e.printStackTrace();
		}
	}
	
	public void publishSensor(SensorEvent event) {
		String jsonMessage = new Gson().toJson(event);
		MqttMessage message = new MqttMessage(Util.toDeviceFormat(jsonMessage).getBytes());
		message.setQos(0);
		String topic = String.format(pubTopic, BusSimulator.TYPE_ID, event.getVehicleId(), "sensor");
		
		try {
			client.publish(topic, message);
		} catch (MqttException e) {
			e.printStackTrace();
		}
	}
	
	// TODO testing
	public void addNewBus() {
		String jsonBus = "{\"number\": \"3\"}";
		MqttMessage message = new MqttMessage(Util.toDeviceFormat(jsonBus).getBytes());
		message.setQos(0);
		
		try {
			client.publish("iot-2/type/client/id/tracker/cmd/new_bus/fmt/json", message);
		} catch (MqttException e) {
			e.printStackTrace();
		}
	}
	
	public void connectionLost(Throwable throwable) {
		throwable.printStackTrace();
		System.exit(0);
	}
	
	public void deliveryComplete(IMqttDeliveryToken arg0) {}
}