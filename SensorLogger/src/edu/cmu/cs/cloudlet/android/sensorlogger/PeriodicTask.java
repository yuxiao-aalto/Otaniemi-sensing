package edu.cmu.cs.cloudlet.android.sensorlogger;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.util.Log;


public class PeriodicTask extends Service {

	private static final String TAG = PeriodicTask.class.getSimpleName();
	
	private Timer mTimer_WiFi;
	public WifiManager mWiFiManager;
	private WiFiScanHandler mWiFiScanHandler;
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		Log.i(TAG, "Service creating");
		
		
		
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		
		Log.i(TAG, "Service destroying");
		
		if (mWiFiManager != null){
			mTimer_WiFi.cancel();
			mTimer_WiFi = null;
		}
	}
	
	
	//int interval: interval of scanning in seconds
	public void startWiFiScanning(int interval){

		mWiFiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		
		if (mWiFiManager != null){
			mTimer_WiFi = new Timer("NetworkScanningTimer");
			mTimer_WiFi.schedule(scanWiFiNetworks, 1000L, interval * 1000L);//every 60 seconds
		}
		
		
	}

	private TimerTask scanWiFiNetworks = new TimerTask() {
		@Override
		public void run() {
			Log.i(TAG, "Timer tasks running");	
			
			try {
				
				//Wi-Fi fingerprints
				//WifiInfo mWiFiInfo = mWiFiManager.getConnectionInfo();
				
				
				//String bssid = mWiFiInfo.getBSSID();
				//int iRssid = mWiFiInfo.getRssi();
				
				// List available networks
				if (mWiFiManager.startScan()){
					// Register Broadcast Receiver
					if (mWiFiScanHandler == null)
						mWiFiScanHandler = new WiFiScanHandler(mWiFiManager);

					registerReceiver(mWiFiScanHandler, new IntentFilter(
							WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
				}
					
				
			} catch (Throwable t) { 
				Log.e(TAG, "Failed to get Wi-Fi information", t);
			}
		}
	};

	
}
