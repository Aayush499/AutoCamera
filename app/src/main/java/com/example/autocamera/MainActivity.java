package com.example.autocamera;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.Manifest;

import android.os.Bundle;
import android.content.Intent;
import android.view.TextureView;
import android.widget.Button;
import android.view.View;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.IBinder;
import android.util.Log;
import android.hardware.camera2.CameraDevice;
import android.content.pm.PackageManager;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private TextureView textureView;
    private Button btnCapture;
    private boolean isCameraServiceRunning = false;
    private Intent cameraServiceIntent;
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
        }

        btnCapture = findViewById(R.id.captureButton);
        btnCapture.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view)
            {


                    if (!isCameraServiceRunning) {
                        cameraServiceIntent = new Intent(MainActivity.this, CameraService.class);
                        startService(cameraServiceIntent);
                        //log message that says "Image taken"
                        Log.d("MainActivity", "Image taken");
                        isCameraServiceRunning = true;
                        btnCapture.setText("Stop Capturing");

                    } else {
                        stopService(cameraServiceIntent);
                        isCameraServiceRunning = false;
                        btnCapture.setText("Start Capturing");
                    }
                }

        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode , @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == CAMERA_PERMISSION_REQUEST_CODE)
        {
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    Toast.makeText(MainActivity.this, "Camera permission granted", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Toast.makeText(MainActivity.this, "Camera permission denied", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

}