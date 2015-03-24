package net.bluemix.iot.bussimulator.util;

import java.util.ArrayList;
import java.util.List;

import net.bluemix.iot.bussimulator.model.Coordinate;

public class CoordGenerator {

	public static final Coordinate[] CHECK_POINTS = {
		new Coordinate(10.5, 100.5),
		new Coordinate(20.5, 100.5),
		new Coordinate(25.5, 120.5),
//		new Coordinate(50.5, 120.5)
	};
	
	public static List<Coordinate> createPathFromCheckpoints(Coordinate[] checkPoints, int depth) {
		List<List<Coordinate>> sections = new ArrayList<List<Coordinate>>();

		for (int i = 0; i < checkPoints.length - 1; i++) {
			List<Coordinate> section = new ArrayList<Coordinate>();
			section.add(checkPoints[i]);
			section.add(checkPoints[i+1]);
			section = padSection(section, depth);
			// Remove duplicate coords
			boolean lastSection = i == checkPoints.length - 2;
			if (!lastSection) section.remove(section.size()-1);
			
			sections.add(section);
		}

		List<Coordinate> path = new ArrayList<Coordinate>();
		for (List<Coordinate> section : sections) {
			for (Coordinate coordinate : section) {
				path.add(coordinate);
			}
		}
		return path;
	}

	private static List<Coordinate> padSection(List<Coordinate> list, int depth){
		List<Coordinate> paddedSection = new ArrayList<Coordinate>();
		for (int i = 0; i < list.size() - 1; i++) {
			Coordinate a = list.get(i);
			Coordinate b = list.get(i+1);
			Coordinate mid = getMiddle(a, b);
			paddedSection.add(a);
			paddedSection.add(mid);
			if (i == list.size() - 2) {
				paddedSection.add(b);
			}
		}
		if (depth > 1) {
			depth--;
			return padSection(paddedSection, depth);
		} else {
			return paddedSection;
		}
	}

	private static Coordinate getMiddle(Coordinate a, Coordinate b){
		double midLat = (a.getLatitude() + b.getLatitude()) / 2;
		double midLong = (a.getLongitude() + b.getLongitude()) / 2;
		return new Coordinate(midLat, midLong); 
	}

	public static void main(String[] args) {
		List<Coordinate> path = createPathFromCheckpoints(CHECK_POINTS, 2);
		Coordinate[] route = Util.listToArray(path);
		for (Coordinate coordinate : route) {
			System.out.println(coordinate);
		}
	}
}