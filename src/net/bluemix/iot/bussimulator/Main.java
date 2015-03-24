package net.bluemix.iot.bussimulator;

import net.bluemix.iot.bussimulator.data.DataLayer;
import net.bluemix.iot.bussimulator.model.Bus;
import net.bluemix.iot.bussimulator.model.BusRoute;

public class Main {

	public static void main(String[] args) {
		DataLayer.initializeFromCloudant();

		BusManager busManager = new BusManager();
		
		BusRoute busRoute = DataLayer.getRoute("3");
		busManager.addBus(new Bus("bus1-1", busRoute));
		
		busManager.startBuses();
	}
}
