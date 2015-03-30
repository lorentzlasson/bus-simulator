package net.bluemix.iot.bussimulator;

public class Main {

	public static void main(String[] args) {
		BusManager busManager = new BusManager();
		busManager.startBuses();
	}
}
