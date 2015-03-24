package net.bluemix.iot.bussimulator.data;
import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.bluemix.iot.bussimulator.model.BusRoute;
import net.bluemix.iot.bussimulator.model.Coordinate;
import net.bluemix.iot.bussimulator.util.CoordGenerator;
import net.bluemix.iot.bussimulator.util.Util;

import com.cloudant.client.api.CloudantClient;
import com.cloudant.client.api.Database;

public class DataLayer {

	private static Map<String, BusRoute> routes = new HashMap<String, BusRoute>();

	public static void initializeFromCsv(){

		BusRoute busRoute;

		// Bus number 1
		busRoute = new BusRoute(
				"1",
				Util.coordinatesFromCsv("/line1.csv"), 
				true);
		routes.put(busRoute.getNumber(), busRoute);

		// Bus number 2
		busRoute = new BusRoute(
				"2",
				Util.coordinatesFromCsv("/line2.csv"), 
				true);
		routes.put(busRoute.getNumber(), busRoute);
		
		// Bus number 3
		busRoute = new BusRoute(
				"3",
				Util.coordinatesFromCsv("/line3cr.csv"),
				false);

		routes.put(busRoute.getNumber(), busRoute);
	}
	
	public static void initializeFromCloudant(){
		CloudantClient client = new CloudantClient(
				"https://1505a557-0dfb-47ee-bbdd-b2cf1e770c65-bluemix:fc43906f40140a60d8b37c6e3e36bbb4b6ffcc97615b2c62e425a26d703df8eb@1505a557-0dfb-47ee-bbdd-b2cf1e770c65-bluemix.cloudant.com",
				"1505a557-0dfb-47ee-bbdd-b2cf1e770c65-bluemix",
				"fc43906f40140a60d8b37c6e3e36bbb4b6ffcc97615b2c62e425a26d703df8eb");

		Database db = client.database("bus_routes", false);
		List<BusRoute> busRoutes = db.view("_all_docs").includeDocs(true).query(BusRoute.class);
		for (BusRoute busRoute : busRoutes) {
			List<Coordinate> path = CoordGenerator.createPathFromCheckpoints(busRoute.getCoordinates(), 2);
			busRoute.setCoordinates(Util.listToArray(path));
			routes.put(busRoute.getNumber(), busRoute);
		}
	}

	public static BusRoute getRoute(String number) throws InvalidParameterException{
		BusRoute busRoute = routes.get(number);
		if (busRoute == null) throw new InvalidParameterException("Route "+number+" does not exist");
		return busRoute;
	}
}