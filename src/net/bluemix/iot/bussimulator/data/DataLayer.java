package net.bluemix.iot.bussimulator.data;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidParameterException;
import java.util.List;
import java.util.Properties;

import net.bluemix.iot.bussimulator.model.BusRoute;
import net.bluemix.iot.bussimulator.util.Util;

import org.apache.commons.io.IOUtils;

import com.cloudant.client.api.CloudantClient;
import com.cloudant.client.api.Database;
import com.cloudant.client.api.model.SearchResult;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class DataLayer {

	private Properties credentials;
	private Database db;

	public DataLayer() {
		credentials = loadVCapCredentials();
		if (credentials == null) 
			credentials = loadProperties();

		CloudantClient client = new CloudantClient(
				credentials.getProperty("url"),
				credentials.getProperty("username"),
				credentials.getProperty("password"));

		db = client.database("bus_routes", false);
	}

	public JsonArray getBusRegister() {
		InputStream inStream = db.find("busCounter");
		String busRegister = null;
		try {
			busRegister = IOUtils.toString(inStream);
		} catch (IOException e) {
			e.printStackTrace();
		}

		JsonObject jsonObject = new JsonParser().parse(busRegister).getAsJsonObject();
		JsonArray jsonArray = jsonObject.get("busCounts").getAsJsonArray();
		return jsonArray;
	}

	public void saveRoute(BusRoute busRoute){
		db.save(busRoute);
	}

	public BusRoute getRoute(String number) throws InvalidParameterException {
		List<SearchResult<BusRoute>.SearchResultRows> result = db
				.search("defaultDesign/number")
				.limit(1)
				.includeDocs(true)
				.querySearchResult("number:"+number, BusRoute.class)
				.getRows();

		if (result.size() == 0) throw new InvalidParameterException("Route "+number+" does not exist");
		
		BusRoute busRoute = result.get(0).getDoc();
		return busRoute;
	}

	private Properties loadVCapCredentials(){
		JsonObject jsonObj = Util.credentialsFromVCap("cloudantNoSQLDB");
		if(jsonObj == null) return null;
		Properties properties = new Properties();
		properties.setProperty("username", jsonObj.get("username").getAsString());
		properties.setProperty("password", jsonObj.get("password").getAsString());
		properties.setProperty("url", jsonObj.get("url").getAsString());
		return properties;
	}

	private Properties loadProperties(){
		Properties properties = new Properties();
		InputStream inputStream = getClass().getClassLoader().getResourceAsStream("cloudant.properties");
		try {
			properties.load(inputStream);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return properties;
	}
}