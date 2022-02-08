
package com.example.DiaSheild;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


//import androidx.annotation.RequiresApi;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
//import android.support.v4.ActivityCompat;
import androidx.core.app.ActivityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.util.Arrays;

public class HeartRateMonitor extends AppCompatActivity {
    static final String TAG = "HeartRateMonitor";
     TextureView textureView; //TextureView to display camera data
    private TextView heartRateView;
    private Button submitHeartRate;
    private String cameraId;
    public CameraDevice cameraDevice;
    public CameraCaptureSession cameraCaptureSessions;
    public CaptureRequest.Builder captureRequestBuilder;
    private Size imageDimension;
    private static final int REQUEST_CAMERA_PERMISSION = 1;

    // Thread handler member variables
    Handler mBckgrdHandler;
    HandlerThread mBckgrndThread;

    //Heart rate detector member variables
    public static int hrtratebpm;
    private int mCurrentRollingAvg;
    private int mLastRollingAvg;
    private int mLastLastRollingAvg;
    private long [] mTimeArray;
    private int numOfCaptures = 0;
    private int mNumOfBeats = 0;

    public HeartRateMonitor() {
        super();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_heart_rate_monitor);
        Bundle b = getIntent().getExtras();
        textureView =  (TextureView) findViewById(R.id.CameraTexture);
        submitHeartRate = findViewById(R.id.submitHeartRate);
        assert textureView != null;
        textureView.setSurfaceTextureListener(textureListener);
        mTimeArray = new long [15];
        heartRateView = (TextView)findViewById(R.id.HeartRateView);

        submitHeartRate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendHeartRate();
                finish();
            }
        });

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        sendHeartRate();
        finish();
    }

    TextureView.SurfaceTextureListener textureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            openCamera();
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
            Bitmap bitmaps = textureView.getBitmap();
            int width = bitmaps.getWidth();
            int height = bitmaps.getHeight();
            int[] allPixels = new int[height * width];

            bitmaps.getPixels(allPixels, 0, width, width / 2, height / 2, width / 20, height / 20);
            int sumOfReds = 0;
            for (int i = 0; i < allPixels.length; i++) {
                int red = (allPixels[i] >> 16) & 0xFF;
                sumOfReds = sumOfReds + red;
            }
            // Waiting till 20 captures, to remove startup artifacts.  First average is the sum.
            if (numOfCaptures == 20) {
                mCurrentRollingAvg = sumOfReds;
            }
            // Next 18 averages needs to used with the sum with the correct N multiplier in rolling average.
            else if (numOfCaptures > 20 && numOfCaptures < 49) {
                mCurrentRollingAvg = (mCurrentRollingAvg*(numOfCaptures-20) + sumOfReds)/(numOfCaptures-19);
            }
            // From 49 on, the rolling average uses with the last 30 rolling averages.
            else if (numOfCaptures >= 49) {
                mCurrentRollingAvg = (mCurrentRollingAvg*29 + sumOfReds)/30;
                if (mLastRollingAvg > mCurrentRollingAvg && mLastRollingAvg > mLastLastRollingAvg && mNumOfBeats < 15) {
                    mTimeArray[mNumOfBeats] = System.currentTimeMillis();
                    mNumOfBeats++;
                    if (mNumOfBeats == 15) {
                        calcBPM();
                    }
                }
            }

            // Another capture
            numOfCaptures++;
            // Save previous two values
            mLastLastRollingAvg = mLastRollingAvg;
            mLastRollingAvg = mCurrentRollingAvg;
        }
    };
    private final CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice camera) {
            Log.e(TAG, "onOpened");
            cameraDevice = camera;
            createCameraPreview();
        }

        @Override
        public void onDisconnected(CameraDevice camera) {
            cameraDevice.close();
        }

        @Override
        public void onError(CameraDevice camera, int error) {
            if (cameraDevice != null)
                cameraDevice.close();
            cameraDevice = null;
        }
    };

    // onPause
    protected void stopBackgroundThread() {
        // stopping camera thread
        mBckgrndThread.quitSafely();
        try {
            mBckgrndThread.join();
            mBckgrndThread = null;
            mBckgrdHandler = null;
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }

    private void calcBPM() {
        long [] timediff = new long [14];
        long sum=0, avg;
        for (int i = 0; i < 14; i++) {
            timediff[i] = mTimeArray[i+1] - mTimeArray[i];
            sum += timediff[i];
        }
        Arrays.sort(timediff);
        //Averaging
        avg = sum/14;
        hrtratebpm = (int)(60000/avg);
        Toast.makeText(this, String.valueOf(60000/avg), Toast.LENGTH_SHORT).show();
        sendHeartRate();
    }
    private void sendHeartRate()
    {
        heartRateView.setText("Heart Rate = "+hrtratebpm+" BPM");
        Intent intent = new Intent("Heart-Rate");

        intent.putExtra("HeartRate", hrtratebpm);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    protected void createCameraPreview() {
        try {
            SurfaceTexture CameraTexture = textureView.getSurfaceTexture();
            assert CameraTexture != null;
            CameraTexture.setDefaultBufferSize(imageDimension.getWidth(), imageDimension.getHeight());
            Surface surface = new Surface(CameraTexture);
            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(surface);
            cameraDevice.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(CameraCaptureSession cameraCaptureSession) {
                    if (null == cameraDevice) {
                        return;
                    }
                    cameraCaptureSessions = cameraCaptureSession;
                    updatePreview();
                }

                @Override
                public void onConfigureFailed(CameraCaptureSession cameraCaptureSession) {
                    Toast.makeText(HeartRateMonitor.this, "Configuration change", Toast.LENGTH_SHORT).show();
                }
            }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void openCamera() {
        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        Log.e(TAG, "is camera open");
        try {
            cameraId = cameraManager.getCameraIdList()[0];
            CameraCharacteristics chars = cameraManager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap map = chars.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            assert map != null;
            imageDimension = map.getOutputSizes(SurfaceTexture.class)[0];

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(HeartRateMonitor.this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
                return;
            }
            cameraManager.openCamera(cameraId, stateCallback, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        Log.e(TAG, "opened Camera X");
    }
    protected void updatePreview() {

        if (null == cameraDevice) {
            Log.e(TAG, "update-Preview error, return");
        }
        captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
        captureRequestBuilder.set(CaptureRequest.FLASH_MODE, CameraMetadata.FLASH_MODE_TORCH);
        try {
            cameraCaptureSessions.setRepeatingRequest(captureRequestBuilder.build(), null, mBckgrdHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
    private void closeCamera() {
        if (null != cameraDevice) {
            cameraDevice.close();
            cameraDevice = null;
        }
    }

    // onResume
    protected void startBackgroundThread() {

        mBckgrndThread = new HandlerThread("Camera running in Background");
        mBckgrndThread.start();
        mBckgrdHandler = new Handler(mBckgrndThread.getLooper());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] Results) {
        super.onRequestPermissionsResult(requestCode, permissions, Results);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (Results[0] == PackageManager.PERMISSION_DENIED) {
                // close the app
                Toast.makeText(HeartRateMonitor.this, "Permission Not granted!!", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }
    @Override
    protected void onPause() {
        Log.e(TAG, "on-Pause");
        closeCamera();
        stopBackgroundThread();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e(TAG, "on-Resume");
        startBackgroundThread();
        if (textureView.isAvailable()) {
            openCamera();
        } else {
            textureView.setSurfaceTextureListener(textureListener);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }
}
