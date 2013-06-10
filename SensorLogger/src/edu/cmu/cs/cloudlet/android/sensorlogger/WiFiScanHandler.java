package edu.cmu.cs.cloudlet.android.sensorlogger;

import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.util.Log;

public class WiFiScanHandler extends BroadcastReceiver {
	WifiManager mWiFiManager;
	
	public WiFiScanHandler(WifiManager manager) {
	    super();
	    this.mWiFiManager = manager;
	 }
	
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		List<ScanResult> mAPList = this.mWiFiManager.getScanResults();
		
		for (ScanResult mAP : mAPList) {
			
			Log.i("SCAN", String.format("Time:%d; BSSID: %s; RSSI: %d; FREQ: %d",System.currentTimeMillis(), mAP.BSSID, mAP.level, mAP.frequency));
			
		}
	}

}
