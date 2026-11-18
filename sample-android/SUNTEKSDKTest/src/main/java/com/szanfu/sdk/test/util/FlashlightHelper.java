package com.szanfu.sdk.test.util;

import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.widget.Toast;

public class FlashlightHelper {
    public static void toggleFlashlight(Context context, boolean enable) {
        CameraManager cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        try {
            String[] cameraIds = cameraManager.getCameraIdList();
            String flashlightCameraId = null;

            // Iterate through all camera IDs to find a suitable one with a flashlight
            for (String id : cameraIds) {
                CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(id);
                Boolean isFlashAvailable = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
                Integer lensFacing = characteristics.get(CameraCharacteristics.LENS_FACING);

                // Check if the camera has a flash and prioritize the back-facing camera
                if (isFlashAvailable != null && isFlashAvailable) {
                    if (lensFacing != null && lensFacing == CameraCharacteristics.LENS_FACING_BACK) {
                        flashlightCameraId = id;
                        break; // Prefer back-facing camera
                    }
                    if (flashlightCameraId == null) {
                        flashlightCameraId = id;
                    }
                }
            }

            if (flashlightCameraId != null) {
                cameraManager.setTorchMode(flashlightCameraId, enable);
            } else {
                Toast.makeText(context, "Flashlight not available on this device", Toast.LENGTH_SHORT).show();
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
            Toast.makeText(context, "Error accessing camera", Toast.LENGTH_SHORT).show();
        } catch (SecurityException e) {
            Toast.makeText(context, "Camera permission required", Toast.LENGTH_SHORT).show();
        }
    }
}
