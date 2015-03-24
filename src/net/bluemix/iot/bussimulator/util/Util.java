package net.bluemix.iot.bussimulator.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

import net.bluemix.iot.bussimulator.Main;
import net.bluemix.iot.bussimulator.model.Coordinate;

public class Util {
	
	public static String toDeviceFormat(String data){
		String deviceFormatted = String.format("{\"d\": %s}", data);
		return deviceFormatted;
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T[] listToArray(List<T> list) throws InvalidParameterException{	
		Class<?> elementClass = null;
		if (list.size() > 0) elementClass = list.get(0).getClass();
		else throw new InvalidParameterException("List is empty");
		T[] array = (T[]) Array.newInstance(elementClass, list.size());
		return list.toArray(array);
	}
	

	public static Coordinate[] coordinatesFromCsv(String path){
		BufferedReader br = null;
		String line = "";
		String cvsSplitBy = ",";		
		List<Coordinate> coordinateList = new ArrayList<Coordinate>();
		InputStream is = Main.class.getResourceAsStream(path);
		InputStreamReader isr = new InputStreamReader(is);
		try {
			br = new BufferedReader(isr);
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
	
	public static Coordinate[] coordinatesFromCrossRoadsCsv(String path){
		Coordinate[] checkPoints = coordinatesFromCsv(path);
		List<Coordinate> listPath = CoordGenerator.createPathFromCheckpoints(checkPoints, 1);
		Coordinate[] arrPath = Util.listToArray(listPath);
		return arrPath;				
	}
}
