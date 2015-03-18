package net.bluemix.iot.bussimulator.util;

public class Util {
	
	public static String toDeviceFormat(String data){
		String deviceFormatted = String.format("{\"d\": %s}", data);
		return deviceFormatted;
	}
}
