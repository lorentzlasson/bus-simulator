package net.bluemix.iot.bussimulator.util;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import net.bluemix.iot.bussimulator.model.Coordinate;

public class CoordinateGenerator {
	
	public static void test() {
		Coordinate c1 = new Coordinate(0,0);
		Coordinate c2 = new Coordinate(1,0);
		Coordinate c3 = new Coordinate(2,1);
		double stepSize = 1000;
		Coordinate[] coordinates = {c1,c2,c3};
		List<Coordinate> path = createPath(coordinates, stepSize);
		for(Coordinate c : path) {
			System.out.println(c);
		}
	}
	
	public static void main(String[] args) {
		test();
	}
	
	public static List<Coordinate> createPath(Coordinate[] coordinates, double stepSizeMeters) {
		List<Vector> vectors = coordinatesToVectors(coordinates);
		double stepSizeGCS = metersToGCS(stepSizeMeters);
		List <Vector> resultVectors = connectTheDotsVectors(vectors, stepSizeGCS);
		return vectorsToCoordinates(resultVectors);
	}
	
	private static List<Coordinate> vectorsToCoordinates(List<Vector> vectors) {
		List<Coordinate> coordinates = new LinkedList<Coordinate>();
		for (Vector vector : vectors) {
			double lat = vector.getX();
			double lon = vector.getY();
			coordinates.add(new Coordinate(lat, lon));
		}
		return coordinates;
	}
	private static List<Vector> coordinatesToVectors(Coordinate[] coordinates) {
		List<Vector> vectors = new LinkedList<Vector>();
		for (Coordinate coordinate : coordinates) {
			double lat = coordinate.getLatitude();
			double lon = coordinate.getLongitude();
			vectors.add(new Vector(lat, lon));
		}
		return vectors;
	}

	private static List<Vector> connectTheDots(Vector start, Vector finish, double stepSize) {
		List<Vector> coordinates = new LinkedList<Vector>();
		Vector step = finish.subtract(start).normalized().multiply(stepSize);
		Vector vector = start.clone();
		while (vector.subtract(finish).size() >= stepSize) {
			coordinates.add(vector.clone());
			vector = vector.add(step);
		}
		return coordinates;
	}
	
	private static List<Vector> connectTheDotsVectors(List<Vector> coordinates, double stepSize) {
		List<Vector> result = new ArrayList<Vector>();
		for (int i = 0; i < coordinates.size() - 1; i++) {
			Vector start = coordinates.get(i);
			Vector finish = coordinates.get(i+1);
			result.addAll(connectTheDots(start, finish, stepSize));
		}
		result.add(coordinates.get(coordinates.size() - 1));
		return result;
	}
	
	private static double metersToGCS(double meters) {
		return meters/222222;
	}
}
