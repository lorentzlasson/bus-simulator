package net.bluemix.iot.bussimulator.connection;

import net.bluemix.iot.bussimulator.BusSimulator;
import net.bluemix.iot.bussimulator.model.Bus;
import net.bluemix.iot.bussimulator.model.event.MoveEvent;
import net.bluemix.iot.bussimulator.model.event.SensorEvent;
import net.bluemix.iot.bussimulator.model.event.UserEvent;
import net.bluemix.iot.bussimulator.util.Util;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class MqttLayer implements MqttCallback {
	
	
	private static final String pubTopic		=	"iot-2/type/%s/id/%s/evt/%s/fmt/json";
	private static final String pubTopicCmd		=	"iot-2/type/%s/id/%s/cmd/%s/fmt/json";
	private static final String allCmdTopic		=	"iot-2/type/+/id/+/cmd/+/fmt/json";
	private static final String allEvtTopic		=	"iot-2/type/+/id/+/evt/+/fmt/json";
	
	private MqttClient client;

	public MqttLayer() {
		
		String org = BusSimulator.iotfCredentials.getProperty("org");
		String apiKey = BusSimulator.iotfCredentials.getProperty("apiKey");
		String apiToken = BusSimulator.iotfCredentials.getProperty("apiToken");
		
		String broker = "tcp://"+org+".messaging.internetofthings.ibmcloud.com:1883";
		String clientId = "a:"+org+":bussimulator";
		
		try {
			client = new MqttClient(broker, clientId, new MemoryPersistence());
			MqttConnectOptions options = new MqttConnectOptions();
			options.setUserName(apiKey);
			options.setPassword(apiToken.toCharArray());
			client.connect(options);
			System.out.println("Connected to "+broker);
			client.setCallback(this);
			client.subscribe(allCmdTopic);
			client.subscribe(allEvtTopic);
		} catch (MqttException e) {
			e.printStackTrace();
		}
	}

	public void messageArrived(String topic, MqttMessage message) throws Exception {		
		String stringPayload = new String(message.getPayload());
		String[] topicParts = topic.split("/");
		String part5 = topicParts[5];
		JsonObject jsonData = Util.fromDeviceFormat(stringPayload);
		if (part5.equals("evt")) {
			String event = topicParts[6];
			if (event.equals("position")) {
				System.out.printf("Position: %s\n", jsonData);
			}
		}
		else if (part5.equals("cmd")) {
			System.out.printf("Command: %s ", topicParts[6]);
			if (topicParts[6].equalsIgnoreCase("new_bus")) {
				String number = jsonData.get("number").getAsString();
				BusSimulator.addBus(number);
			}
			else if (topicParts[6].equalsIgnoreCase("reset_bus")) {
				String id = topicParts[4];
				int busStopIndex = jsonData.get("busStopIndex").getAsInt();
				BusSimulator.placeBus(id, busStopIndex);
			}
		}
	}
	
	public void publishPosition(MoveEvent event) {
		String jsonMessage = new Gson().toJson(event);
		MqttMessage message = new MqttMessage(Util.toDeviceFormat(jsonMessage).getBytes());
		message.setQos(0);
		String topic = String.format(pubTopic, BusSimulator.TYPE_ID, event.getVehicleId(), "position");
		
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
	
	public void publishActive(Bus bus) {
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("vehicleId", bus.getId());
		jsonObject.addProperty("status", "1");
		
		String jsonMessage = jsonObject.toString();
		MqttMessage message = new MqttMessage(Util.toDeviceFormat(jsonMessage).getBytes());
		message.setQos(0);
		String topic = String.format(pubTopic, BusSimulator.TYPE_ID, bus.getId(), "status");
		
		try {
			client.publish(topic, message);
		} catch (MqttException e) {
			e.printStackTrace();
		}
	}
	
	public void publishTestCommand() {
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("busStopIndex", 0);
		
		String jsonMessage = jsonObject.toString();
		MqttMessage message = new MqttMessage(Util.toDeviceFormat(jsonMessage).getBytes());
		message.setQos(0);
		
		String topic = String.format(pubTopicCmd, BusSimulator.TYPE_ID, "bus3-1", "reset_bus");
		
		try {
			client.publish(topic, message);
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