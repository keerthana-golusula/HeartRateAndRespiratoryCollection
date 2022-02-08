package com.example.DiaSheild;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraDevice;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private static int VIDEO_RECORD_CODE = 101;
    private Uri videoPath;
    CameraDevice cameraDevice;
    DBHelper db;
    Intent respIntent;
    TextView collRespRate = null;
    TextView collHeartRate =null;
    Button Symptoms, UploadSigns, RespRate;

    public BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction() == "Respiratory-Rate"){
                String message = intent.getStringExtra("RespRate");
                collRespRate.setText(message);
                Toast.makeText(MainActivity.this, "respiratory Rate collected:" + message, Toast.LENGTH_SHORT).show();
            }else{
                Integer message = intent.getIntExtra("HeartRate", 0);
                collHeartRate.setText(String.valueOf(message));
                Toast.makeText(MainActivity.this, "Hearrate collected:" + message, Toast.LENGTH_SHORT).show();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (isCameraPresent()) {
            Toast.makeText(MainActivity.this, "Camera is avaialble", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(MainActivity.this, "Camera is not avaialble", Toast.LENGTH_SHORT).show();
        }
        EditText lastName = findViewById(R.id.LastName);
        UploadSigns = findViewById(R.id.UplaodSigns);
        Symptoms = findViewById(R.id.Symptoms);
        RespRate = findViewById(R.id.Respiratory);
        collHeartRate = findViewById(R.id.collectedHeartRate);
        collRespRate = findViewById(R.id.collectedRespRate);
        db = new DBHelper(this);
        LocalBroadcastManager.getInstance(this).registerReceiver(
                mMessageReceiver, new IntentFilter("Respiratory-Rate"));
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter("Heart-Rate"));


        RespRate.setOnClickListener((new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                respIntent = new Intent(MainActivity.this, Respiratory.class);
                startService(respIntent);
            }
        }));

        Symptoms.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String LastName = lastName.getText().toString();
                String HeartRate = collHeartRate.getText().toString();
                String RespRate = collRespRate.getText().toString();

                Intent intent = new Intent(MainActivity.this, SymptomsCollection.class);
                intent.putExtra("KeyLastName", LastName);
                intent.putExtra("KeyHeartRate", Integer.parseInt(HeartRate));
                intent.putExtra("KeyRespRate", Integer.parseInt(RespRate));
                startActivity(intent);
            }
        });

        UploadSigns.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                String LastName = lastName.getText().toString();
                String HeartRate = collHeartRate.getText().toString();
                String RespRate = collRespRate.getText().toString();

                Boolean checkinsertdata = db.updateSignsDatatoDB(LastName, Integer.parseInt(HeartRate), Integer.parseInt(RespRate));
                if (checkinsertdata) {
                    Toast.makeText(MainActivity.this, "new user added", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "new user not added", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    public void heartRateCollection(View view) {
        Intent intent = new Intent(MainActivity.this, HeartRateMonitor.class);
        startActivity(intent);
        // recordVideo();

    }

    private boolean isCameraPresent() {
        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
            return true;
        } else {
            return false;
        }
    }


}



