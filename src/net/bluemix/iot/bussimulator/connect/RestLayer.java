package net.bluemix.iot.bussimulator.connect;

import java.util.List;

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
		
	public String registerNewBus(String number){
		int index = getNextBusIndex(number);
		if (index != -1) {
			String url = DEVICES_API+"/{org_id}/devices";
			HttpResponse<JsonNode> response = null;
			
			JsonObject body = new JsonObject();
			body.addProperty("type", "ibmbus");
			String id = String.format("bus%s-%d", number, index);
			body.addProperty("id", id);
			try {
				response = Unirest.post(url)
						.basicAuth("a-qchp0k-xgoi5y6hc3", "B@Plla(m9QX7ocN-Qg")
						.routeParam("org_id", "qchp0k")
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
	
	private int getNextBusIndex(String number){
		String url = DEVICES_API+"/{org_id}/devices/{device_type}";
		HttpResponse<JsonNode> response = null;
		try {
			response = Unirest.get(url)
					.basicAuth("a-qchp0k-xgoi5y6hc3", "B@Plla(m9QX7ocN-Qg")
					.routeParam("org_id", "qchp0k")
					.routeParam("device_type", "ibmbus")
					.asJson();
			
		} catch (UnirestException e) {
			e.printStackTrace();
		}
		
		JsonArray jsonArray = new JsonParser().parse(response.getBody().toString())
		.getAsJsonArray();
		
		int limit = 100;
		List<Integer> freeIndexes = Util.indexList(limit);
		
		for (int i = 0; i < jsonArray.size(); i++) {
			JsonObject jsonObject = jsonArray.get(i).getAsJsonObject();
			String id = jsonObject.get("id").getAsString();
			if (id.contains("bus"+number)) {
				int index = Integer.parseInt(id.split("-")[1]);
				freeIndexes.remove(new Integer(index));
			}			
		}
		return freeIndexes.get(0);
	}
}