package net.bluemix.iot.bussimulator;

public class Main {

	public static void main(String[] args) {
		BusManager busManager = new BusManager();
		busManager.startBuses();
		
//		DataLayer dal = new DataLayer();
//		dal.getRoute2("1");
	}
}
