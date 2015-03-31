package net.bluemix.iot.bussimulator.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

import net.bluemix.iot.bussimulator.model.Bus;
import net.bluemix.iot.bussimulator.model.User;
import net.bluemix.iot.bussimulator.model.softhouse.UserEvent;

public class UserController {

	private double actProbability = 0.3; // 30% prob
	private List<User> users;

	public UserController(String... ids) {
		users = new ArrayList<User>();
		for (String id : ids) {
			users.add(new User(id));
		}
	}

	public List<UserEvent> busStop(List<Bus> buses) {
		List<UserEvent> userEvents = new ArrayList<UserEvent>();
		List<User> elegibleUsers = new CopyOnWriteArrayList<User>(users);

		Random random = new Random();

		for (User user : elegibleUsers) {
			if (actProbability >= random.nextDouble()) {
				
				Bus bus = user.getOnBus();
				String trip = null;
				if (bus != null) {
					user.setOnBus(null);
					trip = UserEvent.END;
				}
				else {
					bus = buses.get(random.nextInt(buses.size()));
					user.setOnBus(bus);
					trip = UserEvent.START;
				}
				UserEvent event = new UserEvent(bus);
				event.setId(user.getId());
				event.setTrip(trip);
				userEvents.add(event);
				elegibleUsers.remove(user);
			}
		}
		return userEvents;		
	}
}