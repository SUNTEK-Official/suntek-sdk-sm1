package com.szanfu.sdk.test.activity.qrscancode;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Parcelable;
import android.os.PowerManager;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.szanfu.sdk.test.util.FileHelper;

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
import com.szanfu.sdk.test.hmscorescan.DisPlayActivity;
import com.szanfu.sdk.test.hmscorescan.DisPlayMulActivity;
import com.szanfu.sdk.test.hmscorescan.GenerateCodeActivity;
import com.szanfu.sdk.test.receiver.MyBroadcastReceiver;
import com.szanfu.sdk.test.util.LogUtils;
import com.szanfu.sdk.test.util.WatermarkUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.InvocationTargetException;

public class QrCodeMainActivity extends Activity implements ActivityCompat.OnRequestPermissionsResultCallback, View.OnClickListener {
    private int iCountNumberNeedsRun = 0;
    private int iRunSuccessful = 0;
    private int iUnsuccessful = 0;
    private int iIsRunsCount = 0;
    boolean isClosedActivity = true;
    private static final String TAG = "QrCodeMainActivity";
    private int scanType = DEFINED_CODE;
    private TextView scan_result;
    private Button btScan;
    private Button btGenerate;
    private Button btn_hms;
    private Button btn_scren;
    Button btn_CustomBackScanUseHW;
    Button btn_CustomFrontScanUseHW;
    Button btn_CustomBackScanUseAX;
    Button btn_CustomFrontScanUseAX;
    Button btn_CustomFixedFocusUseHW;
    Button btn_CustomFixedFocusUseAX;
    Button btn_ScanGoogleMLKit;
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
    private TextView codeFormat;
    private TextView resultType;
    private TextView rawResult;
    private TextView run_count;
    private TextView scan_framework;
    private TextView scan_time;
    private TextView opencamera_time;

    private int iRunSuccess=0;
    private int iRunError=0;
    private int iRunCount=0;
    private int iRunOrg = 1;
    private EditText ed_conut;
    private LinearLayout ll_count;

    private MyBroadcastReceiver broadcastReceiver;
    private TextView tv;
    private Button btn_check;
    private TextView scan_result_show;
    private CheckBox btn_checkbox;
    private EditText et_interval;
    private int iIntervalSeconds;
    private Handler handler = new Handler();
    private int iCheckScanType=0;
    protected Context mContext;
    private EditText  et_timeout;
    private int iTimeOut=1;
    private String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/sdcard/Android/DeviceMSG.txt";
    private static final int REQUEST_CODE_STORAGE_PERMISSION = 100;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.qrcode_activity_main);
        mContext = this;
        initView();
        init();

//        if (AppConfig.buildType != 0){
        setWaterMask();
//        }

    }

    void setWaterMask(){
        String libVersion="";
 // ( )
        WatermarkUtils.addWatermark(this,
                libVersion,
                Color.BLUE, 24, 0, false, 3);
    }


    private String readFile() {
        File file = new File(Environment.getExternalStorageDirectory() + "/Android/DeviceMSG.txt");
        if(!file.exists())
            return "";
        StringBuilder stringBuilder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)))) {
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        LogUtils.e("stringBuilder.toString() :"+stringBuilder.toString());
        return stringBuilder.toString();
    }



    private void initView() {

        scan_result = findViewById(R.id.scan_result);
        codeFormat = findViewById(R.id.barcode_type);
        resultType = findViewById(R.id.barcode_type_mon);
        rawResult = findViewById(R.id.barcode_rawValue);
        run_count = findViewById(R.id.run_count);
        ed_conut = findViewById(R.id.et_count_no);
        ll_count = findViewById(R.id.ll_count);
        scan_framework = findViewById(R.id.scan_framework);
        scan_time = findViewById(R.id.scan_time);
        opencamera_time = findViewById(R.id.opencamera_time);
        btn_CustomBackScanUseHW = findViewById(R.id.btn_backcamera_hw);
        btn_CustomBackScanUseHW.setOnClickListener(this);
        btn_CustomFrontScanUseHW = findViewById(R.id.btn_frontcamera_hw);
        btn_CustomFrontScanUseHW.setOnClickListener(this);
        btn_CustomBackScanUseAX = findViewById(R.id.btn_backcamera_ax);
        btn_CustomBackScanUseAX.setOnClickListener(this);
        btn_CustomFrontScanUseAX = findViewById(R.id.btn_frontcamera_ax);
        btn_CustomFrontScanUseAX.setOnClickListener(this);
        btn_CustomFixedFocusUseHW = findViewById(R.id.btn_custom_hms);
        btn_CustomFixedFocusUseHW.setOnClickListener(this);
        btn_CustomFixedFocusUseAX = findViewById(R.id.btn_camera_ax);
        btn_CustomFixedFocusUseAX.setOnClickListener(this);
        btn_ScanGoogleMLKit = findViewById(R.id.btn_scan_googlemlkit);
        btn_ScanGoogleMLKit.setOnClickListener(this);

        btn_check = findViewById(R.id.btn_check);
        btn_check.setOnClickListener(this);
        scan_result_show = findViewById(R.id.scan_result_show);

//        ll_count.setVisibility(View.VISIBLE);
        btn_scren =findViewById(R.id.btn_scren);
        btn_scren.setOnClickListener(this);
        btScan = (Button) findViewById(R.id.bt_scan);
        btScan.setOnClickListener(this);

        btGenerate = (Button) findViewById(R.id.bt_generate);
        btGenerate.setOnClickListener(this);

        btn_hms = findViewById(R.id.btn_hms);
        btn_hms.setOnClickListener(this);

        et_timeout = findViewById(R.id.et_timeout);

        btn_checkbox = findViewById(R.id.btn_checkbox);
        btn_checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                LogUtils.e( "是否选中调用系统方法清理内存: "+btn_checkbox.isChecked() );
            }
        });
        et_interval = findViewById(R.id.et_interval);
        if (Build.VERSION.SDK_INT >= 25 /*Build.VERSION_CODES.N_MR1*/) {
            if (ContextCompat.checkSelfPermission(QrCodeMainActivity.this,
                    Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(QrCodeMainActivity.this,
                        new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST_CODE);
            }
        }

    }

/*    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (PERMISSION_REQUEST_CODE == requestCode) {
            if (Manifest.permission.CAMERA.equals(permissions[0])) {
                if (PackageManager.PERMISSION_GRANTED == grantResults[0]) {
                    startScan();
                } else {
                    Toast.makeText(QrCodeMainActivity.this, "没有相机权限", Toast.LENGTH_LONG).show();
                }
            }
        }
    }*/


    private void startScan() {
//        ScannerActivity.startActivity(this);
        LogUtils.e( "startScan getMemoryInfo: \n"+getMemoryInfo());
        LogUtils.e( "startScan: "+"Success :"+iRunSuccessful+
                "Error :"+iUnsuccessful+
                "需要执行任务次数："+ iCountNumberNeedsRun);
        Intent intent = new Intent(QrCodeMainActivity.this, ZXingScannerActivity.class);
        if (et_timeout.getText().toString().trim().isEmpty()) {
            iTimeOut = 1*1000*5;
            LogUtils.e("sacnQrCodetimeOut :"+iTimeOut);
        } else {
            iTimeOut = Integer.parseInt(et_timeout.getText().toString().trim())*1000*60;
            LogUtils.e("sacnQrCodetimeOut :"+iTimeOut);
        }
        intent.putExtra("sacnQrCodetimeOut",iTimeOut);
        long startActivityTime = System.currentTimeMillis();
        LogUtils.e("ScannerActivity 启动时间 "+ startActivityTime);
        intent.putExtra("startActivityTime",startActivityTime);
        this.startActivityForResult(intent, REQUEST_CODE_ZX_SCANNER);
 // " " , 0
        overridePendingTransition(R.anim.anim_no_animation, R.anim.anim_no_animation);
    }


    /**
     * Call the default view.
     */
    public void loadScanKitBtnClick(View view) {
        requestPermission(CAMERA_REQ_CODE, DECODE);
    }

    /**
     * Call the customized view.
     */
    public void newViewBtnClick(View view) {
        requestPermission(DEFINED_CODE, DECODE);
    }

    /**
     * Call the bitmap.
     */
    public void bitmapBtnClick(View view) {
        requestPermission(BITMAP_CODE, DECODE);
    }

    /**
     * Call the MultiProcessor API in synchronous mode.
     */
    public void multiProcessorSynBtnClick(View view) {
        requestPermission(MULTIPROCESSOR_SYN_CODE, DECODE);
    }

    /**
     * Call the MultiProcessor API in asynchronous mode.
     */
    public void multiProcessorAsynBtnClick(View view) {
        requestPermission(MULTIPROCESSOR_ASYN_CODE, DECODE);
    }

    /**
     * Start generating the barcode.
     */
    public void generateQRCodeBtnClick(View view) {
        requestPermission(GENERATE_CODE, GENERATE);
    }

    /**
     * Apply for permissions.
     */
    private void requestPermission(int requestCode, int mode) {
        if (mode == DECODE) {
            decodePermission(requestCode);
        } else if (mode == GENERATE) {
            generatePermission(requestCode);
        }
    }

    /**
     * Apply for permissions.
     */
    private void decodePermission(int requestCode) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        if (Build.VERSION.SDK_INT >= 33) {
            ActivityCompat.requestPermissions(
                    this,
//                    new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_MEDIA_IMAGES},
                    new String[]{Manifest.permission.CAMERA,"android.permission.READ_MEDIA_IMAGES"},
                    requestCode);
        } else {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE},
                    requestCode);
        }
    }

    /**
     * Apply for permissions.
     */
    private void generatePermission(int requestCode) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        if (Build.VERSION.SDK_INT >= 33) {

            ActivityCompat.requestPermissions(
                    this,
//                    new String[]{Manifest.permission.MANAGE_EXTERNAL_STORAGE},
                    new String[]{"android.permission.MANAGE_EXTERNAL_STORAGE"},
                    requestCode);
        } else {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    requestCode);
        }
    }

    /**
     * Call back the permission application result. If the permission application is successful, the barcode scanning view will be displayed.
     * @param requestCode Permission application code.
     * @param permissions Permission array.
     * @param grantResults: Permission application result array.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (permissions == null || grantResults == null) {
            return;
        }

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && requestCode == GENERATE_CODE) {
            Intent intent = new Intent(this, GenerateCodeActivity.class);
            this.startActivity(intent);
        }

        if (grantResults.length < 2 || grantResults[0] != PackageManager.PERMISSION_GRANTED || grantResults[1] != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        //Default View Mode
        if (requestCode == CAMERA_REQ_CODE) {
            ScanUtil.startScan(this, REQUEST_CODE_SCAN_ONE, new HmsScanAnalyzerOptions.Creator().create());
        }
        //Customized View Mode
        if (requestCode == DEFINED_CODE) {
            Intent intent = new Intent(this, HWDefinedActivity.class);
            if (et_timeout.getText().toString().trim().isEmpty()) {
                iTimeOut = 1*1000*5;
                LogUtils.e("sacnQrCodetimeOut :"+iTimeOut);
            } else {
                iTimeOut = Integer.parseInt(et_timeout.getText().toString().trim())*1000*60;
                LogUtils.e("sacnQrCodetimeOut :"+iTimeOut);
            }
            long startActivityTime = System.currentTimeMillis();
            LogUtils.e("ScannerActivity 启动时间 "+ startActivityTime);
            intent.putExtra("startActivityTime",startActivityTime);
            intent.putExtra("sacnQrCodetimeOut",iTimeOut);
            this.startActivityForResult(intent, REQUEST_CODE_DEFINE);
 // " " , 0
            overridePendingTransition(R.anim.anim_no_animation, R.anim.anim_no_animation);
        }
        //Custom Camera Scan Code
        if(requestCode == CUSTOM_CODE){
            Intent intent = new Intent(this, ScanByHWActivity.class);
            long startActivityTime = System.currentTimeMillis();
            LogUtils.e("ScannerActivity 启动时间 "+ startActivityTime);
            intent.putExtra("startActivityTime",startActivityTime);
            intent.putExtra("sacnQrCodetimeOut",iTimeOut);
            this.startActivityForResult(intent, REQUEST_CODE_CUSTOM_SCAN);
 // " " , 0
            overridePendingTransition(R.anim.anim_no_animation, R.anim.anim_no_animation);
        }
        if(requestCode == CUSTOM_BACK_FIXED_CAMERA_USE_HW){
            Intent intent = new Intent(this, Camera2Activity.class);
            intent.putExtra(Camera2Activity.CAMERA_ID,Camera2Helper.CAMERA_ID_BACK);
            intent.putExtra(Camera2Activity.DECODE_LIB, Camera2Helper.DECODE_LIB_HW);
            intent.putExtra(Camera2Activity.IS_FIXED_FOUCUS, true);
            if (et_timeout.getText().toString().trim().isEmpty()) {
                iTimeOut = 1*1000*5;
                LogUtils.e("sacnQrCodetimeOut :"+iTimeOut);
            } else {
                iTimeOut = Integer.parseInt(et_timeout.getText().toString().trim())*1000*60;
                LogUtils.e("sacnQrCodetimeOut :"+iTimeOut);
            }
            intent.putExtra("sacnQrCodetimeOut",iTimeOut);
            long startActivityTime = System.currentTimeMillis();
            LogUtils.e("ScannerActivity 启动时间 "+ startActivityTime);
            intent.putExtra("startActivityTime",startActivityTime);
            this.startActivityForResult(intent, REQUEST_CODE_CUSTOM_BACK_CAMERA_FIXED_BY_HW);
 // " " , 0
            overridePendingTransition(R.anim.anim_no_animation, R.anim.anim_no_animation);
        }
        if(requestCode == CUSTOM_BACK_FIXED_CAMERA_USE_AX){
            Intent intent = new Intent(this, Camera2Activity.class);
            intent.putExtra(Camera2Activity.CAMERA_ID,Camera2Helper.CAMERA_ID_BACK);
            intent.putExtra(Camera2Activity.DECODE_LIB, Camera2Helper.DECODE_LIB_AX);
            intent.putExtra(Camera2Activity.IS_FIXED_FOUCUS, true);
            if (et_timeout.getText().toString().trim().isEmpty()) {
                iTimeOut = 1*1000*5;
                LogUtils.e("sacnQrCodetimeOut :"+iTimeOut);
            } else {
                iTimeOut = Integer.parseInt(et_timeout.getText().toString().trim())*1000*60;
                LogUtils.e("sacnQrCodetimeOut :"+iTimeOut);
            }
            intent.putExtra("sacnQrCodetimeOut",iTimeOut);
            long startActivityTime = System.currentTimeMillis();
            LogUtils.e("ScannerActivity 启动时间 "+ startActivityTime);
            intent.putExtra("startActivityTime",startActivityTime);
            this.startActivityForResult(intent, REQUEST_CODE_CUSTOM_BACK_CAMERA_FIXED_BY_AX);
 // " " , 0
            overridePendingTransition(R.anim.anim_no_animation, R.anim.anim_no_animation);
        }
        //Custom Front Camera Scan Code By HW
        if(requestCode == CUSTOM_FRONT_CAMERA_USE_HW){
            Intent intent = new Intent(this, Camera2Activity.class);
            intent.putExtra(Camera2Activity.CAMERA_ID,Camera2Helper.CAMERA_ID_FRONT);
            intent.putExtra(Camera2Activity.DECODE_LIB, Camera2Helper.DECODE_LIB_HW);
            if (et_timeout.getText().toString().trim().isEmpty()) {
                iTimeOut = 1*1000*5;
                LogUtils.e("sacnQrCodetimeOut :"+iTimeOut);
            } else {
                iTimeOut = Integer.parseInt(et_timeout.getText().toString().trim())*1000*60;
                LogUtils.e("sacnQrCodetimeOut :"+iTimeOut);
            }
            intent.putExtra("sacnQrCodetimeOut",iTimeOut);
            long startActivityTime = System.currentTimeMillis();
            LogUtils.e("ScannerActivity 启动时间 "+ startActivityTime);
            intent.putExtra("startActivityTime",startActivityTime);
            this.startActivityForResult(intent, REQUEST_CODE_CUSTOM_FRONT_CAMERA_BY_HW);
 // " " , 0
            overridePendingTransition(R.anim.anim_no_animation, R.anim.anim_no_animation);
        }
        //Custom Back Camera Scan Code By HW
        if(requestCode == CUSTOM_BACK_CAMERA_USE_HW){
            Intent intent = new Intent(this, Camera2Activity.class);
            intent.putExtra(Camera2Activity.CAMERA_ID,Camera2Helper.CAMERA_ID_BACK);
            intent.putExtra(Camera2Activity.DECODE_LIB, Camera2Helper.DECODE_LIB_HW);
            if (et_timeout.getText().toString().trim().isEmpty()) {
                iTimeOut = 1*1000*5;
                LogUtils.e("sacnQrCodetimeOut :"+iTimeOut);
            } else {
                iTimeOut = Integer.parseInt(et_timeout.getText().toString().trim())*1000*60;
                LogUtils.e("sacnQrCodetimeOut :"+iTimeOut);
            }
            intent.putExtra("sacnQrCodetimeOut",iTimeOut);
            long startActivityTime = System.currentTimeMillis();
            LogUtils.e("ScannerActivity 启动时间 "+ startActivityTime);
            intent.putExtra("startActivityTime",startActivityTime);
            this.startActivityForResult(intent, REQUEST_CODE_CUSTOM_BACK_CAMERA_BY_HW);
 // " " , 0
            overridePendingTransition(R.anim.anim_no_animation, R.anim.anim_no_animation);
        }
        //Custom Front Camera Scan Code By AX
        if(requestCode == CUSTOM_FRONT_CAMERA_USE_AX){
            Intent intent = new Intent(this, Camera2Activity.class);
            intent.putExtra(Camera2Activity.CAMERA_ID,Camera2Helper.CAMERA_ID_FRONT);
            intent.putExtra(Camera2Activity.DECODE_LIB, Camera2Helper.DECODE_LIB_AX);
            this.startActivityForResult(intent, REQUEST_CODE_CUSTOM_FRONT_CAMERA_BY_AX);
 // " " , 0
            overridePendingTransition(R.anim.anim_no_animation, R.anim.anim_no_animation);
        }
        //Custom Back Camera Scan Code By AnXin
        if(requestCode == CUSTOM_BACK_CAMERA_USE_AX){
            Intent intent = new Intent(this, Camera2Activity.class);
            intent.putExtra(Camera2Activity.CAMERA_ID,Camera2Helper.CAMERA_ID_BACK);
            intent.putExtra(Camera2Activity.DECODE_LIB, Camera2Helper.DECODE_LIB_AX);
            this.startActivityForResult(intent, REQUEST_CODE_CUSTOM_BACK_CAMERA_BY_AX);
 // " " , 0
            overridePendingTransition(R.anim.anim_no_animation, R.anim.anim_no_animation);
        }
        if(requestCode == SCAN_GOOGLE_MLKIT){
            Intent intent = new Intent(this, QrScanActivity.class);
            this.startActivityForResult(intent, REQUEST_CODE_BY_GOOGLE_MLKIT);
 // " " , 0
            overridePendingTransition(R.anim.anim_no_animation, R.anim.anim_no_animation);
        }
        //Bitmap Mode
        if (requestCode == BITMAP_CODE) {
            Intent intent = new Intent(this, CommonActivity.class);
            intent.putExtra(DECODE_MODE, BITMAP_CODE);
            if (et_timeout.getText().toString().trim().isEmpty()) {
                iTimeOut = 1*1000*5;
                LogUtils.e("sacnQrCodetimeOut :"+iTimeOut);
            } else {
                iTimeOut = Integer.parseInt(et_timeout.getText().toString().trim())*1000*60;
                LogUtils.e("sacnQrCodetimeOut :"+iTimeOut);
            }
            intent.putExtra("sacnQrCodetimeOut",iTimeOut);
            this.startActivityForResult(intent, REQUEST_CODE_SCAN_MULTI);
 // " " , 0
            overridePendingTransition(R.anim.anim_no_animation, R.anim.anim_no_animation);
        }
        //Multiprocessor Synchronous Mode
        if (requestCode == MULTIPROCESSOR_SYN_CODE) {
            Intent intent = new Intent(this, CommonActivity.class);
            intent.putExtra(DECODE_MODE, MULTIPROCESSOR_SYN_CODE);
            if (et_timeout.getText().toString().trim().isEmpty()) {
                iTimeOut = 1*1000*5;
                LogUtils.e("sacnQrCodetimeOut :"+iTimeOut);
            } else {
                iTimeOut = Integer.parseInt(et_timeout.getText().toString().trim())*1000*60;
                LogUtils.e("sacnQrCodetimeOut :"+iTimeOut);
            }
            intent.putExtra("sacnQrCodetimeOut",iTimeOut);
            this.startActivityForResult(intent, REQUEST_CODE_SCAN_MULTI);
 // " " , 0
            overridePendingTransition(R.anim.anim_no_animation, R.anim.anim_no_animation);
        }
        //Multiprocessor Asynchronous Mode
        if (requestCode == MULTIPROCESSOR_ASYN_CODE) {
            Intent intent = new Intent(this, CommonActivity.class);
            intent.putExtra(DECODE_MODE, MULTIPROCESSOR_ASYN_CODE);
            if (et_timeout.getText().toString().trim().isEmpty()) {
                iTimeOut = 1*1000*5;
                LogUtils.e("sacnQrCodetimeOut :"+iTimeOut);
            } else {
                iTimeOut = Integer.parseInt(et_timeout.getText().toString().trim())*1000*60;
                LogUtils.e("sacnQrCodetimeOut :"+iTimeOut);
            }
            intent.putExtra("sacnQrCodetimeOut",iTimeOut);
            this.startActivityForResult(intent, REQUEST_CODE_SCAN_MULTI);
 // " " , 0
            overridePendingTransition(R.anim.anim_no_animation, R.anim.anim_no_animation);
        }
    }

    /**
     * Event for receiving the activity result.
     *
     * @param requestCode Request code.
     * @param resultCode Result code.
     * @param data        Result.
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        LogUtils.e( "onActivityResult requestCode: "+requestCode );
        LogUtils.e( "onActivityResult resultCode: "+resultCode );
        LogUtils.e( "onActivityResult resultCode data: "+data );

        if(requestCode == REQUEST_CODE_ZX_SCANNER){
            if (data != null){
                String obj = data.getStringExtra(ZXingScannerActivity.SCAN_RESULT);
                long time = data.getLongExtra(ZXingScannerActivity.TIME,-1);
                long initCamerausedTime = data.getLongExtra(ZXingScannerActivity.INITCAMERTIME,-1);
                if (obj != null) {
 // Zxing
                    iRunSuccessful = iRunSuccessful+1;
                    saveLocalFile(
                            getResources().getString(R.string.task_interval)+iIntervalSeconds+"S\n"+
                                    getResources().getString(R.string.system_memory_cleanup)+btn_checkbox.isChecked()+"\n"+
                                    getResources().getString(R.string.scanning_method)+"Zxing :"+"\n"+

                                    getResources().getString(R.string.memory_usage_rate)+getMemoryInfo()+"\n"

                                    +getResources().getString(R.string.task_execution)+"Success :"+iRunSuccessful+
                                    "Error :"+iUnsuccessful+
                                    getResources().getString(R.string.needtoRunCount)+ iCountNumberNeedsRun );

                    scan_result.setText("Success ："+iRunSuccessful
                            +"Error :"+iUnsuccessful
                    );
                    Log.e(TAG, "startScan getMemoryInfo: \n"+getMemoryInfo());
                    Log.e(TAG, "startScan: "+"Success :"+iRunSuccessful+
                            "Error :"+iUnsuccessful+
                            "需要执行任务次数："+ iCountNumberNeedsRun);
                    Log.e(TAG, "onActivityResult Zxing扫码成功次数: "+iRunSuccessful );
                    if (iRunSuccessful+iUnsuccessful < iCountNumberNeedsRun){
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Log.e(TAG, "runZxing扫码成功-------------------再次执行扫码: " );
                                startScan();
                            }
                        }, iIntervalSeconds);
                    }else {
                        setButtonEnableTrue();
                    }
                    scan_framework.setText("ZXing");
                    rawResult.setText(obj);
                    scan_time.setText(String.valueOf(time));
                    opencamera_time.setText(String.valueOf(initCamerausedTime));
                }
            } else {
 // Zxing
                Log.e(TAG, "Zxing扫码失败 REQUEST_CODE_ZX_SCANNER onActivityResult: " );
                iUnsuccessful = iUnsuccessful+1;

                saveLocalFile(
                        getResources().getString(R.string.task_interval)+iIntervalSeconds+"S\n"+
                                getResources().getString(R.string.system_memory_cleanup)+btn_checkbox.isChecked()+"\n"+
                                getResources().getString(R.string.scanning_method)+"Zxing"+"\n"+
                                getResources().getString(R.string.memory_usage_rate)+getMemoryInfo()+"\n"
                                +getResources().getString(R.string.task_execution)+"Success :"+iRunSuccessful+
                                "Error :"+iUnsuccessful+
                                getResources().getString(R.string.needtoRunCount)+ iCountNumberNeedsRun );
                scan_result.setText("Success ："+iRunSuccessful
                        +"Error :"+iUnsuccessful
                );
                Log.e(TAG, "startScan getMemoryInfo: \n"+getMemoryInfo());
                Log.e(TAG, "startScan: "+"Success :"+iRunSuccessful+
                        "Error :"+iUnsuccessful+
                        "需要执行任务次数："+ iCountNumberNeedsRun);
                if (iRunSuccessful+iUnsuccessful < iCountNumberNeedsRun){

                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Log.e(TAG, "runZxing扫码失败-------------------再次执行扫码: " );
                            startScan();
                        }
                    }, iIntervalSeconds);

                }else {
                    setButtonEnableTrue();
                }
                Log.e(TAG, "onActivityResult Zxing扫码失败: "+iUnsuccessful );
            }
        }
 // ******************************** ********************************
        if (requestCode == REQUEST_CODE_DEFINE
                || requestCode == REQUEST_CODE_CUSTOM_SCAN
                || requestCode == REQUEST_CODE_CUSTOM_FRONT_CAMERA_BY_HW
                || requestCode == REQUEST_CODE_CUSTOM_BACK_CAMERA_BY_HW
                || requestCode == REQUEST_CODE_CUSTOM_BACK_CAMERA_FIXED_BY_HW) {

            Log.e(TAG, "onActivityResult resultCode: "+resultCode );

            HmsScan hmsScan = null;
            if (data != null){
                if(requestCode == REQUEST_CODE_DEFINE){
                    hmsScan = data.getParcelableExtra(HWDefinedActivity.SCAN_RESULT);
                    scan_framework.setText("HMS");
                    long time = data.getLongExtra(HWDefinedActivity.TIME,-1);
                    long initCamerausedTime = data.getLongExtra(HWDefinedActivity.INITCAMERTIME,-1);
                    LogUtils.e("initCamerausedTime : "+ initCamerausedTime);
                    scan_time.setText(String.valueOf(time));
                    opencamera_time.setText(String.valueOf(initCamerausedTime));
                }else if(requestCode == REQUEST_CODE_CUSTOM_SCAN){
                    hmsScan = data.getParcelableExtra(ScanByHWActivity.SCAN_RESULT);
                    scan_framework.setText("CUSTOM BACK CAMERA & HMS");
                    long time = data.getLongExtra(ScanByHWActivity.SCAN_TIME,-1);
                    long initCamerausedTime = data.getLongExtra(ScanByHWActivity.INITCAMERTIME,-1);
                    LogUtils.e("initCamerausedTime : "+initCamerausedTime);
                    scan_time.setText(String.valueOf(time));
                    opencamera_time.setText(String.valueOf(initCamerausedTime));
                } else if(requestCode == REQUEST_CODE_CUSTOM_FRONT_CAMERA_BY_HW){
                    hmsScan = data.getParcelableExtra(Camera2Activity.SCAN_RESULT);
                    scan_framework.setText("CUSTOM FRONT CAMERA & HMS");
                    long time = data.getLongExtra(Camera2Activity.SCAN_TIME,-1);
                    long initCamerausedTime = data.getLongExtra(Camera2Activity.INITCAMERTIME,-1);
                    scan_time.setText(String.valueOf(time));
                    opencamera_time.setText(String.valueOf(initCamerausedTime));
                } else if(requestCode == REQUEST_CODE_CUSTOM_BACK_CAMERA_BY_HW) {
                    hmsScan = data.getParcelableExtra(Camera2Activity.SCAN_RESULT);
                    scan_framework.setText("CUSTOM BACK CAMERA & HMS");
                    long time = data.getLongExtra(Camera2Activity.SCAN_TIME,-1);
                    long initCamerausedTime = data.getLongExtra(Camera2Activity.INITCAMERTIME,-1);
                    scan_time.setText(String.valueOf(time));
                    opencamera_time.setText(String.valueOf(initCamerausedTime));
                } else if(requestCode == REQUEST_CODE_CUSTOM_BACK_CAMERA_FIXED_BY_HW){
                    hmsScan = data.getParcelableExtra(Camera2Activity.SCAN_RESULT);
                    scan_framework.setText("CUSTOM BACK CAMERA FIXED & HMS");
                    long time = data.getLongExtra(Camera2Activity.SCAN_TIME,-1);
                    long initCamerausedTime = data.getLongExtra(Camera2Activity.INITCAMERTIME,-1);
                    scan_time.setText(String.valueOf(time));
                    opencamera_time.setText(String.valueOf(initCamerausedTime));

                }
            }
            String typeScan = "";
            if ( scanType == 222 ){
                typeScan = "DEFINED_CODE";
            }else if (888 == scanType){
                typeScan = "CUSTOM_FRONT_CAMERA_USE_HW";
            }else if (999 == scanType){
                typeScan = "CUSTOM_BACK_CAMERA_USE_HW";
            }else if ( 1002 == scanType){
                typeScan = "CUSTOM_BACK_FIXED_CAMERA_USE_HW";
            }
            if (hmsScan != null && data!=null) {
                Log.e(TAG, "onActivityResult scanType: "+scanType );
                Log.e(TAG, "onActivityResult: "+"hmsScan 解析结果: " + hmsScan.getOriginalValue());
                iRunSuccessful = iRunSuccessful+1;

                scan_result.setText("Success ："+iRunSuccessful
                        +"Error :"+iUnsuccessful
                );


                saveLocalFile(
                        getResources().getString(R.string.task_interval)+iIntervalSeconds+"S\n"+
                                getResources().getString(R.string.system_memory_cleanup)+btn_checkbox.isChecked()+"\n"+
                                getResources().getString(R.string.scanning_method)+typeScan+"\n"+
                                getResources().getString(R.string.memory_usage_rate)+getMemoryInfo()+"\n"
                                +getResources().getString(R.string.task_execution)+"Success :"+iRunSuccessful+
                                "Error :"+iUnsuccessful+
                                getResources().getString(R.string.needtoRunCount)+ iCountNumberNeedsRun );

                String strReadFile = FileHelper.readFromFile(this,"MonitoringInformation.txt");
                scan_result_show.setText(strReadFile);

                Log.e(TAG, "onActivityResult 华为扫码库扫码成功次数: "+iRunSuccessful );
                if(iRunSuccessful+iUnsuccessful < iCountNumberNeedsRun){

                    Log.e(TAG, "startScan getMemoryInfo: \n"+getMemoryInfo());
                    Log.e(TAG, "startScan: "+"Success :"+iRunSuccessful+
                            "Error :"+iUnsuccessful+
                            "需要执行任务次数："+ iCountNumberNeedsRun);

                    if (scanType == CUSTOM_BACK_FIXED_CAMERA_USE_HW ){

                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                requestPermission(CUSTOM_BACK_FIXED_CAMERA_USE_HW, DECODE);
                            }
                        }, iIntervalSeconds);



                    }else if (scanType == CUSTOM_FRONT_CAMERA_USE_HW ){
                        requestPermission(CUSTOM_FRONT_CAMERA_USE_HW, DECODE);
                    }else if (scanType == CUSTOM_BACK_CAMERA_USE_HW){
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                requestPermission(CUSTOM_BACK_CAMERA_USE_HW, DECODE);
                            }
                        }, iIntervalSeconds);

                    }else if (scanType == DEFINED_CODE){
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                requestPermission(DEFINED_CODE, DECODE);
                            }
                        }, iIntervalSeconds);


                    }
                }else {
                    setButtonEnableTrue();
                }
                rawResult.setText(hmsScan.getOriginalValue());
                if (hmsScan.getScanType() == HmsScan.QRCODE_SCAN_TYPE) {
                    codeFormat.setText("QR code");
                } else if (hmsScan.getScanType() == HmsScan.AZTEC_SCAN_TYPE) {
                    codeFormat.setText("AZTEC code");
                } else if (hmsScan.getScanType() == HmsScan.DATAMATRIX_SCAN_TYPE) {
                    codeFormat.setText("DATAMATRIX code");
                } else if (hmsScan.getScanType() == HmsScan.PDF417_SCAN_TYPE) {
                    codeFormat.setText("PDF417 code");
                } else if (hmsScan.getScanType() == HmsScan.CODE93_SCAN_TYPE) {
                    codeFormat.setText("CODE93");
                } else if (hmsScan.getScanType() == HmsScan.CODE39_SCAN_TYPE) {
                    codeFormat.setText("CODE39");
                } else if (hmsScan.getScanType() == HmsScan.CODE128_SCAN_TYPE) {
                    codeFormat.setText("CODE128");

                } else if (hmsScan.getScanType() == HmsScan.EAN13_SCAN_TYPE) {
                    codeFormat.setText("EAN13 code");

                } else if (hmsScan.getScanType() == HmsScan.EAN8_SCAN_TYPE) {
                    codeFormat.setText("EAN8 code");

                } else if (hmsScan.getScanType() == HmsScan.ITF14_SCAN_TYPE) {
                    codeFormat.setText("ITF14 code");

                } else if (hmsScan.getScanType() == HmsScan.UPCCODE_A_SCAN_TYPE) {
                    codeFormat.setText("UPCCODE_A");

                } else if (hmsScan.getScanType() == HmsScan.UPCCODE_E_SCAN_TYPE) {
                    codeFormat.setText("UPCCODE_E");

                } else if (hmsScan.getScanType() == HmsScan.CODABAR_SCAN_TYPE) {
                    codeFormat.setText("CODABAR");
                }


                //Show the barcode result.
                if (hmsScan.getScanType() == HmsScan.QRCODE_SCAN_TYPE) {
                    resultType.setVisibility(View.VISIBLE);

                    if (hmsScan.getScanTypeForm() == HmsScan.PURE_TEXT_FORM) {

                        resultType.setText("Text");

                    } else if (hmsScan.getScanTypeForm() == HmsScan.EVENT_INFO_FORM) {

                        resultType.setText("Event");

                    } else if (hmsScan.getScanTypeForm() == HmsScan.CONTACT_DETAIL_FORM) {


                        resultType.setText("Contact");

                    } else if (hmsScan.getScanTypeForm() == HmsScan.DRIVER_INFO_FORM) {

                        resultType.setText("License");

                    } else if (hmsScan.getScanTypeForm() == HmsScan.EMAIL_CONTENT_FORM) {

                        resultType.setText("Email");

                    } else if (hmsScan.getScanTypeForm() == HmsScan.LOCATION_COORDINATE_FORM) {

                        resultType.setText("Location");

                    } else if (hmsScan.getScanTypeForm() == HmsScan.TEL_PHONE_NUMBER_FORM) {

                        resultType.setText("Tel");


                    } else if (hmsScan.getScanTypeForm() == HmsScan.SMS_FORM) {

                        resultType.setText("SMS");

                    } else if (hmsScan.getScanTypeForm() == HmsScan.WIFI_CONNECT_INFO_FORM) {

                        resultType.setText("Wi-Fi");


                    } else if (hmsScan.getScanTypeForm() == HmsScan.URL_FORM) {

                        resultType.setText("WebSite");


                        resultType.setText("WebSite");
                    } else {

                        resultType.setText("Text");

                    }
                } else if (hmsScan.getScanType() == HmsScan.EAN13_SCAN_TYPE) {
                    if (hmsScan.getScanTypeForm() == HmsScan.ISBN_NUMBER_FORM) {
                        resultType.setText("ISBN");
                    } else if (hmsScan.getScanTypeForm() == HmsScan.ARTICLE_NUMBER_FORM) {
                        resultType.setText("Product");
                    }

                } else if (hmsScan.getScanType() == HmsScan.EAN8_SCAN_TYPE || hmsScan.getScanType() == HmsScan.UPCCODE_A_SCAN_TYPE
                        || hmsScan.getScanType() == HmsScan.UPCCODE_E_SCAN_TYPE) {
                    if (hmsScan.getScanTypeForm() == HmsScan.ARTICLE_NUMBER_FORM) {
                        resultType.setText("Product");
                    }
                }
            }else {
                Log.e(TAG, "onActivityResult scanType: "+scanType );
                iUnsuccessful = iUnsuccessful+1;
                scan_result.setText("Success ："+iRunSuccessful
                        +"Error :"+iUnsuccessful
                );
                saveLocalFile(
                        getResources().getString(R.string.task_interval)+iIntervalSeconds+"S\n"+
                                getResources().getString(R.string.system_memory_cleanup)+btn_checkbox.isChecked()+"\n"+
                                getResources().getString(R.string.scanning_method)+typeScan+"\n"+
                                getResources().getString(R.string.memory_usage_rate)+getMemoryInfo()+"\n"
                                +getResources().getString(R.string.task_execution)+"Success :"+iRunSuccessful+

                                "Error :"+iUnsuccessful+
                                getResources().getString(R.string.needtoRunCount)+ iCountNumberNeedsRun );

                String strReadFile = FileHelper.readFromFile(this,"MonitoringInformation.txt");
                scan_result_show.setText(strReadFile);

                if(iRunSuccessful+iUnsuccessful < iCountNumberNeedsRun){

                    Log.e(TAG, "startScan getMemoryInfo: \n"+getMemoryInfo());
                    Log.e(TAG, "startScan: "+"Success :"+iRunSuccessful+
                            "Error :"+iUnsuccessful+
                            "需要执行任务次数："+ iCountNumberNeedsRun);

                    if (scanType == CUSTOM_BACK_FIXED_CAMERA_USE_HW ){
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                requestPermission(CUSTOM_BACK_FIXED_CAMERA_USE_HW, DECODE);
                            }
                        }, iIntervalSeconds);



                    }else if (scanType == CUSTOM_FRONT_CAMERA_USE_HW ){

                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                requestPermission(CUSTOM_FRONT_CAMERA_USE_HW, DECODE);
                            }
                        }, iIntervalSeconds);

                    }else if (scanType == CUSTOM_BACK_CAMERA_USE_HW){

                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                requestPermission(CUSTOM_BACK_CAMERA_USE_HW, DECODE);
                            }
                        }, iIntervalSeconds);


                    }else if (scanType == DEFINED_CODE){

                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                requestPermission(DEFINED_CODE, DECODE);
                            }
                        }, iIntervalSeconds);


                    }
                }else {
                    setButtonEnableTrue();
                }
                Log.e(TAG, "onActivityResult 华为扫码库扫码失败次数: "+iUnsuccessful );
            }
        }

 // ******************************** ********************************

        //Default View
        else if (requestCode == REQUEST_CODE_SCAN_ONE) {
            Log.e("szaf", "onActivityResult: 11111" );
            HmsScan obj = data.getParcelableExtra(ScanUtil.RESULT);
            if (obj != null) {
                Intent intent = new Intent(this, DisPlayActivity.class);
                intent.putExtra(RESULT, obj);
                startActivity(intent);
            }
            //MultiProcessor & Bitmap
        } else if (requestCode == REQUEST_CODE_SCAN_MULTI) {
            Log.e("szaf", "onActivityResult: 22222" );
            Parcelable[] obj = data.getParcelableArrayExtra(CommonActivity.SCAN_RESULT);
            if (obj != null && obj.length > 0) {
                //Get one result.
                if (obj.length == 1) {
                    if (obj[0] != null && !TextUtils.isEmpty(((HmsScan) obj[0]).getOriginalValue())) {
                        Intent intent = new Intent(this, DisPlayActivity.class);
                        intent.putExtra(RESULT, obj[0]);
                        startActivity(intent);
                    }
                } else {
                    Intent intent = new Intent(this, DisPlayMulActivity.class);
                    intent.putExtra(RESULT, obj);
                    startActivity(intent);
                }
            }
            //Customized View
        }  else if(requestCode == REQUEST_CODE_CUSTOM_BACK_CAMERA_BY_AX
                || requestCode == REQUEST_CODE_CUSTOM_FRONT_CAMERA_BY_AX){
            String obj = data.getStringExtra(Camera2Activity.SCAN_RESULT);
            long time = data.getLongExtra(Camera2Activity.SCAN_TIME,-1);
            long initCamerausedTime = data.getLongExtra(ZXingScannerActivity.INITCAMERTIME,-1);
            if (obj != null) {
                Log.e("szaf", "onActivityResult: --->>>" + obj);
                Log.i("szaf", "解析结果:" + obj);
                scan_framework.setText("ANXIN");
                rawResult.setText(obj);
                scan_time.setText(String.valueOf(time));
                opencamera_time.setText(String.valueOf(initCamerausedTime));
            }
        } else if(requestCode == REQUEST_CODE_CUSTOM_BACK_CAMERA_FIXED_BY_AX){
            String obj = data.getStringExtra(Camera2Activity.SCAN_RESULT);
            long time = data.getLongExtra(Camera2Activity.SCAN_TIME,-1);
            long initCamerausedTime = data.getLongExtra(ZXingScannerActivity.INITCAMERTIME,-1);
            if (obj != null) {
                Log.e("szaf", "onActivityResult: --->>>" + obj);
                Log.i("szaf", "解析结果:" + obj);
                scan_framework.setText("ANXIN & Camera Fixed");
                rawResult.setText(obj);
                scan_time.setText(String.valueOf(time));
                opencamera_time.setText(String.valueOf(initCamerausedTime));
            }
        } else if(requestCode == REQUEST_CODE_BY_GOOGLE_MLKIT){
            String obj = data.getStringExtra(QrScanActivity.SCAN_RESULT);
            long time = data.getLongExtra(QrScanActivity.SCAN_TIME,-1);
            long initCamerausedTime = data.getLongExtra(ZXingScannerActivity.INITCAMERTIME,-1);
            String strCodeFormat = data.getStringExtra(QrScanActivity.SCAN_FORMAT);
            if (obj != null) {
                Log.e("szaf", "onActivityResult: --->>>" + obj);
                Log.i("szaf", "解析结果:" + obj);
                scan_framework.setText("GOOGLE MLKIT");
                rawResult.setText(obj);
                scan_time.setText(String.valueOf(time));
                opencamera_time.setText(String.valueOf(initCamerausedTime));
                codeFormat.setText(strCodeFormat);
            }
        }
        if (btn_checkbox.isChecked()){
            System.gc();
            Log.e(TAG, "完成调用了系统清理内存方法" );
        }else {
            Log.e(TAG, "未调用了系统清理内存方法" );
        }
    }


    @Override
    protected void onPause() {
        Log.d("szaf", "onPause: ");
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        Log.e("szaf", "onDestroy: ");
        if(broadcastReceiver!=null){
            broadcastReceiver.endObserver();
            Log.i("test", "注销");
        }
        super.onDestroy();
        WatermarkUtils.removeWatermark(this);
    }

    @Override
    protected void onResume() {
        Log.e("szaf", "onResume: " );
        super.onResume();
    }

    @Override
    protected void onRestart() {
        Log.e("szaf", "onRestart: " );
        super.onRestart();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
 // Zxing
            case R.id.bt_scan:
                iCheckScanType = 0;
                iRunSuccessful = 0;
                iUnsuccessful=0;
                if (ed_conut.getText().toString().trim().isEmpty()) {
                    iCountNumberNeedsRun = 1;
                } else {
                    iCountNumberNeedsRun = Integer.parseInt(String.valueOf(ed_conut.getText().toString().trim()));
                }


                if (et_interval.getText().toString().trim().isEmpty()) {
                    iIntervalSeconds = 1000;
                } else {
                    iIntervalSeconds = 1000*Integer.parseInt(String.valueOf(et_interval.getText().toString().trim()));
                }

                setButtonEnableFalse();
                if (Build.VERSION.SDK_INT >= 25 /*Build.VERSION_CODES.N_MR1*/) {
                    if (ContextCompat.checkSelfPermission(QrCodeMainActivity.this,
                            Manifest.permission.CAMERA)
                            != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(QrCodeMainActivity.this,
                                new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST_CODE);
                    } else {
                        startScan();
                    }
                } else {
                    startScan();
                }
                break;
            case R.id.bt_generate:

                GenerateQRCodeActivity.startActivity(QrCodeMainActivity.this);
                break;
            case R.id.btn_hms:

                iCheckScanType = 1;
                iRunSuccessful = 0;
                iUnsuccessful=0;
                if (ed_conut.getText().toString().trim().isEmpty()) {
                    iCountNumberNeedsRun = 1;
                } else {
                    iCountNumberNeedsRun = Integer.parseInt(String.valueOf(ed_conut.getText().toString().trim()));
                }


                if (et_interval.getText().toString().trim().isEmpty()) {
                    iIntervalSeconds = 2000;
                } else {
                    iIntervalSeconds = 1000*Integer.parseInt(String.valueOf(et_interval.getText().toString().trim()));
                }
                setButtonEnableFalse();

                //                requestPermission(CAMERA_REQ_CODE, DECODE);
                requestPermission(DEFINED_CODE, DECODE);
                scanType = DEFINED_CODE;
//                Intent intent = new Intent(QrCodeMainActivity.this, HmsMainActivity.class);
//                startActivity(intent);
                break;
            case R.id.btn_scren:
                goToSleep(QrCodeMainActivity.this);
//                Thread screanThread = new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//                        for (int i = 0; i < 5; i++) {
//                            goToSleep(QrCodeMainActivity.this);
//                            try {
//                                Thread.sleep(1000*5);
//                                wakeUp(QrCodeMainActivity.this);
//                            } catch (InterruptedException e) {
//                                e.printStackTrace();
//                            }
//                            i = i+1;
//                        }
//                    }
//                });
//                screanThread.start();

                break;

            case R.id.btn_backcamera_hw:
                iCheckScanType = 2;
                iRunSuccessful = 0;
                iUnsuccessful=0;
                if (ed_conut.getText().toString().trim().isEmpty()) {
                    iCountNumberNeedsRun = 1;
                } else {
                    iCountNumberNeedsRun = Integer.parseInt(String.valueOf(ed_conut.getText().toString().trim()));
                }


                if (et_interval.getText().toString().trim().isEmpty()) {
                    iIntervalSeconds = 2000;
                } else {
                    iIntervalSeconds = 1000*Integer.parseInt(String.valueOf(et_interval.getText().toString().trim()));
                }
                setButtonEnableFalse();

                requestPermission(CUSTOM_BACK_CAMERA_USE_HW, DECODE);
                scanType = CUSTOM_BACK_CAMERA_USE_HW;
                break;

            case R.id.btn_frontcamera_hw:
                iCheckScanType = 3;
                iRunSuccessful = 0;
                iUnsuccessful=0;
                if (ed_conut.getText().toString().trim().isEmpty()) {
                    iCountNumberNeedsRun = 1;
                } else {
                    iCountNumberNeedsRun = Integer.parseInt(String.valueOf(ed_conut.getText().toString().trim()));
                }


                if (et_interval.getText().toString().trim().isEmpty()) {
                    iIntervalSeconds = 2000;
                } else {
                    iIntervalSeconds = 1000*Integer.parseInt(String.valueOf(et_interval.getText().toString().trim()));
                }
                setButtonEnableFalse();

                requestPermission(CUSTOM_FRONT_CAMERA_USE_HW, DECODE);
                scanType = CUSTOM_FRONT_CAMERA_USE_HW;
                break;
            case R.id.btn_backcamera_ax:
                requestPermission(CUSTOM_BACK_CAMERA_USE_AX, DECODE);
                break;
            case R.id.btn_frontcamera_ax:
                requestPermission(CUSTOM_FRONT_CAMERA_USE_AX, DECODE);
                break;

            case R.id.btn_custom_hms:
                iCheckScanType = 4;
                iRunSuccessful = 0;
                iUnsuccessful=0;
                if (ed_conut.getText().toString().trim().isEmpty()) {
                    iCountNumberNeedsRun = 1;
                } else {
                    iCountNumberNeedsRun = Integer.parseInt(String.valueOf(ed_conut.getText().toString().trim()));
                }


                if (et_interval.getText().toString().trim().isEmpty()) {
                    iIntervalSeconds = 2000;
                } else {
                    iIntervalSeconds = 1000*Integer.parseInt(String.valueOf(et_interval.getText().toString().trim()));
                }
                setButtonEnableFalse();

                requestPermission(CUSTOM_BACK_FIXED_CAMERA_USE_HW, DECODE);
                scanType = CUSTOM_BACK_FIXED_CAMERA_USE_HW;
                break;
            case R.id.btn_camera_ax:
                requestPermission(CUSTOM_BACK_FIXED_CAMERA_USE_AX, DECODE);
                break;
            case R.id.btn_scan_googlemlkit:
                requestPermission(SCAN_GOOGLE_MLKIT, DECODE);
                break;
            case R.id.btn_check:
                String strFile = FileHelper.readFromFile(this,"MonitoringInformation.txt");

                scan_result_show.setText(strFile);
                break;
        }
    }

    private void init() {
        tv=(TextView) findViewById(R.id.tv);
        tv.setText("开始测试 : ");
        broadcastReceiver=new MyBroadcastReceiver(this);
        broadcastReceiver.startObserver(new MyBroadcastReceiver.ScreenStateListener() {

            @Override
            public void onUserPresent() {
                String str=tv.getText()+"+";
                tv.setText(str+"解锁");
            }

            @Override
            public void onScreenOn() {
                String str=tv.getText()+"+";
                tv.setText(str+"开屏");
            }

            @Override
            public void onScreenOff() {
                String str=tv.getText()+"+";
                tv.setText(str+"锁屏");
            }
 // home
            @Override
            public void onHomePressed() {
                String str=tv.getText()+"+";
                tv.setText(str+"home键");

            }
        });


    }



    public static void goToSleep(Context context) {
        PowerManager powerManager= (PowerManager)context.getSystemService(Context.POWER_SERVICE);
        try {
            powerManager.getClass().getMethod("goToSleep", new Class[]{long.class}).invoke(powerManager, SystemClock.uptimeMillis());
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    /**
     * 唤醒屏幕
     *
     * @param context 上下文
     */
    public static void wakeUp(Context context) {
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        try {
            if (powerManager != null) {
                powerManager.getClass().getMethod("wakeUp", long.class).invoke(powerManager, SystemClock.uptimeMillis());
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {

        if (isClosedActivity) {
            LogUtils.d("onBackPressed");
            super.onBackPressed();
        } else {
            LogUtils.d("onBackPressed 任务未执行完毕，页面不能关闭");
            Toast.makeText(this,"任务未执行完毕，页面不能关闭",Toast.LENGTH_LONG).show();
            return;
        }
    }

    private  void setButtonEnableTrue(){
        isClosedActivity = true;

        ed_conut.setEnabled(true);
        btScan.setEnabled(true);
        btScan.setBackgroundResource(R.drawable.clors_btn);
        btGenerate.setEnabled(true);
        btGenerate.setBackgroundResource(R.drawable.clors_btn);
        btn_hms.setEnabled(true);
        btn_hms.setBackgroundResource(R.drawable.clors_btn);
        btn_CustomFixedFocusUseHW.setEnabled(true);
        btn_CustomFixedFocusUseHW.setBackgroundResource(R.drawable.clors_btn);
        btn_CustomBackScanUseHW.setEnabled(true);
        btn_CustomBackScanUseHW.setBackgroundResource(R.drawable.clors_btn);
        btn_CustomFrontScanUseHW.setEnabled(true);
        btn_CustomFrontScanUseHW.setBackgroundResource( R.drawable.clors_btn);
        btn_check.setEnabled(true);
        btn_check.setBackgroundResource( R.drawable.clors_btn);


    }
    private  void setButtonEnableFalse(){
        isClosedActivity = false;
        ed_conut.setEnabled(false);
        btScan.setEnabled(false);
        btScan.setBackgroundResource(R.drawable.clors_dis);
        btGenerate.setEnabled(false);
        btGenerate.setBackgroundResource(R.drawable.clors_dis);
        btn_hms.setEnabled(false);
        btn_hms.setBackgroundResource(R.drawable.clors_dis);
        btn_CustomFixedFocusUseHW.setEnabled(false);
        btn_CustomFixedFocusUseHW.setBackgroundResource(R.drawable.clors_dis);
        btn_CustomBackScanUseHW.setEnabled(false);
        btn_CustomBackScanUseHW.setBackgroundResource(R.drawable.clors_dis);
        btn_CustomFrontScanUseHW.setEnabled(false);
        btn_CustomFrontScanUseHW.setBackgroundResource( R.drawable.clors_dis);
        btn_check.setEnabled(false);
        btn_check.setBackgroundResource( R.drawable.clors_dis);

    }

    private String getMemoryInfo(){

        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        activityManager.getMemoryInfo(memoryInfo);

        float totalMemory = memoryInfo.totalMem;
        float availableMemory = memoryInfo.availMem;
        float memoryUsage = (totalMemory - availableMemory) / totalMemory * 100;

        String strMemoryTask =    memoryUsage + "%";
        Log.e(TAG, "内存使用率: "+strMemoryTask );
 // CPU
        double cpuUsage = getCpuUsage();
        String cpuTask ="CPU使用率: " + cpuUsage + "%";
 // Log.d(TAG, "CPU : " + cpuUsage + "%");
        return  strMemoryTask ;
    }

    private double getCpuUsage() {
        try {
            String[] cpuInfo = readCpuInfo();
            long idleTime = Long.parseLong(cpuInfo[3]);
            long totalTime = 0;
            for (String time : cpuInfo) {
                totalTime += Long.parseLong(time);
            }
            return (1 - (idleTime * 1.0 / totalTime)) * 100;
        } catch (Exception e) {
            e.printStackTrace();
            return 0.0;
        }
    }

    private String[] readCpuInfo() {
        StringBuilder stringBuilder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader("/proc/stat"))) {
            String line = reader.readLine();
            stringBuilder.append(line);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stringBuilder.toString().split(" ");
    }

    private void saveLocalFile(String MonitoringInformation){

        BufferedWriter bw = null;
        FileOutputStream fos = null;
        try {
            fos = this.openFileOutput("taskLinser", this.MODE_PRIVATE);
            bw = new BufferedWriter(new OutputStreamWriter(fos));
            bw.write(MonitoringInformation);
 // AID txt
            FileHelper.writeToFile(this, "MonitoringInformation.txt", MonitoringInformation);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String strFile = FileHelper.readFromFile(QrCodeMainActivity.this,"MonitoringInformation.txt");
                    scan_result_show.setText(strFile);
                }
            });
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bw != null) {
                try {
                    bw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
    }

}
