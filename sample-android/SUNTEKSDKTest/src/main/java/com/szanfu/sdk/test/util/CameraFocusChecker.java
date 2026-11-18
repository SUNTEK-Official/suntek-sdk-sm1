package com.szanfu.sdk.test.util;

import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.os.Build;

/**
 * Android 13 摄像头对焦类型判断（Java）
 * 判断：变焦（AF，带自动对焦） / 定焦（FF，固定焦距）
 */
public class CameraFocusChecker {

    /**
     * 判断【后置摄像头】是否为变焦（AF）
     *
     * @return true = 变焦 / false = 定焦
     */
    public static boolean isBackCameraZoom(Context context) {
        return isCameraAutoFocus(context, true);
    }

    /**
     * 判断【前置摄像头】是否为变焦（AF）
     *
     * @return true = 变焦 / false = 定焦
     */
    public static boolean isFrontCameraZoom(Context context) {
        return isCameraAutoFocus(context, false);
    }

    /**
     * 核心判断逻辑
     */
    private static boolean isCameraAutoFocus(Context context, boolean isBackCamera) {
        CameraManager cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);

        try {

            for (String cameraId : cameraManager.getCameraIdList()) {
                CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraId);
                Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);


                int targetFacing = isBackCamera
                        ? CameraCharacteristics.LENS_FACING_BACK
                        : CameraCharacteristics.LENS_FACING_FRONT;

                if (facing == null || facing != targetFacing) {
                    continue;
                }


                int[] afModes = characteristics.get(CameraCharacteristics.CONTROL_AF_AVAILABLE_MODES);
                if (afModes == null || afModes.length == 0) {
                    return false;
                }


                for (int mode : afModes) {
                    if (mode == CaptureRequest.CONTROL_AF_MODE_AUTO
                            || mode == CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE
                            || mode == CaptureRequest.CONTROL_AF_MODE_MACRO) {
                        return true;
                    }
                }
                return false;
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

        return false;
    }
}