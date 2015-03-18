package net.bluemix.iot.bussimulator;

public class Main {

	public static void main(String[] args) {
		MockData.initialize();
		
		BusManager busManager = new BusManager();
		busManager.addBus("1");
		busManager.addBus("1");
		busManager.addBus("2");
		busManager.addBus("2");
		busManager.startBuses();
	}
}
