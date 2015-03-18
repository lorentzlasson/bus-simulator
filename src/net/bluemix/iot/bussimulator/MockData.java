package net.bluemix.iot.bussimulator;
import java.io.BufferedReader;
import java.io.FileReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import net.bluemix.iot.bussimulator.model.BusRoute;
import net.bluemix.iot.bussimulator.model.Coordinate;

public class MockData {

	private static Map<String, BusRoute> routes;

	public static void initialize(){

		routes = new HashMap<String, BusRoute>();
		BusRoute busRoute;
		
		// Bus number 1, index1
		busRoute = new BusRoute(
				"1",
				coordinatesFromCsv("/line1.csv"), 
				true);
		routes.put(busRoute.getNumber(), busRoute);
		
		// Bus number 2, index1
		busRoute = new BusRoute(
				"2",
				coordinatesFromCsv("/line2.csv"), 
				true);
		routes.put(busRoute.getNumber(), busRoute);
	}

	public static BusRoute getRoute(String number){
		return routes.get(number);
	}

	public static BusRoute getRandomRoute(){
		Random random = new Random();
		int randomIndex = random.nextInt(routes.keySet().size() - 1);
		BusRoute randomRoute = null;

		int i = 0;
		for (BusRoute route : routes.values()) {
			if (i == randomIndex) randomRoute = route;
			i++;
		}
		return randomRoute;
	}

	@SuppressWarnings("resource")
	private static Coordinate[] coordinatesFromCsv(String path){
		BufferedReader br = null;
		String line = "";
		String cvsSplitBy = ",";		
		List<Coordinate> coordinateList = new ArrayList<Coordinate>();
		URL url = MockData.class.getResource(path);
		try {
			br = new BufferedReader(new FileReader(url.getPath()));
			while ((line = br.readLine()) != null) {
				// use comma as separator
				String[] entry = line.split(cvsSplitBy);
				double latitude = Double.parseDouble(entry[0]);
				double longitude = Double.parseDouble(entry[1]);
				coordinateList.add(new Coordinate(latitude, longitude));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		Coordinate[] coordinates = coordinateList.toArray(new Coordinate[coordinateList.size()]);
		return coordinates;
	}
}