package edu.cmu.cs.cloudlet.android.sensorlogger;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class SystemBootReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		//start the sensing service after the system is booted
		Intent serviceIntent = new Intent(SensingService.class.getName());
        context.startService(serviceIntent); 
	}

	
}
