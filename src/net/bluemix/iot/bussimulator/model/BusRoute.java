package net.bluemix.iot.bussimulator.model;

import java.util.List;

import net.bluemix.iot.bussimulator.BusSimulator;
import net.bluemix.iot.bussimulator.util.CoordinateGenerator;
import net.bluemix.iot.bussimulator.util.Util;

public class BusRoute {

	private String number;
	private Coordinate[] coordinates;
	
	public BusRoute(String number, Coordinate[] route) {
		this.coordinates = route;
		this.number = number;
	}
	
	public int length() {
		return coordinates.length;
	}
	
	public Coordinate location(int station) {
		return coordinates[station];
	}

	public String getNumber() {
		return number;
	}

	public Coordinate[] getCoordinates() {
		return coordinates;
	}

	public void setCoordinates(Coordinate[] coordinates) {
		this.coordinates = coordinates;
	}
	
	public void enhance(){
		List<Coordinate> path = CoordinateGenerator.createPath(coordinates, BusSimulator.STEP_DISTANCE);
		restoreStations(coordinates, path);
		coordinates = Util.listToArray(path);
	}
	
	private void restoreStations(Coordinate[] checkPoints, List<Coordinate> path){
		for (Coordinate checkPoint : checkPoints) {
			if (checkPoint.isStation()) {
				for (Coordinate coordinate : path) {
					boolean sameLat = checkPoint.getLatitude() == coordinate.getLatitude();
					boolean sameLon = checkPoint.getLongitude() == coordinate.getLongitude();
					if (sameLat && sameLon) {
						coordinate.setStation(true);
					}
				}
			}
		}
	}
}