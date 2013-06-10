package edu.cmu.cs.cloudlet.android.sensorlogger;

import java.util.List;
import java.util.Map;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;



public class SensingService extends Service implements SensorEventListener{
	
	private static final String TAG = PeriodicTask.class.getSimpleName();

	//private enum SensorType {Accelerometer, LinearAccelerometer, GyroScope, GPS, WiFi, Cellular};

	private SensorManager mSensorManager;
	
	//private Map<SensorType, Sensor> mSubscribedSensorList;
	
	private Sensor mAccelerometer;
	private Sensor mGyroscope;
	private long lastUpdate;
	private LocationManager mLocationManager;
	private LocationListener mGPSListener; 
	private TelephonyManager mTeleManager;

	/*private static SensingService mInstance = null;
	
	public static SensingService getInstance() {
		
		if (mInstance == null) {
			
			synchronized (SensingService.class){
		
            if (mInstance == null) 
            	mInstance = new SensingService();
			}
		}
			
		return mInstance;
	}
	
	*/
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void onCreate() {
		
		super.onCreate();
		
	}
	
	@Override
    public int onStartCommand(Intent intent, int flags, int startId) {
      
		Log.i("LocalService", "Received start id " + startId + ": " + intent);
		
		mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
	    
	    if (mSensorManager == null){
	    	Log.i(TAG, "mSensorManager is null");
	    	return -1;
	    }

	   // printSensorInfo(true);
	    
		if (!startLinearAccelerometer(1000000)){
			
			startAccelerometer(1000000);//rate = 1 per second
			
		}
		
		startGPSTracking(1000, 1);
		
		PeriodicTask wifiScanning = new PeriodicTask();
		wifiScanning.startWiFiScanning(30);
		
		
		
        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
    }


	@Override
	public void onDestroy() {
		
	    super.onDestroy();
	    
		stopAccelerometer();
		stopGPSTracking();
	
	}

	 
	
	private boolean startAccelerometer(int rate){
         
		
		boolean acceExists = getPackageManager().hasSystemFeature(PackageManager.FEATURE_SENSOR_ACCELEROMETER);
	
		if (acceExists){
			
			mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
			
			//mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
			return mSensorManager.registerListener(this, mAccelerometer, rate);
			
		}else
			return false;
			
	}

	private boolean startLinearAccelerometer(int rate){
		
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD){
			Log.i(TAG, "Linear accelerometer requires API level 9 and above");
			return false;
		}
		
		mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
		
		return mSensorManager.registerListener(this, mAccelerometer, rate);
			
	}
	
	
	//stop collecting data
	private void stopAccelerometer(){
			
		Log.i(TAG, "stop accelerometer");
		
		if ((mSensorManager != null) && (mAccelerometer != null))
			mSensorManager.unregisterListener(this, mAccelerometer);
		
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		
		
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		// TODO Auto-generated method stub
		switch (event.sensor.getType()){
		
		case Sensor.TYPE_ACCELEROMETER:
			getAccelerometerReading(event);
			break;
		case Sensor.TYPE_LINEAR_ACCELERATION:
			getAccelerometerReading(event);
			break;
		case Sensor.TYPE_GYROSCOPE:
			break;
		case Sensor.TYPE_PROXIMITY:
			break;
		default:
			break;
		}
		
		
	}
	

	private void getAccelerometerReading(SensorEvent event){
	  
	    float x = event.values[0];
	    float y = event.values[1];
	    float z = event.values[2];

	    lastUpdate = System.currentTimeMillis();//local time in millisecond 
	    
	   // Log.i("Acce", String.format("time:%d, x: %f; y: %f; z: %f", lastUpdate, x, y, z));
		
	}

	//minTime: in millisecond; minDistance: in meters
	private void startGPSTracking(long minTime, float minDistance){

		mLocationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);

		if (mLocationManager == null){
			Log.i(TAG, "mLocationManager is null");
			return;
		}
		
		mGPSListener = new GPSListener();
		
		if (mGPSListener == null)
			return;
		
        // getting GPS status
        boolean isGPSEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        boolean isNetworkEnabled = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

             
        if (isGPSEnabled || isNetworkEnabled){
        	        	
        	for (String provider : mLocationManager.getProviders(true)){
        		
        		Log.i("Provider", provider);
        		
        	}
        		
        	Criteria mCriteria = new Criteria();
    		mCriteria.setCostAllowed(false);
    		mCriteria.setPowerRequirement(Criteria.POWER_LOW);
    		mCriteria.setAccuracy(Criteria.ACCURACY_COARSE);//network provider will be selected. Criteria.ACCURACY_FINE GPS
    		String selectedProviderName = mLocationManager.getBestProvider(mCriteria, true);
    		
    		Log.i("Selected", selectedProviderName);
    		
    		mLocationManager.requestLocationUpdates(selectedProviderName, minTime, minDistance, mGPSListener);
    		
    		Location mLocation;
    		
    		if (isNetworkEnabled){
    			mLocation = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
    		} else
    			mLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
    			
            if (mLocation != null) {
            	double latitude = mLocation.getLatitude();
            	double longitude = mLocation.getLongitude();
            	Log.i("GPS", String.format("last reading, latitude:%f, longitude:%f", latitude, longitude));
            } else
            	Log.i("GPS", "no readings available");
                		
        }
        
		
	}
	
	private void stopGPSTracking(){
		
		if ((mLocationManager != null) && (mGPSListener != null)){
		
			mLocationManager.removeUpdates(mGPSListener);
			Log.i(TAG, "Stop GPS tracking");
		}
		
	}
	
	
	//whether to get detailed information including minDelay, maxRange, resolution and etc
	//if not, only print the names
	private void printSensorInfo(boolean bDetails){
		
		String sResult = "";
		
		// List of Available Sensors
	    List<Sensor> mSensorList = mSensorManager.getSensorList(Sensor.TYPE_ALL);

	    for (Sensor mSensor: mSensorList){
	    	
	    	if (bDetails)
	    		sResult += String.format("Name:%s, maxRange:%f, Resolution>%f, Power:%f;\r\n", mSensor.getName(), mSensor.getMaximumRange(), mSensor.getResolution(), mSensor.getPower());
	    	else
	    		sResult += mSensor.getName();
	    	
	    }
		
	    Log.i(TAG, sResult);
	}
	
	private String getSensorHWInfo(int type){
		
		String mInfo = null;
		
		switch (type){
		
		case Sensor.TYPE_ACCELEROMETER:{
			
			boolean acceExists = getPackageManager().hasSystemFeature(PackageManager.FEATURE_SENSOR_ACCELEROMETER);
			
			if (acceExists){
				
				mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
				mInfo = String.format("Name:%s, maxRange:%f, Resolution>%f, Power:%f", mAccelerometer.getName(), mAccelerometer.getMaximumRange(), mAccelerometer.getResolution(), mAccelerometer.getPower());
				
			}
			
			break;
		}
		case Sensor.TYPE_GYROSCOPE:{

			boolean gyroscopeExists = getPackageManager().hasSystemFeature(PackageManager.FEATURE_SENSOR_GYROSCOPE);
			
			if (gyroscopeExists){
				
				mGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
				mInfo = String.format("Name:%s, maxRange:%f, Resolution>%f, Power:%f", mGyroscope.getName(), mGyroscope.getMaximumRange(), mGyroscope.getResolution(), mGyroscope.getPower());
				
			}
			
			break;
		}
		
		}
		
		return mInfo;
		
	}
	
	
	private String getCellularInfo(){
		
		mTeleManager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
		
		GsmCellLocation cellLocation = (GsmCellLocation) mTeleManager.getCellLocation();
		
		int cellID = cellLocation.getCid();
		
		
		//get location area code.
		int cellLac = cellLocation.getLac();
		
	    String deviceID = mTeleManager.getDeviceId();
	       

	    //String networkCountryIso = mTeleManager.getNetworkCountryIso();
	    //String networkOperator = mTeleManager.getNetworkOperator();
	    String sNetworkOperator = mTeleManager.getNetworkOperatorName();

	 
	    int networkType = mTeleManager.getNetworkType();
	    
	    String sNetworkType = "NA";
	    
	    switch (networkType) {
	    	case TelephonyManager.NETWORK_TYPE_UMTS:
	    		sNetworkType = "UMTS";
	            break;
	    	case TelephonyManager.NETWORK_TYPE_EDGE:
	    		sNetworkType = "EDGE";
	            break;
	        case TelephonyManager.NETWORK_TYPE_GPRS:
	        	sNetworkType = "GPRS";
	            break;   
	        case TelephonyManager.NETWORK_TYPE_HSDPA:
	        	sNetworkType = "HSDPA";
	        	break;
	        case TelephonyManager.NETWORK_TYPE_HSPA:
	        	sNetworkType = "HSPA";
	        	break;
	        case TelephonyManager.NETWORK_TYPE_1xRTT:
	        	sNetworkType = "1xRTT";
	        	break;
	        case TelephonyManager.NETWORK_TYPE_CDMA:
	        	sNetworkType = "CDMA";
	        	break;
	        case TelephonyManager.NETWORK_TYPE_IDEN:
	        	sNetworkType = "iDEN";
	        	break;
	        case TelephonyManager.NETWORK_TYPE_EVDO_0:
	        	sNetworkType = "EVDO_0";
	        	break;
	        case TelephonyManager.NETWORK_TYPE_EVDO_A:
	        	sNetworkType = "EVDO_A";
	        	break;
	        case TelephonyManager.NETWORK_TYPE_EVDO_B:
	        	sNetworkType = "EVDO_B";
	        	break;
	        default:
	        	sNetworkType = "NA";
	        	
	    }
	    
	    String result = String.format("CellID: %d; CellLoc: %i; DeviceID: %s, Operator: %s, Type:  %s", cellID, cellLac, deviceID, sNetworkOperator, sNetworkType);
	    
	    Log.i(TAG, result);
	    
	    return result;
	}
	

}
