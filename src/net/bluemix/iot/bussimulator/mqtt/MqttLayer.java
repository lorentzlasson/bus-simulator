package net.bluemix.iot.bussimulator.mqtt;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import net.bluemix.iot.bussimulator.BusManager;
import net.bluemix.iot.bussimulator.model.Bus;
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
	
	private static final String pubTopic		=	"iot-2/type/ibmbus/id/%s/evt/position/fmt/json";
	private static final String allCmdTopic		=	"iot-2/type/+/id/+/cmd/+/fmt/json";
	private static final String allEvtTopic		=	"iot-2/type/+/id/+/evt/+/fmt/json";
	
	private Properties properties;
	private MqttClient client;
	private BusManager busManager;

	public MqttLayer(BusManager busManager) {
		properties = loadProperties();
		String org = properties.getProperty("org");
		String apiKey = properties.getProperty("apiKey");
		String apiToken = properties.getProperty("apiToken");
		
		String broker = "tcp://"+org+".messaging.internetofthings.ibmcloud.com:1883";
		String clientId = "a:"+org+":bussimulator-local";
		
		this.busManager = busManager;
		try {
			client = new MqttClient(broker, clientId, new MemoryPersistence());
			MqttConnectOptions options = new MqttConnectOptions();
			options.setUserName(apiKey);
			options.setPassword(apiToken.toCharArray());
			client.connect(options);
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
		if (part5.equals("evt")) {
			String event = topicParts[6];
			if (event.equals("position")) {
				System.out.printf("Position: %s\n", stringPayload);
			}
		}
		else if (part5.equals("cmd")) {
			String busDocId = Util.jsonValueFromAttribute(stringPayload, "\"busDocId\": \"");
			System.out.printf("Command: %s ", topicParts[6]);
			System.out.printf("busDocId: %s\n", busDocId);
		}
	}
	
	public void publishBusMovement(Bus bus) {
		String jsonBus = new Gson().toJson(bus.getLocation());
		MqttMessage message = new MqttMessage(Util.toDeviceFormat(jsonBus).getBytes());
		message.setQos(0);
		String topic = String.format(pubTopic, bus.getId());
		
		try {
			client.publish(topic, message);
		} catch (MqttException e) {
			e.printStackTrace();
		}
		
//		String jsonBus = "{\"busDocId\": \"BUS_DOC_ID_1\"}";
//		MqttMessage message = new MqttMessage(Util.toDeviceFormat(jsonBus).getBytes());
//		message.setQos(0);
//		String topic = String.format(pubTopic, bus.getId());
//		
//		try {
//			client.publish("iot-2/type/client/id/tracker/cmd/newbus/fmt/json", message);
//		} catch (MqttException e) {
//			e.printStackTrace();
//		}
	}
	
	private Properties loadProperties(){
		Properties properties = new Properties();
		InputStream inputStream = getClass().getClassLoader().getResourceAsStream("config.properties");
		try {
			properties.load(inputStream);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return properties;
	}
	
	public void connectionLost(Throwable arg0) {}
	public void deliveryComplete(IMqttDeliveryToken arg0) {}
}