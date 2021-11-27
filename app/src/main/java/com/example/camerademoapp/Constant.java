package com.example.camerademoapp;

public class Constant {
    public static String FILE_SAVE_LOCATION = "/mnt/sdcard/Pictures/CameraXPhotos";
}

enum CameraSelectorType{
    FRONT_CAM(0),BACK_CAM(1);
    public final int value;
    CameraSelectorType(final int newValue) { value = newValue; }
}