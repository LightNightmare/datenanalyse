/* Written by FlorianB for SQLQueryLogTransformer
 * Using the example files for GeoLite API by Maxmind.
 * Receive the IP, look in the referenced local database file and return the location details
 */
package geolocation;

import java.io.IOException;

import com.maxmind.geoip.Location;
import com.maxmind.geoip.LookupService;

public class Geolocator {

	/**
	 * @param args
	 */
	public Location locateIP(String IP) {
		Location location = null;
		try {
			//IP = "91.89.58.242";
		    LookupService cl = new LookupService("GeoLiteCity.dat",
						LookupService.GEOIP_MEMORY_CACHE );
	            location = cl.getLocation(IP);
	            /*System.out.println(l1.countryCode);
	            System.out.println(l2);
		    System.out.println("countryCode: " + l2.countryCode +
	                               "\n countryName: " + l2.countryName +
	                               "\n region: " + l2.region +
	                               "\n regionName: " + regionName.regionNameByCode(l2.countryCode, l2.region) +
	                               "\n city: " + l2.city +
	                               "\n postalCode: " + l2.postalCode +
	                               "\n latitude: " + l2.latitude +
	                               "\n longitude: " + l2.longitude +
	                               "\n distance: " + l2.distance(l1) +
	                               "\n distance: " + l1.distance(l2) +
	 			       "\n metro code: " + l2.metro_code +
	 			       "\n area code: " + l2.area_code +
	                               "\n timezone: " + timeZone.timeZoneByCountryAndRegion(l2.countryCode, l2.region));
			*/
		    cl.close();
		}
		catch (IOException e) {
		    System.out.println("IO Exception");
		}
		return location;
	}

}
