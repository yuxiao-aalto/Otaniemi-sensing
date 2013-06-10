//Reference: http://developer.android.com/training/camera/videobasics.html
//reference: http://developer.android.com/guide/topics/media/camera.html
	
package com.example.streamclient;

import java.io.FileDescriptor;
import java.io.IOException;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.net.LocalServerSocket;
import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.util.Log;
import android.view.Surface;


public class CameraRecording extends Thread{
	private MediaRecorder videoRecorder;
	private LocalSocket localSender;
	private LocalServerSocket localLoop;
	private Camera camera;
	private boolean isRecording;
	public static final int MEDIA_TYPE_IMAGE = 1;
	public static final int MEDIA_TYPE_VIDEO = 2;
	private Surface previewSurface;
	private int videoEncoderIndex;
	private int videoOutputFormatIndex;
	
	public CameraRecording(){
		localSender = null;
		localLoop = null;
		videoRecorder = null;
		camera = null;
		isRecording = false;
		previewSurface = null;
		videoEncoderIndex = MediaRecorder.VideoEncoder.DEFAULT;
		videoOutputFormatIndex = MediaRecorder.OutputFormat.DEFAULT;
	}
	
	private void initLocalSocket(){
		try{
			
			localSender = new LocalSocket();
			localSender.connect(new LocalSocketAddress("videoserver"));
			localSender.setReceiveBufferSize(10240);
			localSender.setSendBufferSize(10240);
				
		}catch(IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void run(){
		initLocalSocket();
		
		FileDescriptor fd = localSender.getFileDescriptor();
		
		if (fd == null)
			Log.v("fd", "null");
		else
			try {
				if (!startVideoCapturing(fd)){
					Log.v("camera","failed to start");
					stopCapturing();
					return;
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
	}
	
	
	//unlock camera, configure video recorder, start recording and write the data to fd
	private boolean startVideoCapturing(FileDescriptor fd) throws IOException{
    	    	
    	videoRecorder = new MediaRecorder();
    	
    	camera.unlock();
    	
    	videoRecorder.setCamera(camera);
    	
    	//don't change the order in the following steps
    	
    	//videoRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
    	videoRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
    	
    	//for Android 2.2 and higher version, use cameraprofile
    	//CamcorderProfile cameraProfile = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);
    	//videoRecorder.setProfile(cameraProfile);
    	
    	videoRecorder.setOutputFormat(videoOutputFormatIndex);

    	videoRecorder.setOutputFile(fd);
    	
    	videoRecorder.setVideoSize(176, 144);
    	videoRecorder.setVideoFrameRate(30);
    	videoRecorder.setVideoEncoder(videoEncoderIndex);
    	
    	/*String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
    	File tempFile = new File(Environment.getExternalStoragePublicDirectory(
	              Environment.DIRECTORY_PICTURES)+File.separator+"video_"+timeStamp+".mp4");
    	videoRecorder.setOutputFile(tempFile.toString());*/
    	
    	videoRecorder.setPreviewDisplay(previewSurface);
    	//if any of the above configuration is missing, prepare() will fail
    	
    	try {
			videoRecorder.prepare();
			Log.v("camera","prepared");
			
		} catch (IOException ex) {
			ex.printStackTrace();
			System.out.println(ex.getMessage());
			this.stopCapturing();
			return false;
		}
    	
    	videoRecorder.start();
		isRecording = true;
		
    	return true;
    }
	
	public void stopCapturing() throws IOException{
		
		if (videoRecorder != null) {
			if (isRecording)
				videoRecorder.stop();
			
			videoRecorder.reset();
			videoRecorder.release();
		}
		
		if (camera != null){
			camera.stopPreview();
			camera.lock();
			camera.release();
			camera = null;
		}
		
		if (localSender != null)
			localSender.close();
		
		if (localLoop != null)
			localLoop.close();
		
		localSender = null;
		localLoop = null;
		
	}
	
	public void setPreviewSurface(Surface s){
		previewSurface = s;
	}
	
	
	public void setCamera(Camera id){
		camera = id;
	}
	
	public void setVideoEncoder(int i){
		videoEncoderIndex = i;
	}
	
	public int getVideoEncoder(){
		return videoEncoderIndex;
	}
	
	public void setVideoOutputFormat(int i){
		videoOutputFormatIndex = i;
	}
	
	public int getVideoOutputFormat(){
		return videoOutputFormatIndex;
	}

}
