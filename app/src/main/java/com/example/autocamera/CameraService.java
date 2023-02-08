package com.example.autocamera;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Camera;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.view.WindowManager;

import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import androidx.annotation.NonNull;
import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.media.Image;
import android.media.ImageReader;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.hardware.camera2.CameraDevice;
import android.content.pm.PackageManager;
import android.util.Size;
import android.view.Surface;
import android.util.*;
import android.widget.Toast;
import java.util.Collections;
import java.util.Comparator;


import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class CameraService extends Service {
    private long pictureInterval = 30000; // 30 seconds
    private boolean running = true;
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;
    private CameraDevice cameraDevice;

    private static final String Tag = "CameraService";

    private CameraManager cameraManager;
    private String cameraId;
    private ImageReader imageReader;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private ScheduledExecutorService executor;
    public CameraService() {
    }

    private static class CompareSizesByArea implements Comparator<Size> {

        @Override
        public int compare(Size lhs, Size rhs) {
            // We want to sort the sizes in descending order so that the largest size is first.
            return Long.signum((long) rhs.getWidth() * rhs.getHeight() -
                    (long) lhs.getWidth() * lhs.getHeight());
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @Override

    public void onCreate() {
        super.onCreate();
        cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        executor = Executors.newSingleThreadScheduledExecutor();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //log message that states "Command started"
        Log.d(Tag, "Command started");
        executor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                takePicture();
            }
        }, 0, 30, TimeUnit.SECONDS);
        return START_STICKY;
    }

    @SuppressLint("MissingPermission")
    private Camera mCamera;

    private CameraDevice mCameraDevice;
    private CameraCaptureSession mCaptureSession;
    private CaptureRequest.Builder mPreviewBuilder;
    private ImageReader mImageReader;

    @SuppressLint("MissingPermission")
    private void takePicture() {

        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            //log a message
            Log.d(Tag, "Reached try");
            String cameraId = cameraManager.getCameraIdList()[0];
            final ImageReader imageReader = ImageReader.newInstance(1280, 720, ImageFormat.JPEG, 1);
            cameraManager.openCamera(cameraId, new CameraDevice.StateCallback() {

                @Override
                public void onOpened(@NonNull CameraDevice camera) {
    //log a message
                    Log.d(Tag, "Reached onOpened");
                    try {
                        //log that states "Reached onOpened
                        Log.d(Tag, "Reached onOpened");
                        List<Surface> outputSurfaces = Arrays.asList(imageReader.getSurface());
                        camera.createCaptureSession(outputSurfaces, new CameraCaptureSession.StateCallback() {
                            @Override
                            public void onConfigured(@NonNull CameraCaptureSession session) {
                                //log a message
                                Log.d(Tag, "Reached onConfigured");
                                try {
                                    CaptureRequest.Builder builder = camera.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
                                    builder.addTarget(imageReader.getSurface());
                                    builder.set(CaptureRequest.JPEG_ORIENTATION, 0);
                                    session.capture(builder.build(), new CameraCaptureSession.CaptureCallback() {
                                        @Override
                                        public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
                                            //log a message
                                            Log.d(Tag, "Reached onCaptureCompleted");
                                            super.onCaptureCompleted(session, request, result);
                                            Image image = imageReader.acquireLatestImage();
                                            if (image != null) {
                                                ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                                                byte[] bytes = new byte[buffer.remaining()];
                                                buffer.get(bytes);
                                                // Save the image to the device storage
                                                saveImage(bytes);
                                                image.close();
                                            }
                                        }
                                    }, null);
                                } catch (CameraAccessException e) {
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                            //log that states "Failed to configure camera"
                                Log.d(Tag, "Failed to configure camera");

                            }
                        }, null);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onDisconnected(@NonNull CameraDevice camera) {
                    camera.close();
                }@Override
                public void onError(@NonNull CameraDevice camera, int error) {
                    camera.close();
                }
            }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }}

    private void saveImage(byte[] imageBytes) {
        //log message that says "Saving image to Gallery"
        Log.d(Tag, "Saving image to Gallery");
        File picturesDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File imageFile = new File(picturesDirectory, System.currentTimeMillis() + ".jpg");
        try {
            FileOutputStream outputStream = new FileOutputStream(imageFile);
            outputStream.write(imageBytes);
            outputStream.close();
// Make the image visible in the gallery
            //log message that says "Image saved to Gallery"
            Log.d(Tag, "Image saved to Gallery");
            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            Uri contentUri = Uri.fromFile(imageFile);
            mediaScanIntent.setData(contentUri);
            sendBroadcast(mediaScanIntent);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

                private File getOutputMediaFile() {
        // Create a file for saving the picture
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "MyCameraApp");

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d(TAG, "failed to create directory");
                return null;
            }
        }

        // Create a unique file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                "IMG_"+ timeStamp + ".jpg");

        return mediaFile;
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        // Close the image reader
        imageReader.close();
    }
}

