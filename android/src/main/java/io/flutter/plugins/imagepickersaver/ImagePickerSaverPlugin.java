// Copyright 2017 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package io.flutter.plugins.imagepickersaver;

import android.app.Activity;
import android.os.Environment;

import java.io.File;
import java.io.IOException;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;

import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.PluginRegistry;
import io.flutter.embedding.engine.plugins.FlutterPlugin;


public class ImagePickerSaverPlugin implements MethodChannel.MethodCallHandler,FlutterPlugin, ActivityAware {
    private static final String CHANNEL = "plugins.flutter.io/image_picker_saver";

    private static final int SOURCE_CAMERA = 0;
    private static final int SOURCE_GALLERY = 1;

//    private final PluginRegistry.Registrar registrar;
//    private final ImagePickerDelegate delegate;
    private ImagePickerDelegate delegate;
    private  MethodChannel channel;
    private Activity activity;

//    public static void registerWith(PluginRegistry.Registrar registrar) {
//        final MethodChannel channel = new MethodChannel(registrar.messenger(), CHANNEL);
//
//        final File externalFilesDirectory =
//                registrar.activity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
//        final ExifDataCopier exifDataCopier = new ExifDataCopier();
//        final ImageResizer imageResizer = new ImageResizer(externalFilesDirectory, exifDataCopier);
//
//        final ImagePickerDelegate delegate =
//                new ImagePickerDelegate(registrar.activity(), externalFilesDirectory, imageResizer);
//        registrar.addActivityResultListener(delegate);
//        registrar.addRequestPermissionsResultListener(delegate);
//
////        final ImagePickerSaverPlugin instance = new ImagePickerSaverPlugin(registrar, delegate);
//        channel.setMethodCallHandler(instance);
//    }

//    @VisibleForTesting
//    ImagePickerSaverPlugin(PluginRegistry.Registrar registrar, ImagePickerDelegate delegate) {
//        this.registrar = registrar;
//        this.delegate = delegate;
//    }

    @Override
    public void onMethodCall(MethodCall call, MethodChannel.Result result) {
//        if (registrar.activity() == null) {
        if (activity == null) {
            result.error("no_activity", "image_picker plugin requires a foreground activity.", null);
            return;
        }
        if (call.method.equals("pickImage")) {
            int imageSource = call.argument("source");
            switch (imageSource) {
                case SOURCE_GALLERY:
                    delegate.chooseImageFromGallery(call, result);
                    break;
                case SOURCE_CAMERA:
                    delegate.takeImageWithCamera(call, result);
                    break;
                default:
                    throw new IllegalArgumentException("Invalid image source: " + imageSource);
            }
        } else if (call.method.equals("pickVideo")) {
            int imageSource = call.argument("source");
            switch (imageSource) {
                case SOURCE_GALLERY:
                    delegate.chooseVideoFromGallery(call, result);
                    break;
                case SOURCE_CAMERA:
                    delegate.takeVideoWithCamera(call, result);
                    break;
                default:
                    throw new IllegalArgumentException("Invalid video source: " + imageSource);
            }
        } else if (call.method.equals("saveFile")) {


            try {
                delegate.saveImageToGallery(call, result);
            } catch (IOException e) {
                e.printStackTrace();
                throw new IllegalArgumentException(e);
            }


        } else {
            throw new IllegalArgumentException("Unknown method " + call.method);
        }
    }

    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding binding) {

        channel = new MethodChannel(binding.getBinaryMessenger(), CHANNEL);
        channel.setMethodCallHandler(this);
    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
        channel.setMethodCallHandler(null);
    }

    @Override
    public void onAttachedToActivity(@NonNull ActivityPluginBinding binding) {
        activity = binding.getActivity();
        //        final File externalFilesDirectory = registrar.activity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        final File externalFilesDirectory = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        final ExifDataCopier exifDataCopier = new ExifDataCopier();
        final ImageResizer imageResizer = new ImageResizer(externalFilesDirectory, exifDataCopier);

//        final ImagePickerDelegate delegate = new ImagePickerDelegate(registrar.activity(), externalFilesDirectory, imageResizer);
        final ImagePickerDelegate delegate = new ImagePickerDelegate(activity, externalFilesDirectory, imageResizer);
//        registrar.addActivityResultListener(delegate);
        binding.addActivityResultListener(delegate);
//        registrar.addRequestPermissionsResultListener(delegate);
        binding.addRequestPermissionsResultListener(delegate);

//        final ImagePickerSaverPlugin instance = new ImagePickerSaverPlugin(registrar, delegate);
    }

    @Override
    public void onDetachedFromActivityForConfigChanges() {

    }

    @Override
    public void onReattachedToActivityForConfigChanges(@NonNull ActivityPluginBinding binding) {
        onAttachedToActivity(binding);
    }

    @Override
    public void onDetachedFromActivity() {
        activity = null;
    }
}
