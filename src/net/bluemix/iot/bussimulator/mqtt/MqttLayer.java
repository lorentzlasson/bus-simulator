package net.bluemix.iot.bussimulator.mqtt;

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
	
	private static final String pubTopic =	"iot-2/type/ibmbus/id/%s/evt/position/fmt/json";
	private static final String subTopic =	"iot-2/type/ibmbus/id/+/evt/+/fmt/json";
	private static final String broker = 	"tcp://scvxps.messaging.internetofthings.ibmcloud.com:1883";
	private static final String clientId = 	"a:scvxps:bussimulator-local";
	
	private MqttClient client;
	@SuppressWarnings("unused")
	private BusManager busManager;

	public MqttLayer(BusManager busManager) {
		this.busManager = busManager;
		try {
			client = new MqttClient(broker, clientId, new MemoryPersistence());
			MqttConnectOptions options = new MqttConnectOptions();
			options.setUserName("a-scvxps-5nmephb29h");
			options.setPassword("orcEFs!uMZibsO1Ml@".toCharArray());
			client.connect(options);
			client.setCallback(this);
			client.subscribe(subTopic);
		} catch (MqttException e) {
			e.printStackTrace();
		}
	}

	public void messageArrived(String topic, MqttMessage message) throws Exception {		
		String stringPayload = new String(message.getPayload());
		System.out.println(stringPayload);
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
	}
	
	public void connectionLost(Throwable arg0) {}
	public void deliveryComplete(IMqttDeliveryToken arg0) {}
}
