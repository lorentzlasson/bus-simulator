package net.bluemix.iot.bussimulator.model;

public class User {
	
	private String id;
	private Bus onBus;

	public User(String id) {
		this.id = id;
	}
	
	public String getId(){
		return id;
	}

	public Bus getOnBus() {
		return onBus;
	}

	public void setOnBus(Bus onBus) {
		this.onBus = onBus;
	}

	public void setId(String id) {
		this.id = id;
	}
}
