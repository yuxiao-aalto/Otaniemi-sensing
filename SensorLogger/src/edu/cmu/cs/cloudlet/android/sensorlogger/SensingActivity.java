package edu.cmu.cs.cloudlet.android.sensorlogger;

import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class SensingActivity extends Activity {

	private static final String TAG = SensingActivity.class.getSimpleName();
	private boolean bRunning = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
   
        bRunning = isServiceRunning(SensingService.class.getName());
        
        updateButtonView();
        
    }
    
    private boolean isServiceRunning(String name) {
    	
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        
        for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
        	
            if (name.equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
    
    private void updateButtonView(){
    	
    	Button startButton = (Button)findViewById(R.id.button1); 
        Button stopButton = (Button)findViewById(R.id.button3);
        
        if (bRunning){
        	startButton.setEnabled(false);
        	stopButton.setEnabled(true);
        } else{
        	startButton.setEnabled(true);
        	stopButton.setEnabled(false);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
        
    }

    public void startLoggingService(View view){
    	
    	Log.i(TAG, "Start button clicked");
    	
    	LocationManager mLocationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);

		if (mLocationManager != null){
			// getting GPS status
			boolean isGPSEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

			boolean isNetworkEnabled = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

			if (!isGPSEnabled && !isNetworkEnabled){
				showSettingsAlert();
			}
		}
		
		
    	if (!bRunning){
    	
    		if (startService(new Intent(SensingService.class.getName())) != null){
    			
    			bRunning = true;
    			
    			updateButtonView();
    			Toast.makeText(this, "Sensing service is now running in the background", Toast.LENGTH_LONG).show();
    	    	
    		}
    		
    	}else
    		Toast.makeText(this, "Sensing Service is already Running in the background", Toast.LENGTH_LONG).show();
    	
    	
    }
    
    /**called when the user clicks the "Stop Logging" button*/
    public void stopLoggingService(View view) {
    
       	//stop the logging service
    	if (stopService(new Intent(SensingService.class.getName()))){
    		
    		bRunning = false;
    		Log.i(TAG, "service is stopped");
    	}
    
    	//show notification
    	updateButtonView();
    	Toast.makeText(this, "Sensing service is stopped", Toast.LENGTH_LONG).show();
    	
    }
    
    /**called when the user clicks the "Statistics" button*/
    public void displayStatistics(View view){
    	
    	Toast.makeText(this, "This function is coming soon", Toast.LENGTH_LONG).show();
    }
    
    /**
     * Function to show settings alert dialog
     * On pressing Settings button will lauch Settings Options
     * */
    public void showSettingsAlert(){
    	
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
 
        final Context mContext = this;
        // Setting Dialog Title
        alertDialog.setTitle("GPS settings");
 
        // Setting Dialog Message
        alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");
 
        // On pressing Settings button
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {
            	 
            	Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            	 
                mContext.startActivity(intent);
            }
        });
 
        // on pressing cancel button
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            	dialog.cancel();
            }
        });
 
        // Showing Alert Message
        alertDialog.show();
    }
}
