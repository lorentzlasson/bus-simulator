package net.bluemix.iot.bussimulator.connection;

import java.util.List;

import net.bluemix.iot.bussimulator.BusSimulator;
import net.bluemix.iot.bussimulator.util.Util;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

public class RestLayer {

	private static final String DEVICES_API = "https://internetofthings.ibmcloud.com/api/v0001/organizations";
	private static final String SOFTHOUSE_API = "http://mvdapiwar.mybluemix.net";
		
	public String registerNewBus(String number){
		int index = getNextBusIndex(number);
		if (index != -1) {
			String url = DEVICES_API+"/{org_id}/devices";
			HttpResponse<JsonNode> response = null;
			
			JsonObject body = new JsonObject();
			body.addProperty("type", BusSimulator.TYPE_ID);
			String id = String.format("bus%s-%d", number, index);
			body.addProperty("id", id);
			try {
				String org = BusSimulator.iotfCredentials.getProperty("org");
				String apiKey = BusSimulator.iotfCredentials.getProperty("apiKey");
				String apiToken = BusSimulator.iotfCredentials.getProperty("apiToken");
				
				response = Unirest.post(url)
						.basicAuth(apiKey, apiToken)
						.routeParam("org_id", org)
						.header("content-type", "application/json")
						.body(body.toString())
						.asJson();
				
			} catch (UnirestException e) {
				e.printStackTrace();
			}
			int status = response.getStatus();
			if (status == 200 || status == 201) {
				JsonObject jsonObject = new JsonParser().parse(response.getBody().toString())
				.getAsJsonObject();
				String regedId = jsonObject.get("id").getAsString();
				return regedId;
			}
			else {
				return null;
			}
		}
		return null;
	}
	
	public boolean clearRegisterBuses() {
		String[] busIds = getRegisteredBuses();
		for (int i = 0; i < busIds.length; i++) {
			String deviceId = busIds[i];
			String url = DEVICES_API+"/{org_id}/devices/{device_type}/{device_id}";
			HttpResponse<JsonNode> response = null;
			try {
				String org = BusSimulator.iotfCredentials.getProperty("org");
				String apiKey = BusSimulator.iotfCredentials.getProperty("apiKey");
				String apiToken = BusSimulator.iotfCredentials.getProperty("apiToken");
				
				response = Unirest.delete(url)
						.basicAuth(apiKey, apiToken)
						.routeParam("org_id", org)
						.routeParam("device_type", BusSimulator.TYPE_ID)
						.routeParam("device_id", deviceId)
						.asJson();
				
			} catch (UnirestException e) {
				e.printStackTrace();
			}
			
			int status = response.getStatus();
			if (status != 204) return false;
			
		}
		return true;
	}
	
	private int getNextBusIndex(String number){
		String[] buses = getRegisteredBuses();
		int limit = 100;
		List<Integer> freeIndexes = Util.indexList(limit);

		for (String id : buses) {
			if (id.contains("bus"+number)) {
				int index = Integer.parseInt(id.split("-")[1]);
				freeIndexes.remove(new Integer(index));
			}			
		}
		return freeIndexes.get(0);
	}
	
	public String[] getRegisteredBuses(){
		String url = DEVICES_API+"/{org_id}/devices/{device_type}";
		HttpResponse<JsonNode> response = null;
		try {
			String org = BusSimulator.iotfCredentials.getProperty("org");
			String apiKey = BusSimulator.iotfCredentials.getProperty("apiKey");
			String apiToken = BusSimulator.iotfCredentials.getProperty("apiToken");
			
			response = Unirest.get(url)
					.basicAuth(apiKey, apiToken)
					.routeParam("org_id", org)
					.routeParam("device_type", BusSimulator.TYPE_ID)
					.asJson();
			
		} catch (UnirestException e) {
			e.printStackTrace();
		}
		
		JsonArray jsonArray = new JsonParser().parse(response.getBody().toString())
				.getAsJsonArray();

		return jsonArrayToStringArray(jsonArray, "id");
	}
	
	public String[] getUserIds(){
		String url = SOFTHOUSE_API+"/user/allUsers";
		HttpResponse<JsonNode> response = null;
		try {
			response = Unirest.get(url).asJson();
		} catch (UnirestException e) {
			e.printStackTrace();
		}
		
		JsonArray jsonArray = new JsonParser().parse(response.getBody().toString())
				.getAsJsonArray();
		
		return jsonArrayToStringArray(jsonArray, "userId");
	}
	
	private String[] jsonArrayToStringArray(JsonArray jsonArray, String attr){
		String[] array = new String[jsonArray.size()];
		for (int i = 0; i < jsonArray.size(); i++) {
			array[i] = jsonArray.get(i)
					.getAsJsonObject()
					.get(attr)
					.getAsString();
		}
		return array;
	}
}