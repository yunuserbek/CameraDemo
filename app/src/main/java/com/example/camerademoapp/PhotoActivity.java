package com.example.camerademoapp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.util.Arrays;

public class PhotoActivity extends AppCompatActivity {
    String imgFilePath = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);
        // imgFilePath = new Bundle().getString("imgFilepath");

// getting the bundle back from the android
        Bundle bundle = getIntent().getExtras();

// getting the string back
         imgFilePath = bundle.getString("imgFilepath", null);


        setImageFromPath(imgFilePath);
    }

    private void setImageFromPath(String imgFile){
        if (imgFile != null) {
            Bitmap myBitmap = BitmapFactory.decodeFile(imgFile);
            ImageView myImage = (ImageView) findViewById(R.id.photoImageView);
            myImage.setImageBitmap(myBitmap);
        }
    }
    // read photo from file director
    private void getAllPhoto() {
        File directory = new File(com.example.camerademoapp.Constant.FILE_SAVE_LOCATION);
        if (directory.exists()) {
            System.out.println(directory.exists());
            System.out.println(Arrays.toString(directory.listFiles()));

            File[] files = directory.listFiles();
            if (files != null) {
                for (File imgFile : files) {
                    // her bir fotoğrafın dosya yolu  = imgFile
                }
            }
        }
    }
}