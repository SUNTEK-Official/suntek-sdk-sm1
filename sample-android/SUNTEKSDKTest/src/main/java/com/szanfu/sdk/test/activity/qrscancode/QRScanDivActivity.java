package com.szanfu.sdk.test.activity.qrscancode;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.szanfu.sdk.api.ISdkServiceManager;
import com.szanfu.sdk.api.constant.ServiceID;
import com.szanfu.sdk.api.system.ISystemBinderService;
import com.szanfu.sdk.api.system.ScanCallback;
import com.szanfu.sdk.test.activity.mycamera.CameraActivity;
import com.szanfu.sdk.test.activity.scans.MainScansActivity;
import com.szanfu.sdk.test.base.ListButtonBaseActivity;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.huawei.hms.hmsscankit.ScanUtil;
import com.huawei.hms.ml.scan.HmsScan;
import com.huawei.hms.ml.scan.HmsScanAnalyzerOptions;
import com.szanfu.sdk.test.R;
import com.szanfu.sdk.test.activity.Camera2Activity;
import com.szanfu.sdk.test.activity.QrScanActivity;
import com.szanfu.sdk.test.activity.ScanByHWActivity;
import com.szanfu.sdk.test.activity.camera2.Camera2Helper;
import com.szanfu.sdk.test.hmscorescan.CommonActivity;
import com.szanfu.sdk.test.hmscorescan.HWDefinedActivity;
import com.szanfu.sdk.test.hmscorescan.GenerateCodeActivity;
import com.szanfu.sdk.test.receiver.MyBroadcastReceiver;
import com.szanfu.sdk.test.util.CameraFocusChecker;
import com.szanfu.sdk.test.util.FileStorageUtils;
import com.szanfu.sdk.test.util.LogUtils;
import com.szanfu.sdk.test.util.WatermarkUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class QRScanDivActivity extends ListButtonBaseActivity implements ActivityCompat.OnRequestPermissionsResultCallback {
    private int qrScanType = 0; // 0:Zxing 1:HW 2:HW 3:DIKEscans
    private boolean bonBackPressed = true;
    private final AtomicInteger iRunDone = new AtomicInteger(0);
    public static final String EXTRA_MANUAL_FOCUS = "manual_focus";
    public static final String EXTRA_FOCUS_DISTANCE = "focus_distance";
    private final AtomicInteger iNeedsRun = new AtomicInteger(1);
    public static final int POSITION_QRSCAN_GENERATECODE = 0;
    public static final int POSITION_POSITION_ZXINg = 1;
    public static final int POSITION_HMS_QRScan = 2;
    public static final int POSITION_HMS_QRSCAN_FRONT = 3;

    public static final int POSITION_SCANS_MYCAMERA = 4;
    public static final int POSITION_CHECK_RUN_STARUS = 5;
    public static final int POSITION_CHECK_CHECK_CAMERA_FIXED_FOCUS_ZOOM = 6;
//    public static final int POSITION_SCANS = 4;
//    public static final int POSITION_SCANS_PRESSURE = 5;
//    public static final int POSITION_SCANS_activity = 6;
 // WeakReference
    private WeakReference<AlertDialog> alertDialogWeakRef;
    private WeakReference<FileStorageUtils> fileStorageWeakRef;
    private WeakReference<ISystemBinderService> systemBinderServiceWeakRef;

    private int iCountNumberNeedsRun = 0;
    private int iIsRunsCount = 0;
    boolean isClosedActivity = true;
    private String[] mButtons;
    private int scanType = DEFINED_CODE;

    private static final int PERMISSION_REQUEST_CODE = 11;
    public static final int CAMERA_REQ_CODE = 111;
    public static final int DEFINED_CODE = 222;
    public static final int BITMAP_CODE = 333;
    public static final int MULTIPROCESSOR_SYN_CODE = 444;
    public static final int MULTIPROCESSOR_ASYN_CODE = 555;
    public static final int GENERATE_CODE = 666;
    public static final int CUSTOM_CODE = 777;
    public static final int CUSTOM_FRONT_CAMERA_USE_HW = 888;
    public static final int CUSTOM_BACK_CAMERA_USE_HW = 999;
    public static final int CUSTOM_FRONT_CAMERA_USE_AX = 1000;
    public static final int CUSTOM_BACK_CAMERA_USE_AX = 1001;
    public static final int CUSTOM_BACK_FIXED_CAMERA_USE_HW = 1002;
    public static final int CUSTOM_BACK_FIXED_CAMERA_USE_AX = 1003;
    public static final int SCAN_GOOGLE_MLKIT = 1004;
    public static final int DECODE = 1;
    public static final int GENERATE = 2;
    private static final int REQUEST_CODE_SCAN_ONE = 0X01;
    private static final int REQUEST_CODE_DEFINE = 0X0111;
    private static final int REQUEST_CODE_SCAN_MULTI = 0X011;
    private static final int REQUEST_CODE_ZX_SCANNER = 0X0112;
    private static final int REQUEST_CODE_CUSTOM_SCAN = 0X113;
    private static final int REQUEST_CODE_CUSTOM_FRONT_CAMERA_BY_HW = 0X114;
    private static final int REQUEST_CODE_CUSTOM_BACK_CAMERA_BY_HW = 0X115;
    private static final int REQUEST_CODE_CUSTOM_FRONT_CAMERA_BY_AX = 0X116;
    private static final int REQUEST_CODE_CUSTOM_BACK_CAMERA_BY_AX = 0X117;
    private static final int REQUEST_CODE_CUSTOM_BACK_CAMERA_FIXED_BY_HW = 0X118;
    private static final int REQUEST_CODE_CUSTOM_BACK_CAMERA_FIXED_BY_AX = 0X119;
    private static final int REQUEST_CODE_BY_GOOGLE_MLKIT = 0X200;
    public static final String DECODE_MODE = "decode_mode";
    public static final String RESULT = "SCAN_RESULT";

    private final AtomicInteger iRunSuccess = new AtomicInteger(0);
    private final AtomicInteger iRunError = new AtomicInteger(0);
    private final AtomicInteger iRunCount = new AtomicInteger(0);
    private int iRunOrg = 1;

 // WeakReference BroadcastReceiver
    private WeakReference<MyBroadcastReceiver> broadcastReceiverWeakRef;
    private int iIntervalSeconds;
    private ScheduledExecutorService scheduledExecutorService;
    private ExecutorService singleExecutor;
    private int iCheckScanType = 0;
    private WeakReference<QRScanDivActivity> activityWeakRef; // Activity , Context
    private int iTimeOut = 7;

    private static final int REQUEST_CODE_STORAGE_PERMISSION = 100;
    private LinearLayout ll_count;
    private EditText ed_conut;
    private TextView tv_ps;
    private Button clear_text;

    private final StringBuilder fileWriteBuffer = new StringBuilder();
    private static final int BATCH_WRITE_THRESHOLD = 100;

    private boolean isScanPermissionGranted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mButtons = getResources().getStringArray(R.array.QRCODE_SERVICE);
        setButtonDataSource(mButtons);

 // Activity , Context
        activityWeakRef = new WeakReference<>(this);
 // fileStorageWeakRef
        fileStorageWeakRef = new WeakReference<>(FileStorageUtils.getInstance(this));

        setWaterMask();
        ll_count = findViewById(R.id.ll_count);
        ll_count.setVisibility(View.VISIBLE);
        ed_conut = findViewById(R.id.ed_conut);
        tv_ps = findViewById(R.id.tv_ps);
        tv_ps.setVisibility(View.VISIBLE);
        clear_text = findViewById(R.id.clear_text);
        clear_text.setVisibility(View.GONE);

 // ( )
        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        singleExecutor = Executors.newSingleThreadExecutor();

        checkAndCacheScanPermission();
    }

    @Override
    protected void onItemClick(int position, String itemName) {
        super.onItemClick(position, itemName);
        QRScanDivActivity activity = activityWeakRef.get();
        if (activity == null) return;

        switch (position) {
            case POSITION_QRSCAN_GENERATECODE:
                appendInfoText(itemName);
                Intent intent = new Intent(activity, GenerateQRCodeActivity.class);
                startActivity(intent);
                break;

            case POSITION_POSITION_ZXINg:
                bonBackPressed = false;
                appendInfoText(itemName);
                qrScanType = 0;
                String strNeedsRun = ed_conut.getText().toString().trim();
                LogUtils.e(" strNeedsRun : " + strNeedsRun);

                if (!TextUtils.isEmpty(strNeedsRun)) {
                    iNeedsRun.set(Integer.parseInt(strNeedsRun));
                }

                initPressureConfig(qrScanType, iNeedsRun.get());
 // fileStorage
                FileStorageUtils fileStorage = fileStorageWeakRef.get();
                if (fileStorage != null) {
                    fileStorage.deleteFile("pressureZXingResult.txt");
                    fileStorage.writeFile("runDone.txt", "0");
                }

                iRunDone.set(0);
                fileWriteBuffer.setLength(0);

                if (isScanPermissionGranted) {
                    startScanTask();
                } else {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.CAMERA},
                            PERMISSION_REQUEST_CODE);
                }
                break;

            case POSITION_HMS_QRScan:
                bonBackPressed = false;
                qrScanType = 1;
                appendInfoText(itemName);
                String strNeedsRunHW = ed_conut.getText().toString().trim();
                LogUtils.e(" strNeedsRunHW : " + strNeedsRunHW);

                if (!TextUtils.isEmpty(strNeedsRunHW)) {
                    iNeedsRun.set(Integer.parseInt(strNeedsRunHW));
                }

                initPressureConfig(qrScanType, iNeedsRun.get());
 // fileStorage
                fileStorage = fileStorageWeakRef.get();
                if (fileStorage != null) {
                    fileStorage.deleteFile("DEFINED_CODE.txt");
                    fileStorage.writeFile("runDone.txt", "0");
                }

                iRunDone.set(0);
                fileWriteBuffer.setLength(0);

                if (isScanPermissionGranted) {
                    runOnUiThread(this::startHWDefinedScan);
                } else {
                    String[] permissions;
                    if (Build.VERSION.SDK_INT >= 33) {
                        permissions = new String[]{Manifest.permission.CAMERA, "android.permission.READ_MEDIA_IMAGES"};
                    } else {
                        permissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE};
                    }
                    ActivityCompat.requestPermissions(this, permissions, DEFINED_CODE);
                }
                scanType = DEFINED_CODE;
                break;

            case POSITION_HMS_QRSCAN_FRONT:
                bonBackPressed = false;
                qrScanType = 2;
                appendInfoText(itemName);
                String strNeedsRunHWFront = ed_conut.getText().toString().trim();
                LogUtils.e(" strNeedsRunHWFront : " + strNeedsRunHWFront);

                if (!TextUtils.isEmpty(strNeedsRunHWFront)) {
                    iNeedsRun.set(Integer.parseInt(strNeedsRunHWFront));
                }

                initPressureConfig(qrScanType, iNeedsRun.get());
 // fileStorage
                fileStorage = fileStorageWeakRef.get();
                if (fileStorage != null) {
                    fileStorage.deleteFile("CUSTOM_FRONT_CAMERA_USE_HW.txt");
                    fileStorage.writeFile("runDone.txt", "0");
                }

                iRunDone.set(0);
                fileWriteBuffer.setLength(0);

                if (isScanPermissionGranted) {
                    runOnUiThread(this::startHWFrontScan);
                } else {
                    String[] permissions;
                    if (Build.VERSION.SDK_INT >= 33) {
                        permissions = new String[]{Manifest.permission.CAMERA, "android.permission.READ_MEDIA_IMAGES"};
                    } else {
                        permissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE};
                    }
                    ActivityCompat.requestPermissions(this, permissions, CUSTOM_FRONT_CAMERA_USE_HW);
                }
                scanType = CUSTOM_FRONT_CAMERA_USE_HW;
                break;

            /*case POSITION_SCANS:
                appendInfoText(itemName);
                showFocusSettingDialog();
                break;

            case POSITION_SCANS_PRESSURE:
                qrScanType = 3;
                String strNeedsRunScans = ed_conut.getText().toString().trim();
                LogUtils.e(" strNeedsRunHW : " + strNeedsRunScans);

                if (!TextUtils.isEmpty(strNeedsRunScans)) {
                    iNeedsRun.set(Integer.parseInt(strNeedsRunScans));
                }

                initPressureConfig(qrScanType, iNeedsRun.get());
 // fileStorage
                fileStorage = fileStorageWeakRef.get();
                if (fileStorage != null) {
                    fileStorage.deleteFile("DIKEScans.txt");
                    fileStorage.writeFile("runDone.txt", "0");
                }

                iRunDone.set(0);
                fileWriteBuffer.setLength(0);

                runOnUiThread(this::scansQrScan);
                break;

            case POSITION_SCANS_activity:
                Intent intentCamera = new Intent(activity, MainScansActivity.class);
                startActivity(intentCamera);
                break;*/

            case POSITION_SCANS_MYCAMERA:
                Intent intentScans = new Intent(activity, CameraActivity.class);
                startActivity(intentScans);
                break;

            case POSITION_CHECK_RUN_STARUS:
                appendInfoText(itemName);
                checkResult();
                break;
            case POSITION_CHECK_CHECK_CAMERA_FIXED_FOCUS_ZOOM:

                boolean backIsZoom = CameraFocusChecker.isBackCameraZoom(mContext);
                if ( backIsZoom){
                    appendText("判断后置:变焦（AF，带对焦马达）");
                }else {
                    appendText("判断后置:定焦（FF）");
                }

                boolean frontIsZoom = CameraFocusChecker.isFrontCameraZoom(mContext);
 // true → (AF, )
 // false → (FF)
                if ( frontIsZoom){
                    appendText("判断前置:变焦（AF，带对焦马达）");
                }else {
                    appendText("判断前置:定焦（FF）");
                }
                break;
        }
    }

    private void checkAndCacheScanPermission() {
        QRScanDivActivity activity = activityWeakRef.get();
        if (activity == null) return;

        boolean cameraGranted = ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
        boolean storageGranted = true;

        if (Build.VERSION.SDK_INT >= 33) {
            storageGranted = ContextCompat.checkSelfPermission(activity, "android.permission.READ_MEDIA_IMAGES") == PackageManager.PERMISSION_GRANTED;
        } else if (Build.VERSION.SDK_INT >= 23) {
            storageGranted = ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }

        isScanPermissionGranted = cameraGranted && (Build.VERSION.SDK_INT < 23 || storageGranted);
        LogUtils.e("权限缓存初始化：" + (isScanPermissionGranted ? "已授权" : "未授权"));
    }

    private void startHWDefinedScan() {
        QRScanDivActivity activity = activityWeakRef.get();
        if (activity == null) return;

        Intent intent = new Intent(activity, HWDefinedActivity.class);
        startActivityForResult(intent, REQUEST_CODE_DEFINE);
        overridePendingTransition(R.anim.anim_no_animation, R.anim.anim_no_animation);
    }

    private void startHWFrontScan() {
        QRScanDivActivity activity = activityWeakRef.get();
        if (activity == null) return;

        Intent intent = new Intent(activity, Camera2Activity.class);
        intent.putExtra(Camera2Activity.CAMERA_ID, Camera2Helper.CAMERA_ID_FRONT);
        intent.putExtra(Camera2Activity.DECODE_LIB, Camera2Helper.DECODE_LIB_HW);
        startActivityForResult(intent, REQUEST_CODE_CUSTOM_FRONT_CAMERA_BY_HW);
        overridePendingTransition(R.anim.anim_no_animation, R.anim.anim_no_animation);
    }

    private boolean checkCameraPermission() {
        QRScanDivActivity activity = activityWeakRef.get();
        if (activity == null) return false;

        return ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    private void initPressureConfig(int qrScanType, int needsRun) {
        try {
            JSONArray jsonArray = new JSONArray();
            JSONObject jsonObjectMsg = new JSONObject();
            jsonObjectMsg.put("needsRun", needsRun);
            jsonObjectMsg.put("qrScanType", qrScanType);
            jsonObjectMsg.put("qrScanTimeOut", 1000 * iTimeOut);

            FileStorageUtils fileStorage = fileStorageWeakRef.get();
            if (fileStorage != null) {
                fileStorage.writeFile("pressureConfig.txt", jsonArray.put(jsonObjectMsg).toString());
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void startScanTask() {
        singleExecutor.submit(() -> runOnUiThread(this::startScan));
    }

    void scansQrScan() {
        ISystemBinderService systemBinderService = systemBinderServiceWeakRef.get();
        if (systemBinderService == null) return;

        try {
            systemBinderService.scansOpenScancallback(2, true, 1, 0, 30, 1000 * iTimeOut, new ScanCallback.Stub() {
                @Override
                public void onScanSuccess(String result) throws RemoteException {
                    LogUtils.e(" 扫码成功 result :" + result);
                    appendText(" 扫码成功 result :" + result);
                    runOnUiThread(() -> saveQRScanResult("DIKEScans.txt", 3, 0));
                }

                @Override
                public void onScanFailed(String errorMsg) throws RemoteException {
                    LogUtils.e(" 扫码失败 errorMsg :" + errorMsg);
                    appendText(" 扫码失败 errorMsg :" + errorMsg);
                    runOnUiThread(() -> saveQRScanResult("DIKEScans.txt", 3, -1));
                }
            });
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void showFocusSettingDialog() {
        QRScanDivActivity activity = activityWeakRef.get();
        if (activity == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_focus_setting, null);

        Switch switchManualFocus = dialogView.findViewById(R.id.dialog_switch_manual_focus);
        SeekBar seekbarFocus = dialogView.findViewById(R.id.dialog_seekbar_focus);
        TextView tvFocusDistance = dialogView.findViewById(R.id.dialog_tv_focus_distance);

 // 30cm
        int defaultProgress = 30;
        seekbarFocus.setProgress(defaultProgress);
        tvFocusDistance.setText(String.format("对焦距离：%dcm", defaultProgress));

        seekbarFocus.setEnabled(false);

        switchManualFocus.setOnCheckedChangeListener((buttonView, isChecked) -> {
            seekbarFocus.setEnabled(isChecked);
 // 30cm
            if (!isChecked) {
                seekbarFocus.setProgress(defaultProgress);
                tvFocusDistance.setText(String.format("对焦距离：%dcm", defaultProgress));
            }
        });

        seekbarFocus.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int focusDistanceCm = Math.max(1, progress);
                tvFocusDistance.setText(String.format("对焦距离：%dcm", focusDistanceCm));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        builder.setView(dialogView)
                .setTitle("扫码设置")
 // [ ]: + , ( , )
                .setPositiveButton("确定", (dialog, which) -> {
                    boolean manualFocus = switchManualFocus.isChecked();
                    int focusDistance = seekbarFocus.getProgress();
                    LogUtils.e("手动对焦: " + manualFocus + ", 焦距: " + focusDistance + "cm");

 // Intent
                    Intent intentCamera = new Intent(QRScanDivActivity.this, MainScansActivity.class);
                    intentCamera.putExtra(EXTRA_MANUAL_FOCUS, manualFocus);
                    intentCamera.putExtra(EXTRA_FOCUS_DISTANCE, focusDistance);
                    startActivity(intentCamera);
                    dialog.dismiss();
                })
 // [ ]
                .setNegativeButton("取消", (dialog, which) -> dialog.dismiss())
 // [ ] : , 8cm
                .setNeutralButton("默认设置", (dialog, which) -> {
                    Intent intentCamera = new Intent(QRScanDivActivity.this, MainScansActivity.class);
                    intentCamera.putExtra(EXTRA_MANUAL_FOCUS, false);
                    intentCamera.putExtra(EXTRA_FOCUS_DISTANCE, 8);
                    startActivity(intentCamera);
                    dialog.dismiss();
                });

        AlertDialog dialog = builder.create();
        alertDialogWeakRef = new WeakReference<>(dialog);
        dialog.show();
    }

    void checkResult() {
        FileStorageUtils fileStorage = fileStorageWeakRef.get();
        if (fileStorage == null) return;

        String[] fileNames = {
                "pressureZXingResult.txt",
                "DEFINED_CODE.txt",
                "CUSTOM_FRONT_CAMERA_USE_HW.txt",
                "DIKEScans.txt"
        };

        for (String fileName : fileNames) {
            try {
                String fileContent = fileStorage.readFileSync(fileName);
                int countMinus1 = countOccurrences(fileContent, "\"qrScanresult\":-1");
                int count0 = countOccurrences(fileContent, "\"qrScanresult\":0");

                appendText("文件: " + fileName);
                appendText("  失败 数量: " + countMinus1);
                appendText("  成功 数量: " + count0);
                appendText("  总计: " + (countMinus1 + count0));
                appendText("-----------------------------");

            } catch (Exception e) {
                LogUtils.e("处理文件 " + fileName + " 时出错: " + e.getMessage());
            }
        }
    }

    private static int countOccurrences(String text, String pattern) {
        if (text == null || pattern == null || text.isEmpty()) {
            return 0;
        }

        int count = 0;
        int index = 0;
        int patternLength = pattern.length();

        while ((index = text.indexOf(pattern, index)) != -1) {
            count++;
            index += patternLength;
        }

        return count;
    }

    void setWaterMask() {
 // Activity , Context
        String libVersion = "";
        WatermarkUtils.addWatermark(QRScanDivActivity.this, libVersion, Color.BLUE, 24, 0, false, 3);
    }

    private void startScan() {
        QRScanDivActivity activity = activityWeakRef.get();
        if (activity == null) return;

        Intent intent = new Intent(activity, ZXingScannerActivity.class);
        startActivityForResult(intent, REQUEST_CODE_ZX_SCANNER);
        overridePendingTransition(R.anim.anim_no_animation, R.anim.anim_no_animation);
    }

    public void loadScanKitBtnClick(View view) {
        requestPermission(CAMERA_REQ_CODE, DECODE);
    }

    public void newViewBtnClick(View view) {
        requestPermission(DEFINED_CODE, DECODE);
    }

    public void bitmapBtnClick(View view) {
        requestPermission(BITMAP_CODE, DECODE);
    }

    public void multiProcessorSynBtnClick(View view) {
        requestPermission(MULTIPROCESSOR_SYN_CODE, DECODE);
    }

    public void multiProcessorAsynBtnClick(View view) {
        requestPermission(MULTIPROCESSOR_ASYN_CODE, DECODE);
    }

    public void generateQRCodeBtnClick(View view) {
        requestPermission(GENERATE_CODE, GENERATE);
    }

    private void requestPermission(int requestCode, int mode) {
        if (mode == DECODE) {
            decodePermission(requestCode);
        } else if (mode == GENERATE) {
            generatePermission(requestCode);
        }
    }

    private void decodePermission(int requestCode) {
        QRScanDivActivity activity = activityWeakRef.get();
        if (activity == null) return;

        String[] permissions;
        if (Build.VERSION.SDK_INT >= 33) {
            permissions = new String[]{Manifest.permission.CAMERA, "android.permission.READ_MEDIA_IMAGES"};
        } else {
            permissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE};
        }
        ActivityCompat.requestPermissions(this, permissions, requestCode);
    }

    private void generatePermission(int requestCode) {
        QRScanDivActivity activity = activityWeakRef.get();
        if (activity == null) return;

        String[] permissions;
        if (Build.VERSION.SDK_INT >= 33) {
            permissions = new String[]{"android.permission.MANAGE_EXTERNAL_STORAGE"};
        } else {
            permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
        }
        ActivityCompat.requestPermissions(this, permissions, requestCode);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (permissions == null || grantResults == null) {
            return;
        }

        boolean isPermissionGranted = false;
        if (grantResults.length > 0) {
            isPermissionGranted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
            if (grantResults.length > 1) {
                isPermissionGranted = isPermissionGranted && (grantResults[1] == PackageManager.PERMISSION_GRANTED);
            }
        }

        if (isPermissionGranted) {
            isScanPermissionGranted = true;
            LogUtils.e("权限申请成功，更新缓存状态为已授权");
        } else {
            QRScanDivActivity activity = activityWeakRef.get();
            if (activity != null) {
                Toast.makeText(activity, "权限申请失败，无法进行扫码测试", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && requestCode == GENERATE_CODE) {
            QRScanDivActivity activity = activityWeakRef.get();
            if (activity != null) {
                Intent intent = new Intent(activity, GenerateCodeActivity.class);
                startActivity(intent);
            }
        }

        if (isPermissionGranted) {
            switch (requestCode) {
                case PERMISSION_REQUEST_CODE:
                    startScanTask();
                    break;
                case DEFINED_CODE:
                    startHWDefinedScan();
                    break;
                case CUSTOM_FRONT_CAMERA_USE_HW:
                    startHWFrontScan();
                    break;
                case CAMERA_REQ_CODE:
                    ScanUtil.startScan(this, REQUEST_CODE_SCAN_ONE, new HmsScanAnalyzerOptions.Creator().create());
                    break;
                case BITMAP_CODE:
                    Intent intentBitmap = new Intent(this, CommonActivity.class);
                    intentBitmap.putExtra(DECODE_MODE, BITMAP_CODE);
                    startActivityForResult(intentBitmap, REQUEST_CODE_SCAN_MULTI);
                    overridePendingTransition(R.anim.anim_no_animation, R.anim.anim_no_animation);
                    break;
                case MULTIPROCESSOR_SYN_CODE:
                    Intent intentSyn = new Intent(this, CommonActivity.class);
                    intentSyn.putExtra(DECODE_MODE, MULTIPROCESSOR_SYN_CODE);
                    startActivityForResult(intentSyn, REQUEST_CODE_SCAN_MULTI);
                    overridePendingTransition(R.anim.anim_no_animation, R.anim.anim_no_animation);
                    break;
                case MULTIPROCESSOR_ASYN_CODE:
                    Intent intentAsyn = new Intent(this, CommonActivity.class);
                    intentAsyn.putExtra(DECODE_MODE, MULTIPROCESSOR_ASYN_CODE);
                    startActivityForResult(intentAsyn, REQUEST_CODE_SCAN_MULTI);
                    overridePendingTransition(R.anim.anim_no_animation, R.anim.anim_no_animation);
                    break;
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        LogUtils.e(" requestCode: " + requestCode + " resultCode: " + resultCode + " resultCode data: " + data);

        singleExecutor.submit(() -> {
            try {
                FileStorageUtils fileStorage = fileStorageWeakRef.get();
                if (fileStorage != null) {
                    String strPressure = fileStorage.readFileSync("pressureConfig.txt");
                    if (!TextUtils.isEmpty(strPressure)) {
                        JsonArray jsonArrayLocalNew = JsonParser.parseString(strPressure).getAsJsonArray();
                        JsonObject firstObject = jsonArrayLocalNew.get(0).getAsJsonObject();
                        iNeedsRun.set(firstObject.get("needsRun").getAsInt());
                    }
                }

                if (requestCode == REQUEST_CODE_ZX_SCANNER) {
                    if (data != null) {
                        String obj = data.getStringExtra(ZXingScannerActivity.SCAN_RESULT);
                        if (obj != null) {
                            appendText("ZXing");
                            appendText(obj);
                            appendInfoText("QRZxing Success ");
                            saveQRScanResult("pressureZXingResult.txt", -1, 0);
                        } else {
                            appendErrorText("QRZxing Fail ");
                            saveQRScanResult("pressureZXingResult.txt", -1, -1);
                        }
                    } else {
                        appendErrorText("QRZxing Fail ");
                        saveQRScanResult("pressureZXingResult.txt", -1, -1);
                    }
                }

                if (requestCode == REQUEST_CODE_DEFINE
                        || requestCode == REQUEST_CODE_CUSTOM_SCAN
                        || requestCode == REQUEST_CODE_CUSTOM_FRONT_CAMERA_BY_HW
                        || requestCode == REQUEST_CODE_CUSTOM_BACK_CAMERA_BY_HW
                        || requestCode == REQUEST_CODE_CUSTOM_BACK_CAMERA_FIXED_BY_HW) {

                    LogUtils.e("onActivityResult resultCode: " + resultCode);

                    HmsScan hmsScan = null;
                    if (data != null) {
                        if (requestCode == REQUEST_CODE_DEFINE) {
                            hmsScan = data.getParcelableExtra(HWDefinedActivity.SCAN_RESULT);
                            appendText("HMS");
                        } else if (requestCode == REQUEST_CODE_CUSTOM_SCAN) {
                            hmsScan = data.getParcelableExtra(ScanByHWActivity.SCAN_RESULT);
                            appendText("CUSTOM BACK CAMERA & HMS");
                        } else if (requestCode == REQUEST_CODE_CUSTOM_FRONT_CAMERA_BY_HW) {
                            hmsScan = data.getParcelableExtra(Camera2Activity.SCAN_RESULT);
                            appendText("CUSTOM FRONT CAMERA & HMS");
                        } else if (requestCode == REQUEST_CODE_CUSTOM_BACK_CAMERA_BY_HW) {
                            hmsScan = data.getParcelableExtra(Camera2Activity.SCAN_RESULT);
                            appendText("CUSTOM BACK CAMERA & HMS");
                        } else if (requestCode == REQUEST_CODE_CUSTOM_BACK_CAMERA_FIXED_BY_HW) {
                            hmsScan = data.getParcelableExtra(Camera2Activity.SCAN_RESULT);
                            appendText("CUSTOM BACK CAMERA FIXED & HMS");
                        }
                    }

                    if (hmsScan != null && data != null) {
                        LogUtils.e("onActivityResult scanType: " + scanType);
                        LogUtils.e("onActivityResult: " + "hmsScan 解析结果: " + hmsScan.getOriginalValue());

                        String fileName = "";
                        if (scanType == CUSTOM_BACK_FIXED_CAMERA_USE_HW) {
                            fileName = "CUSTOM_BACK_FIXED_CAMERA_USE_HW.txt";
                        } else if (scanType == CUSTOM_FRONT_CAMERA_USE_HW) {
                            fileName = "CUSTOM_FRONT_CAMERA_USE_HW.txt";
                        } else if (scanType == CUSTOM_BACK_CAMERA_USE_HW) {
                            fileName = "CUSTOM_BACK_CAMERA_USE_HW.txt";
                        } else if (scanType == DEFINED_CODE) {
                            fileName = "DEFINED_CODE.txt";
                        }

                        saveQRScanResult(fileName, scanType, 0);
                        appendText(hmsScan.getOriginalValue());
                    } else {
                        LogUtils.e("onActivityResult scanType: " + scanType);
                        appendText(scanType + " QRScan Fial");

                        String fileName = "";
                        if (scanType == CUSTOM_BACK_FIXED_CAMERA_USE_HW) {
                            fileName = "CUSTOM_BACK_FIXED_CAMERA_USE_HW.txt";
                        } else if (scanType == CUSTOM_FRONT_CAMERA_USE_HW) {
                            fileName = "CUSTOM_FRONT_CAMERA_USE_HW.txt";
                        } else if (scanType == CUSTOM_BACK_CAMERA_USE_HW) {
                            fileName = "CUSTOM_BACK_CAMERA_USE_HW.txt";
                        } else if (scanType == DEFINED_CODE) {
                            fileName = "DEFINED_CODE.txt";
                        }

                        saveQRScanResult(fileName, scanType, -1);
                    }
                }

                if (iRunDone.get() % 100 == 0) {
                    System.gc();
                    LogUtils.e("完成调用了系统清理内存方法");
                }

            } catch (Exception e) {
                LogUtils.e("处理ActivityResult异常: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    void saveQRScanResult(String fileName, int needScanType, int successOrFail) {
        try {
            JSONObject jsonObjectMsg = new JSONObject();
            jsonObjectMsg.put("qrScanresult", successOrFail);
            jsonObjectMsg.put("qrScanUseTime", "");
            jsonObjectMsg.put("timestamp", System.currentTimeMillis());

            fileWriteBuffer.append(jsonObjectMsg.toString()).append("\n");

            int currentCount = iRunDone.incrementAndGet();
            appendInfoText(getResources().getString(R.string.pressMsg_done) + currentCount+"/"+iNeedsRun.get());

            FileStorageUtils fileStorage = fileStorageWeakRef.get();
            if (fileStorage != null) {
                if (currentCount % BATCH_WRITE_THRESHOLD == 0 || currentCount >= iNeedsRun.get()) {
                    fileStorage.appendFile(fileName, fileWriteBuffer.toString());
                    fileWriteBuffer.setLength(0);
                    fileStorage.writeFile("runDone.txt", String.valueOf(currentCount));
                }
            }

            if (currentCount == iNeedsRun.get()) {
                bonBackPressed = true;
                if (fileStorage != null && fileWriteBuffer.length() > 0) {
                    fileStorage.appendFile(fileName, fileWriteBuffer.toString());
                    fileWriteBuffer.setLength(0);
                }
                return;
            }

            appendInfoText("needScanType ：" + needScanType);

            if (currentCount < iNeedsRun.get()) {
                scheduledExecutorService.schedule(() -> {
                    try {
                        runOnUiThread(() -> {
                            String toastMsg = "";
                            QRScanDivActivity activity = activityWeakRef.get();

                            if (needScanType == -1) {
                                toastMsg = "ZXing ：" + currentCount + "/" + iNeedsRun.get();
                                startScan();
                            } else if (needScanType == 3) {
                                toastMsg = "DIKE Scans ：" + currentCount + "/" + iNeedsRun.get();
                                scansQrScan();
                            } else {
                                switch (scanType) {
                                    case CUSTOM_BACK_FIXED_CAMERA_USE_HW:
                                        toastMsg = "CUSTOM_BACK_FIXED_CAMERA_USE_HW ：" + currentCount + "/" + iNeedsRun.get();
                                        requestPermission(CUSTOM_BACK_FIXED_CAMERA_USE_HW, DECODE);
                                        break;
                                    case CUSTOM_FRONT_CAMERA_USE_HW:
                                        toastMsg = "CUSTOM_FRONT_CAMERA_USE_HW ：" + currentCount + "/" + iNeedsRun.get();
                                        startHWFrontScan();
                                        break;
                                    case CUSTOM_BACK_CAMERA_USE_HW:
                                        toastMsg = "CUSTOM_BACK_CAMERA_USE_HW ：" + currentCount + "/" + iNeedsRun.get();
                                        requestPermission(CUSTOM_BACK_CAMERA_USE_HW, DECODE);
                                        break;
                                    case DEFINED_CODE:
                                        toastMsg = "DEFINED_CODE ：" + currentCount + "/" + iNeedsRun.get();
                                        startHWDefinedScan();
                                        break;
                                }
                            }

                            if (activity != null && !TextUtils.isEmpty(toastMsg)) {
                                Toast.makeText(activity, toastMsg, Toast.LENGTH_SHORT).show();
                            }
                        });
                    } catch (Exception e) {
                        LogUtils.e("执行下一次扫码异常: " + e.getMessage());
                        e.printStackTrace();
                    }
                }, 2, TimeUnit.SECONDS);
            }

        } catch (JSONException e) {
            e.printStackTrace();
            FileStorageUtils fileStorage = fileStorageWeakRef.get();
            if (fileStorage != null) {
                fileStorage.appendFile(fileName, "{\"qrScanresult\":" + successOrFail + ",\"error\":\"" + e.getMessage() + "\"}\n");
            }
        }
    }

    @Override
    protected void onPause() {
        LogUtils.e("onPause: ");
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        LogUtils.e("onDestroy: ");


        if (alertDialogWeakRef != null) {
            AlertDialog dialog = alertDialogWeakRef.get();
            if (dialog != null && dialog.isShowing()) {
                dialog.dismiss();
            }
            alertDialogWeakRef.clear();
        }

        if (broadcastReceiverWeakRef != null) {
            MyBroadcastReceiver receiver = broadcastReceiverWeakRef.get();
            if (receiver != null) {
                receiver.endObserver();
            }
            broadcastReceiverWeakRef.clear();
        }


        if (scheduledExecutorService != null) {
            scheduledExecutorService.shutdownNow();
        }
        if (singleExecutor != null) {
            singleExecutor.shutdownNow();
        }


        FileStorageUtils fileStorage = fileStorageWeakRef.get();
        if (fileStorage != null && fileWriteBuffer.length() > 0) {
            String fileName = "";
            switch (scanType) {
                case DEFINED_CODE:
                    fileName = "DEFINED_CODE.txt";
                    break;
                case CUSTOM_FRONT_CAMERA_USE_HW:
                    fileName = "CUSTOM_FRONT_CAMERA_USE_HW.txt";
                    break;
                case -1:
                    fileName = "pressureZXingResult.txt";
                    break;
                case 3:
                    fileName = "DIKEScans.txt";
                    break;
                default:
                    fileName = "DEFINED_CODE.txt";
            }
            fileStorage.appendFile(fileName, fileWriteBuffer.toString());
            fileWriteBuffer.setLength(0);
        }


        if (fileStorageWeakRef != null) {
            fileStorageWeakRef.clear();
        }
        if (systemBinderServiceWeakRef != null) {
            systemBinderServiceWeakRef.clear();
        }
        if (activityWeakRef != null) {
            activityWeakRef.clear();
        }

        super.onDestroy();
        WatermarkUtils.removeWatermark(this);
    }

    @Override
    protected void onResume() {
        LogUtils.e("onResume: ");
        super.onResume();
        checkAndCacheScanPermission();
    }

    @Override
    public void onServiceConnected(ISdkServiceManager sdkServiceManager) {
        try {
            ISystemBinderService service = ISystemBinderService.Stub.asInterface(sdkServiceManager.getService(ServiceID.SERVICE_ID_SYSTEM));
            systemBinderServiceWeakRef = new WeakReference<>(service);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {

    }

    public static void goToSleep(Context context) {

    }

    public static void wakeUp(Context context) {

    }

    private double getCpuUsage() {
        return 0.0;
    }

    private String[] readCpuInfo() {
        return new String[0];
    }

    @Override
    public void onBackPressed() {
        if (bonBackPressed) {
            LogUtils.e("任务执行完毕，页面能关闭: ");
            super.onBackPressed();
        } else {
            LogUtils.e("任务未执行完毕，页面不能关闭 ");
            QRScanDivActivity activity = activityWeakRef.get();
            if (activity != null) {
                Toast.makeText(activity, "任务未执行完毕，页面不能关闭", Toast.LENGTH_LONG).show();
            }
            return;
        }
    }
}