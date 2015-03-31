package net.bluemix.iot.bussimulator.data;

import java.util.List;

import net.bluemix.iot.bussimulator.BusSimulator;
import net.bluemix.iot.bussimulator.exception.BusSimulatorException;
import net.bluemix.iot.bussimulator.model.BusRoute;

import com.cloudant.client.api.CloudantClient;
import com.cloudant.client.api.Database;
import com.cloudant.client.api.model.SearchResult;

public class DataLayer {

	private Database db;

	public DataLayer() {
		CloudantClient client = new CloudantClient(
				BusSimulator.cloudantCredentials.getProperty("url"),
				BusSimulator.cloudantCredentials.getProperty("username"),
				BusSimulator.cloudantCredentials.getProperty("password"));

		db = client.database("bus_routes", false);
	}

	public void saveRoute(BusRoute busRoute){
		db.save(busRoute);
	}

	public BusRoute getRoute(String number) throws BusSimulatorException {
		List<SearchResult<BusRoute>.SearchResultRows> result = db
				.search("defaultDesign/number")
				.limit(1)
				.includeDocs(true)
				.querySearchResult("number:"+number, BusRoute.class)
				.getRows();

		if (result.size() == 0) throw new BusSimulatorException("Route "+number+" does not exist");
		
		BusRoute busRoute = result.get(0).getDoc();
		return busRoute;
	}
}