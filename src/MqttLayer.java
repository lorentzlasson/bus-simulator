import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import com.google.gson.Gson;
import com.google.gson.JsonParser;

public class MqttLayer implements MqttCallback {

//	private static final String pubTopic =	"iot-2/type/simulator/id/%s/evt/move/fmt/json"; // placeholder for bus id
////	private static final String subTopic =	"iot-2/type/+/id/+/evt/admin/fmt/json";
//	private static final String subTopic =	"iot-2/type/+/id/+/evt/+/fmt/json";
//	private static final String broker = 	"tcp://scvxps.messaging.internetofthings.ibmcloud.com:1883";
//	private static final String clientId = 	"a:scvxps:bussimulator";
	
	private static final String pubTopic =	"iot-2/type/BusSimulator/id/%s/evt/bus.position/fmt/json";
	private static final String subTopic =	"iot-2/type/+/id/+/evt/+/fmt/json";
	private static final String broker = 	"tcp://oafilj.messaging.internetofthings.ibmcloud.com:1883";
	private static final String clientId = 	"a:oafilj:bussimulator";
	
	private static final JsonParser jsonParser = new JsonParser();
	
	private MqttClient client;
	private BusManager busManager;

	public MqttLayer(BusManager busManager) {
		this.busManager = busManager;
		try {
			client = new MqttClient(broker, clientId, new MemoryPersistence());
			MqttConnectOptions options = new MqttConnectOptions();
//			options.setUserName("a-scvxps-6ydvrvge3g");
//			options.setPassword("w3rEx@2lx65@g9-Rc(".toCharArray());
			options.setUserName("a-oafilj-g00uriursg");
			options.setPassword("Qq5j@1gVlRX-P4j7z*".toCharArray());
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
//		JsonElement jsonPayload = jsonParser.parse(stringPayload);
//		JsonObject jsonObject = jsonPayload.getAsJsonObject();
//		
//		jsonObject = jsonObject.getAsJsonObject("a");
//		String busNumber = jsonObject.get("busNumber").getAsString();
//		
//		busManager.addBus(busNumber);
	}
	
	public void publishBusMovement(Bus.BusLight bus) {
		String jsonBus = new Gson().toJson(bus);
		MqttMessage message = new MqttMessage(jsonBus.getBytes());
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
