package com.example.streamclient;

import java.io.File;
import java.io.IOException;

import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.streamclient.ControlChannel;
import com.example.streamclient.R;
import com.example.streamclient.R.id;

public class MainActivity extends Activity {
	private static final int SETTINGS_ID = Menu.FIRST;
	private static final int EXIT_ID = SETTINGS_ID+1;
	private static final int CHANGE_SETTING_CODE = 2;
	
	//data can be delivered using different protocols
	private static final int PROTOCOL_UDP = 0;
	private static final int PROTOCOL_TCP = PROTOCOL_UDP+1;
	private static final int PROTOCOL_RTPUDP = PROTOCOL_TCP+1;
	private static final int PROTOCOL_RTPTCP = PROTOCOL_RTPUDP+1;
	private int protocolIndex;//protocol used for streaming
	
	CameraRecording cameraRecorder;//thread for recording video
	StreamingThread streamServer;//thread for streaming video to remote proxy
	ControlChannel ctlChannel;//TCP connection for message exchange with the proxy
	
	private String remoteIP;//Proxy IP
	private int remotePort;//remote TCP port for message exchange
	private boolean enableCtlChannel;
	private SharedPreferences sharedPref;
	
	private boolean hasStarted;
	
	private Camera mCamera;
	private SurfaceView mPreview;//video preview
	private SurfaceHolder mHolder;
	//private CameraPreview mPreview;
	
	//use Android video recording intent for testing
	private boolean bIntent;
	private static final int CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE = 200;
	private Uri fileUri;
	
	private int videoEncoderIndex;
	private int videoOutputFormatIndex;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
       
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
     
        cameraRecorder = null;
        streamServer = null;
        ctlChannel = null;
        
        hasStarted = false;
        bIntent = false;
        
        setProtocolIndex(PROTOCOL_UDP);
        setDefaultPreferences();
        getPreferences();
        
        mPreview = new SurfaceView(this);
        if (mPreview != null){
        	mHolder = mPreview.getHolder();
        	mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        	
        	FrameLayout previewFrame = (FrameLayout) findViewById(id.camera_preview);
         	previewFrame.addView(mPreview);
        }
        else
        	System.out.println("mPreview is null");
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(0, SETTINGS_ID, 0, "Settings").setIcon(android.R.drawable.ic_menu_preferences);
        menu.add(0, EXIT_ID, 1, "Exit").setIcon(android.R.drawable.ic_menu_close_clear_cancel);
        
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	Intent intent;
      
    	switch (item.getItemId()) {
    	case SETTINGS_ID: 
    		intent = new Intent().setClass(this, SettingActivity.class);
    		startActivityForResult(intent, CHANGE_SETTING_CODE);
    		break;
    	case EXIT_ID:
    		cleanBeforeExit();
    		break;
    	}
      
      return super.onOptionsItemSelected(item);
    }
    
    public boolean initCtlChannel(){
    	
    	if ((ctlChannel != null) && (ctlChannel.isConnected() == false)){
    		try {
				ctlChannel.closeCtlChannel();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    	
    	if (ctlChannel == null){
    		
    		ctlChannel = new ControlChannel(remoteIP, remotePort);

    		TextView outputView = (TextView)findViewById(R.id.debugInfo);
    		outputView.setText("connected to message channel "+ remoteIP+ ":" + remotePort);
    		
    		return ctlChannel.initCtlChannel();
    	}
    	
    	return ctlChannel.isConnected();
    	
    }
    
    public boolean prepareCamera(){
    	
    	 if (this.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
             // Create an instance of Camera
    		try{
    			mCamera = Camera.open();
    		} catch (RuntimeException e){
    			System.out.println("cannot connect to camera service. Please restart the phone");
    			return false;
    		}
    		
         	if (mCamera == null)
         		return false;
         	
         	try {
 				mCamera.setPreviewDisplay(mHolder);
 				
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
         	
         	mCamera.startPreview();
         	
         	return true;
         	
         }else
        	 return false;
    }
    
    public void startStreaming(View view){  
    	System.out.println("start streaming clicked");
    	
    	int servicePort = 8080;
    	//show info on screen
    	TextView outputView = (TextView)findViewById(R.id.debugInfo);
    	
    	// start the Video Capture Intent
    	if (bIntent){
 	        //create new Intent
 	        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
 	        intent.putExtra(MediaStore.EXTRA_OUTPUT, Environment.getExternalStorageDirectory()+File.separator+"temp.mp4");  // set the image file name
 	        System.out.println(fileUri.toString());
 	        intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1); // set the video image quality to high
 	        startActivityForResult(intent, CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE);
 	        return;
        }
        
    	if (!hasStarted){
    		
    		hasStarted = true;
    		
    		//disable button 'start streaming'
    	}
    	else{
    		
    		
    		try {
    			if (cameraRecorder != null){
    				//to release camera in case the app crashed in the previous execution
    				cameraRecorder.stopCapturing();
    				cameraRecorder = null;
    			}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    		
    				
    		
    		
    	}
    	  	
    	if (enableCtlChannel && initCtlChannel()){
    		
    		servicePort = ctlChannel.getRemoteServicePort("streaming", "video");
    		//video will be sent to servicePort on remote machine
    		Log.v("stream", "servicePort:"+servicePort);
    		
    		if (servicePort == -1){
        		outputView.setText("Cannot get port number from server");
    			return;
    		}
    	}
    	
    	if (!prepareCamera()){
    		outputView.setText("Camera is not available. You may need to reboot your phone");
    		hasStarted = false;
    		return;
    	}
    	
    	//if the user click start, stop, and then start again
    	if ((cameraRecorder != null) && (cameraRecorder.getState() == Thread.State.TERMINATED))
    		cameraRecorder = null;
    	
    	if ((streamServer != null) && (streamServer.getState() == Thread.State.TERMINATED))
    		streamServer = null;
    	
    	
    	if (cameraRecorder == null)
    		cameraRecorder = new CameraRecording();
    	
    	cameraRecorder.setCamera(mCamera);
    	//surface handler is needed for MediaRecorder configuration
    	cameraRecorder.setPreviewSurface(mPreview.getHolder().getSurface());
    	
    	cameraRecorder.setVideoEncoder(videoEncoderIndex);
    	cameraRecorder.setVideoOutputFormat(videoOutputFormatIndex);
    	
    	//localLoop is initialized during streamServer initialization. so streamServer should be created before starting camerarecording
    	if (streamServer == null)
    		streamServer = new StreamingThread(protocolIndex, remoteIP, servicePort);
    	
    	System.out.println("streamserver state 3"+streamServer.getState());
    	
    	//streamServer run() includes localLoop.accept().so streamServer.start() should be invoked before cameraRecorder.start()
    	if (streamServer.getState() == Thread.State.NEW) 
    		streamServer.start();
    	
    	
    	if (cameraRecorder.getState() == Thread.State.NEW) 
    		cameraRecorder.start();
    	
    	
    	outputView.setText("Streaming starts");
    
    }
    
	public void stopStreaming(View view) throws IOException{
    	if (cameraRecorder != null){
    		cameraRecorder.stopCapturing();

        	if (cameraRecorder.getState() == Thread.State.TERMINATED)
        		cameraRecorder = null;
    	}
    	
    	if (streamServer != null){
    		streamServer.stopStreaming();
    		
    		if (streamServer.getState() == Thread.State.TERMINATED)
        		streamServer = null;
    	}
    		
   
    	hasStarted = false;
    	
    	if (mCamera != null){
    		
    		mCamera = null;
    	}
    	
    }
    
	 @Override
	 protected void onActivityResult(int requestCode, int resultCode, Intent data){
	    	
	    	if (resultCode == RESULT_OK){
	    		
	    		switch (requestCode){
	    		case CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE:
	    			Toast.makeText(this, "Video saved to:\n" +
	                        data.getData(), Toast.LENGTH_LONG).show();
	    			break;
	    		case CHANGE_SETTING_CODE:
	    			getPreferences();
	    			break;
	    		}//switch
	    	}//resultCode
	    	
	 }
	 
	 public void setDefaultPreferences(){
	       	//setDefaultValues will only be invoked if it has not been invoked     	
	    	PreferenceManager.setDefaultValues(this, R.xml.preferences, false);//this sentence used to be included in getPreference
	    	
	    	sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
	    	
	    	sharedPref.edit().putBoolean(SettingActivity.KEY_PROXY_ENABLED, true);
	    	sharedPref.edit().putString(SettingActivity.KEY_PROTOCOL_LIST, "UDP");
	    	sharedPref.edit().putString(SettingActivity.KEY_PROXY_IP, "128.2.213.18");
	    	sharedPref.edit().putInt(SettingActivity.KEY_PROXY_PORT, 5000);
	    	sharedPref.edit().putBoolean(SettingActivity.KEY_ENABLE_CTLCHANNEL, true);
	    	sharedPref.edit().putString(SettingActivity.KEY_VIDEO_ENCODER, "H263");
	    	sharedPref.edit().putString(SettingActivity.KEY_VIDEO_OUTPUT_FORMAT, "MPEG_4");
	    	sharedPref.edit().commit();
	 }
	    
	 public void getPreferences(){
	    	
	    	sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
	    	
	    	String sProtocol = sharedPref.getString(SettingActivity.KEY_PROTOCOL_LIST, "UDP");
	    	String[] sProtocolList = getResources().getStringArray(R.array.protocol_list);
	    	
	    	if (sProtocol.compareToIgnoreCase(sProtocolList[0]) == 0)
	    		setProtocolIndex(PROTOCOL_UDP);
	    	else if (sProtocol.compareToIgnoreCase(sProtocolList[1]) == 0)
	    		setProtocolIndex(PROTOCOL_TCP);
	    	else if (sProtocol.compareToIgnoreCase(sProtocolList[2]) == 0)
	    		setProtocolIndex(PROTOCOL_RTPUDP);
	    	else
	    		setProtocolIndex(PROTOCOL_RTPTCP);
	    	
	    	
	    	String sVideoFormat = sharedPref.getString(SettingActivity.KEY_VIDEO_OUTPUT_FORMAT, "MPEG_$");
	    	String[] sVideoFormatList = getResources().getStringArray(R.array.video_output_format_list);
	    	
	    	if (sVideoFormat.compareToIgnoreCase(sVideoFormatList[0]) == 0)
	    		videoOutputFormatIndex = MediaRecorder.OutputFormat.MPEG_4;
	    	else if (sVideoFormat.compareToIgnoreCase(sVideoFormatList[1]) == 0)
	    		videoOutputFormatIndex = MediaRecorder.OutputFormat.AMR_NB;
	    	else if (sVideoFormat.compareToIgnoreCase(sVideoFormatList[2]) == 0)
	    		videoOutputFormatIndex = MediaRecorder.OutputFormat.AMR_WB;
	    	else if (sVideoFormat.compareToIgnoreCase(sVideoFormatList[3]) == 0)
	    		videoOutputFormatIndex = MediaRecorder.OutputFormat.RAW_AMR;
	    	else if (sVideoFormat.compareToIgnoreCase(sVideoFormatList[4]) == 0)
	    		videoOutputFormatIndex = MediaRecorder.OutputFormat.THREE_GPP;
	    	else
	    		videoOutputFormatIndex = MediaRecorder.OutputFormat.DEFAULT;
	    	
	    	
	    	String sVideoEncoder = sharedPref.getString(SettingActivity.KEY_VIDEO_ENCODER, "H263");
	    	String[] sVideoEncoderlList = getResources().getStringArray(R.array.video_encoder_list);
	    	
	    	if (sVideoEncoder.compareToIgnoreCase(sVideoEncoderlList[0]) == 0)
	    		videoEncoderIndex = MediaRecorder.VideoEncoder.H263;
	    	else if (sVideoEncoder.compareToIgnoreCase(sVideoEncoderlList[1]) == 0)
	    		videoEncoderIndex = MediaRecorder.VideoEncoder.H264;
	    	else if (sVideoEncoder.compareToIgnoreCase(sVideoEncoderlList[2]) == 0)
	    		videoEncoderIndex = MediaRecorder.VideoEncoder.MPEG_4_SP;
	    	else
	    		videoEncoderIndex = MediaRecorder.VideoEncoder.DEFAULT;
	    		
	    	
	    	remoteIP = sharedPref.getString(SettingActivity.KEY_PROXY_IP, "128.2.213.18");
	        remotePort = Integer.parseInt(sharedPref.getString(SettingActivity.KEY_PROXY_PORT, "8080"));
	    	enableCtlChannel = sharedPref.getBoolean(SettingActivity.KEY_ENABLE_CTLCHANNEL, true);
	        
	 }

	 public int getProtocolIndex() {
			return protocolIndex;
	 }

	 public void setProtocolIndex(int protocolIndex) {
			this.protocolIndex = protocolIndex;
	 }
		
	 @Override
	 public boolean onKeyDown(int keyCode, KeyEvent event) {
		    if (keyCode == KeyEvent.KEYCODE_BACK) {
		    	
		    	cleanBeforeExit();
		    	
		        return true;
		    }
		    
		    return super.onKeyDown(keyCode, event);
	 }
		
	 private void cleanBeforeExit(){
		 
		 if (ctlChannel != null)
				try {
					ctlChannel.closeCtlChannel();
					ctlChannel = null;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	    	
	    	if (cameraRecorder != null){
	    		try {
					cameraRecorder.stopCapturing();
					cameraRecorder = null;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	    	
	    	}
	    	
	    	if (streamServer != null)
				try {
					streamServer.stopStreaming();
					streamServer = null;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	    	
	        finish();
	 }
		
}

