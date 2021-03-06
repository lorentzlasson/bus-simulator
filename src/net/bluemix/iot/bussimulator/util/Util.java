package net.bluemix.iot.bussimulator.util;

import java.lang.reflect.Array;
import java.security.InvalidParameterException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.TimeZone;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class Util {
	
	public static String toDeviceFormat(String data){
		String deviceFormatted = String.format("{\"d\": %s}", data);
		return deviceFormatted;
	}
	
	public static JsonObject fromDeviceFormat(String data){
		JsonObject jsonObject = new JsonParser().parse(data).getAsJsonObject();
		return jsonObject.get("d").getAsJsonObject();
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T[] listToArray(List<T> list) throws InvalidParameterException{
		Class<?> elementClass = null;
		if (list.size() > 0) elementClass = list.get(0).getClass();
		else throw new InvalidParameterException("List is empty");
		T[] array = (T[]) Array.newInstance(elementClass, list.size());
		return list.toArray(array);
	}
	
	public static JsonObject credentialsFromVCap(String serviceName){
		String vCap = System.getenv("VCAP_SERVICES");
		if(vCap == null) return null;
		JsonObject jsonObj = new JsonParser().parse(vCap).getAsJsonObject();
		jsonObj = jsonObj.get(serviceName).getAsJsonArray().get(0).getAsJsonObject();
		jsonObj = jsonObj.get("credentials").getAsJsonObject();
		return jsonObj;
	}
	
	public static String jsonValueFromAttribute(String json, String attr){
		String jsonAttr = String.format("\"%s\": \"", attr);
		int indexStart = json.indexOf(jsonAttr) + jsonAttr.length();
		int indexEnd = json.indexOf("\"", indexStart);
		String jsonValue = json.substring(indexStart, indexEnd);
		return jsonValue;
	}
	
	public static List<Integer> indexList(int length){
		List<Integer> list = new ArrayList<Integer>();
		for (int i = 1; i <= length; i++) {
			list.add(i);
		}
		return list;
	}
	
	public static String getNowAsISO8601(){
		TimeZone tz = TimeZone.getTimeZone("GMT+2:00");
	    DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
	    df.setTimeZone(tz);
	    String nowAsISO = df.format(new Date());
	    return nowAsISO;
	}
	
	public static boolean probabilityHit(int percent){
		double decPercent = (double) percent / 100;
		Random random = new Random();
		return decPercent >= random.nextDouble();
	}
}
