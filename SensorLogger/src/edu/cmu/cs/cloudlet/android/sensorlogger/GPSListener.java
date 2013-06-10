package edu.cmu.cs.cloudlet.android.sensorlogger;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;


public class GPSListener implements LocationListener  {

	@Override
	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub	
		
		if (location.hasAltitude())
			Log.i("GPS", String.format("Longitude:%d; Latitude:%d; Altitude:%d;", location.getLongitude(), location.getLatitude(), location.getAltitude()));
		else
			Log.i("GPS", String.format("Longitude:%d; Latitude:%d;", location.getLongitude(), location.getLatitude()));
		
	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
		Log.i("GPS", provider+" Provider disabled");
	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		Log.i("GPS", provider+" Provider enabled");
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		Log.i("GPSListener", String.format("%s:%d", provider, status));
		
	}

}
