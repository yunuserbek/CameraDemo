package com.example.camerademoapp;


import static android.content.pm.PackageManager.PERMISSION_GRANTED;


import static com.example.camerademoapp.CameraSelectorType.BACK_CAM;
import static com.example.camerademoapp.CameraSelectorType.FRONT_CAM;

import android.Manifest.permission;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.core.VideoCapture;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.util.Date;
import java.util.concurrent.Executor;

public class MainActivity extends AppCompatActivity implements ImageAnalysis.Analyzer {

    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;

    PreviewView previewView;
    private ImageCapture imageCapture;

    private ImageButton bCapture;
    private String lastImagePath = "";
    private CameraSelectorType mCameraSelectorType = BACK_CAM;
    private boolean timerState = false;
    private int countDownTimerValue = 0;
    String[] PERMISSIONS = {permission.CAMERA, permission.RECORD_AUDIO, permission.WRITE_EXTERNAL_STORAGE, permission.READ_EXTERNAL_STORAGE,};
    private TextView countDownCounter;
    int countDownTimerLastTime = 0;
    Executor executor;
    ImageButton timerButton;
    ImageButton cameraSwitchButton;
    ImageView resultPhoto;
    ConstraintLayout camera_container;
    CountDownTimer myCountDownTimer;

    private void init() {
        previewView = findViewById(R.id.view_finder);
        bCapture = findViewById(R.id.camera_capture_button);
        countDownCounter = findViewById(R.id.countdown);
        timerButton = findViewById(R.id.timer_button);
        resultPhoto = findViewById(R.id.resultPhoto);
        cameraSwitchButton = findViewById(R.id.camera_switch_button);
        camera_container = findViewById(R.id.camera_container);
        executor = ContextCompat.getMainExecutor(this);
        checkPermissions(this);
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            startCameraX(BACK_CAM);
        }, executor);
        timerButton.setBackgroundResource(R.drawable.ic_timer_off);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        init();
        bCapture.setOnClickListener(v -> {
            capturePhoto();
        });



        cameraSwitchButton.setOnClickListener(v -> {
            cameraProviderFuture.addListener(() -> {
                if (mCameraSelectorType == BACK_CAM)
                    startCameraX(FRONT_CAM);
                else startCameraX(BACK_CAM);
            }, executor);
        });

        timerButton.setOnClickListener(v -> {
            if (timerState) {
                timerState = false;
                findViewById(R.id.countdown).setVisibility(View.INVISIBLE);
                startCameraX(mCameraSelectorType);
                timerButton.setBackgroundResource(R.drawable.ic_timer_off);
            } else {
                timerState = true;
                findViewById(R.id.countdown).setVisibility(View.VISIBLE);
                startCameraX(mCameraSelectorType);
                timerButton.setBackgroundResource(R.drawable.ic_timer_10_sec);

            }
        });

    }

    public void checkPermissions(Context context) {
        boolean state = false;
        if (context != null && PERMISSIONS != null)
            for (String permission : PERMISSIONS)
                if (ActivityCompat.checkSelfPermission(context, permission) != PERMISSION_GRANTED)
                    state = true;

        if (state) ActivityCompat.requestPermissions(this, PERMISSIONS, 1);
    }

    @SuppressLint("RestrictedApi")
    private void startCameraX(CameraSelectorType cameraSelectorType) {

        ProcessCameraProvider cameraProvider;
        try {
            if (cameraSelectorType == BACK_CAM)
                mCameraSelectorType = BACK_CAM;
            else mCameraSelectorType = FRONT_CAM;

            cameraProvider = cameraProviderFuture.get();
            cameraProvider.unbindAll();
            CameraSelector cameraSelector = new CameraSelector.Builder()
                    .requireLensFacing(cameraSelectorType.value)
                    .build();
            Preview preview = new Preview.Builder()
                    .build();
            preview.setSurfaceProvider(previewView.getSurfaceProvider());
            imageCapture = new ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                    .build();

            ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build();

            imageAnalysis.setAnalyzer(executor, MainActivity.this::analyze);
            cameraProvider.bindToLifecycle(MainActivity.this::getLifecycle, cameraSelector, preview, imageCapture);
        } catch (Exception e) { }
    }

    @Override
    public void analyze(@NonNull ImageProxy image) {
        Log.d("TAG", "analyze: got the frame at: " + image.getImageInfo().getTimestamp());
        image.close();
    }

    private void capturePhoto() {

        if (timerState) countDownTimerLastTime = 10000;

        else countDownTimerLastTime = 0;

        if (myCountDownTimer != null){
            myCountDownTimer.cancel();
        }

        myCountDownTimer = new CountDownTimer(countDownTimerLastTime, 1000) {

            public void onTick(long duration) {
                long Ssec = (duration / 1000) % 60;
                if (Ssec < 10)
                    countDownCounter.setText(String.valueOf(Ssec));
            }

            public void onFinish() {
                countDownCounter.setVisibility(View.INVISIBLE);
                File photoDir = new File(Constant.FILE_SAVE_LOCATION);

                if (!photoDir.exists())
                    photoDir.mkdir();

                Date date = new Date();
                String timestamp = String.valueOf(date);
                String photoFilePath = photoDir.getAbsolutePath() + "/" + timestamp + ".jpg";
                File photoFile = new File(photoFilePath);

                imageCapture.takePicture(
                        new ImageCapture.OutputFileOptions.Builder(photoFile).build(),
                        executor,
                        new ImageCapture.OnImageSavedCallback() {
                            @SuppressLint("ResourceType")
                            @Override
                            public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                                Toast.makeText(MainActivity.this, "Photo has been saved successfully.", Toast.LENGTH_SHORT).show();
                                lastImagePath = photoFilePath;
                                resultPhoto.setVisibility(View.VISIBLE);
                                camera_container.setVisibility(View.GONE);
                                setImageFromPath(lastImagePath);



                            }

                            @Override
                            public void onError(@NonNull ImageCaptureException exception) {
                                Toast.makeText(MainActivity.this, "Error saving photo: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                );
            }

        }.start();



    }
    private void setImageFromPath(String imgFile){
        if (imgFile != null) {
            Bitmap myBitmap = BitmapFactory.decodeFile(imgFile);
            ImageView myImage = (ImageView) findViewById(R.id.resultPhoto);

            myImage.setImageBitmap(myBitmap);
            myImage.setRotation(90f);

        }
    }
    @Override
    public void onBackPressed() {

        if (resultPhoto.getVisibility() == View.VISIBLE){
            resultPhoto.setVisibility(View.GONE);
            camera_container.setVisibility(View.VISIBLE);
        }

    }
}