package net.bluemix.iot.bussimulator.data;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import net.bluemix.iot.bussimulator.model.BusRoute;
import net.bluemix.iot.bussimulator.model.Coordinate;
import net.bluemix.iot.bussimulator.util.CoordGenerator;
import net.bluemix.iot.bussimulator.util.Util;

import com.cloudant.client.api.CloudantClient;
import com.cloudant.client.api.Database;
import com.google.gson.JsonObject;

public class DataLayer {

	private static Map<String, BusRoute> routes = new HashMap<String, BusRoute>();
	private Database db;
	private Properties credentials;
	
	public DataLayer() {
		credentials = loadVCapCredentials();
		if (credentials == null) 
			credentials = loadProperties();
				
		CloudantClient client = new CloudantClient(
				credentials.getProperty("url"),
				credentials.getProperty("username"),
				credentials.getProperty("password"));

		db = client.database("bus_routes", false);
		List<BusRoute> busRoutes = db.view("_all_docs").includeDocs(true).query(BusRoute.class);
		for (BusRoute busRoute : busRoutes) {
			List<Coordinate> path = CoordGenerator.createPathFromCheckpoints(busRoute.getCoordinates(), 2);
			busRoute.setCoordinates(Util.listToArray(path));
			routes.put(busRoute.getNumber(), busRoute);
		}
	}
	
	public void saveRoute(BusRoute busRoute){
		db.save(busRoute);
	}
	
	public BusRoute getRoute2(String number) throws InvalidParameterException {
		System.out.println(db.listIndices());
		return null;
	}

	public BusRoute getRoute(String number) throws InvalidParameterException {
		BusRoute busRoute = routes.get(number);
		if (busRoute == null) throw new InvalidParameterException("Route "+number+" does not exist");
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