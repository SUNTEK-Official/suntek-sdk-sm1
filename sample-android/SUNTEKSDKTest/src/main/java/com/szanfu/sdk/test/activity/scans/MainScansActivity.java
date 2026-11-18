package com.szanfu.sdk.test.activity.scans;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Debug;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.imagealgorithmlab.barcode.DecodeEngine;
import com.imagealgorithmlab.barcode.SaveMode;
import com.imagealgorithmlab.barcode.SymbologyData;
import com.imagealgorithmlab.barcode.camera.DecoderLibrary;
import com.szanfu.sdk.test.R;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * 继承DecoderLibrary.DecoderLibraryCallBack接口,实现获取解码结果方法
 */
public class MainScansActivity extends Activity implements DecoderLibrary.DecoderLibraryCallBack {


    private DecoderLibrary mDecodeLibrary;
 // view
    private View cameraPreview;
 // view
    private LinearLayout captureView;

    private TextView tv_tapToScan;
    private TextView tv_barcode;
    private TextView tv_symbology;
    private ImageButton btnSwitchCamera;


    private Switch switchManualFocus;
    private SeekBar seekbarFocus;
    private TextView tvFocusDistance;

 // ( : cm), 30cm
    private int focusDistanceCm = 30;

    //1 represents normal mode;
    // 2 represents continue mode
    private int mScanMode = 2;

    static final String kScanPSettings = "ScanSSettings";
    static final String kScanModeKey = "ScanMode";

    MarshMallowPermission marshMallowPermission = new MarshMallowPermission(this);
    private boolean isBackCamera = true;

    /**
     * 声音相关 ==================start=======================
     */
    private AudioManager mAudioManager;
    private HashMap<Integer, Integer> mSoundPoolMap = new HashMap();
    private SoundPool mSoundPool = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);
    public static final int SUCCESS_SOUND_ID = 0;
    public static final int FAIL_SOUND_ID = 1;
    ///=============================end======================================


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_scans);

        initPlayVideo();
        initView();
        initListener();
        loadScanMode();

        /**
         * 传入激活码,激活
         * 参数一:上下文对象
         * 参数二:激活码
         */
        DecodeEngine.init(this, "bcc2b15ccde345e2af9497c37bfda9df", "/mnt/sdcard/decodeEngine", new DecodeEngine.RegisterCallback() {
            @Override
            public void registerStatus(boolean b) {
            }
        });


        /**
         * 获取解码库对象
         */
        try {
            mDecodeLibrary = DecoderLibrary.sharedObject(this, "camera2");
        } catch (Exception e) {
            e.printStackTrace();
        }

        mDecodeLibrary.setCallback(this);
 // view
        cameraPreview = mDecodeLibrary.getCameraPreview();
        if (cameraPreview != null) {
            ViewGroup parent = (ViewGroup) cameraPreview.getParent();
            if (parent != null) {
                parent.removeView(cameraPreview);
            }
        }
        captureView.addView(cameraPreview);

        mDecodeLibrary.setCameraType(DecoderLibrary.CameraType.BackFacing);
        isBackCamera = true;

        mDecodeLibrary.isNeedToAutomaticDetect(true);
        mDecodeLibrary.setPreviewResolution(DecoderLibrary.Resolution.Resolution_1920x1080);
        mDecodeLibrary.setDecodeTimeout(900);
 // mDecodeLibrary.setSaveMode(SaveMode.SAVEPREVIEWALLBMP); //
        mDecodeLibrary.isEnableLoopFocus(false);
        mDecodeLibrary.setFocusMode(DecoderLibrary.Focus.Focus_Fix_Far);
        mDecodeLibrary.setFocusDistanceInCm(focusDistanceCm);
//        Debug.debug("DL.Camera2", "LENS_FOCUS_DISTANCE = " + focusDistanceDiopters + " diopters (" + focusDistanceInches + " inches)");
        mDecodeLibrary.showLog(false);
        mDecodeLibrary.setDisplayOrientation(DecoderLibrary.Rotation.ROTATION_0);  //
        mDecodeLibrary.isNeedToAutomaticDetect(true);

        captureView.setKeepScreenOn(true);
        if (!marshMallowPermission.checkPermissionForCamera()) {
            marshMallowPermission.requestPermissionForCamera();
        } else {
            startScanning();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_main, menu);
        for (int i = 0; i < menu.size(); i++) {
            MenuItem item = menu.getItem(i);
            if (item.getItemId() == R.id.action_switchmode) {
                updateScanModeOptionMenuItem(item);
            }
        }
        return true;
    }

    private void updateScanModeOptionMenuItem(MenuItem item) {
        if (mScanMode == 1) {
            item.setTitle(R.string.action_scanmode_to_continue);
        } else {
            item.setTitle(R.string.action_scanmode_to_normal);

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MarshMallowPermission.CAMERA_PERMISSION_REQUEST_CODE:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startScanning();
                } else {

                }
                return;
        }
    }

    private void initListener() {
        tv_tapToScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.setVisibility(View.INVISIBLE);
                count = 0;
                startScanning();
                tv_barcode.setText("");
                tv_symbology.setText("");
            }
        });


        if (btnSwitchCamera != null) {
            btnSwitchCamera.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    toggleCamera();
                }
            });
        }


        switchManualFocus.setOnCheckedChangeListener((buttonView, isChecked) -> {

            mDecodeLibrary.isEnableLoopFocus(false);

            seekbarFocus.setEnabled(isChecked);

            if (!isChecked) {
                focusDistanceCm = 30;
                seekbarFocus.setProgress(focusDistanceCm);
                tvFocusDistance.setText(String.format("对焦距离：%dcm", focusDistanceCm));
                mDecodeLibrary.setFocusDistanceInCm(focusDistanceCm);
            }
            Toast.makeText(MainScansActivity.this,
                    isChecked ? "已开启手动对焦" : "已关闭手动对焦",
                    Toast.LENGTH_SHORT).show();
        });


        seekbarFocus.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
 // 1cm
                focusDistanceCm = Math.max(1, progress);

                tvFocusDistance.setText(String.format("对焦距离：%dcm", focusDistanceCm));

                mDecodeLibrary.setFocusDistanceInCm(focusDistanceCm);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
 // ( )
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

                Toast.makeText(MainScansActivity.this,
                        String.format("已设置对焦距离为%dcm", focusDistanceCm),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initView() {
        captureView = (LinearLayout) findViewById(R.id.capture_preview);
        tv_barcode = (TextView) findViewById(R.id.tv_barcode);
        tv_symbology = (TextView)findViewById(R.id.tv_symbology);
        tv_tapToScan = (TextView)findViewById(R.id.tv_tapToScan);
        btnSwitchCamera = (ImageButton) findViewById(R.id.btn_switch_camera);


        switchManualFocus = (Switch)findViewById(R.id.switch_manual_focus);
        seekbarFocus = (SeekBar)findViewById(R.id.seekbar_focus);
        tvFocusDistance = (TextView)findViewById(R.id.tv_focus_distance);

 // ( 8cm)
        seekbarFocus.setProgress(focusDistanceCm);
        tvFocusDistance.setText(String.format("对焦距离：%dcm", focusDistanceCm));
    }


    private void initPlayVideo() {
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        this.mSoundPoolMap.put(Integer.valueOf(SUCCESS_SOUND_ID),
                Integer.valueOf(this.mSoundPool.load(this, R.raw.success, 1)));
        this.mSoundPoolMap.put(Integer.valueOf(FAIL_SOUND_ID),
                Integer.valueOf(this.mSoundPool.load(this, R.raw.fail, 1)));
    }

    private void loadScanMode() {
        SharedPreferences sp = this.getSharedPreferences(kScanPSettings, Context.MODE_PRIVATE);
        int scanMode = sp.getInt(kScanModeKey, mScanMode);
        mScanMode = scanMode;

    }


    private int count = 0;

    @Override
    public void receivedDecodedData(final ArrayList<SymbologyData> arrayList) {
        /**
         * 解码结果
         */
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                playSound(SUCCESS_SOUND_ID, 1);

                StringBuffer sb = new StringBuffer();
                for (int i = 0; i < arrayList.size(); i++) {
                    sb.append("Barcode: " + arrayList.get(i).getData() + "\r\r" + "symbology: " + arrayList.get(i).getName() + "\r\n"
                            + "count: " + count);
                }
                count++;
                tv_barcode.setText(sb.toString());
                if (mScanMode == 1) {

                    stopScanning();
                    tv_tapToScan.setVisibility(View.VISIBLE);
                } else {

                    mDecodeLibrary.startDecoding();
                }
            }
        });
    }


    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {

        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_version) {

            boolean wrapInScrollView = true;
            MaterialDialog dialog = new MaterialDialog.Builder(this)
                    .title(R.string.versionTitle)
                    .customView(R.layout.dialog_version, wrapInScrollView)
                    .positiveText(R.string.versionPositive).build();

            VersionViewHodler vh = new VersionViewHodler(dialog.getCustomView());
            vh.setDecodeVersion(DecodeEngine.getDecodeLibVersion());
            vh.setVersionName(getVersionName());
            dialog.show();
            return true;
        } else if (id == R.id.action_switchmode) {
            if (mScanMode == 1) {
                mScanMode = 2;

                item.setTitle(getString(R.string.action_scanmode_to_normal));
 // scan, camera preview

            } else if (mScanMode == 2) {
                mScanMode = 1;

                item.setTitle(this.getString(R.string.action_scanmode_to_continue));
            }

            writeScanMode();
            updateScanModeOptionMenuItem(item);
            Toast.makeText(this, R.string.promptChangeScanMode, Toast.LENGTH_LONG).show();
        } else if (id == R.id.action_config) {

            startActivity(new Intent(this, SettingActivity.class));
        }
//
        return super.onMenuItemSelected(featureId, item);
    }

    /**
     * 开始扫描
     */
    void startScanning() {
        /**
         * 开启摄像头预览
         */
        mDecodeLibrary.startCameraPreview();
        /**
         * 开始解码
         */
        mDecodeLibrary.startDecoding();
    }

    /**
     * 停止扫描
     */
    void stopScanning() {
        /**
         * 停止解码
         */
        mDecodeLibrary.stopDecoding();
        /**
         * 停止摄像头预览
         */
        mDecodeLibrary.stopCameraPreview();
    }

    private void toggleCamera() {
        if (isBackCamera) {
            mDecodeLibrary.setCameraType(DecoderLibrary.CameraType.FrontFacing);
            isBackCamera = false;
        } else {
            mDecodeLibrary.setCameraType(DecoderLibrary.CameraType.BackFacing);
            isBackCamera = true;
        }
    }

    /**
     * 关闭相机
     */
    public void closeCamera() {

        mDecodeLibrary.stopDecoding();

        mDecodeLibrary.stopCameraPreview();

        mDecodeLibrary.closeCamera();
    }


    private String getVersionName() {
        try {
            String pkName = this.getPackageName();
            String versionName = this.getPackageManager().getPackageInfo(
                    pkName, 0).versionName;
            return versionName;
        } catch (Exception e) {
        }
        return null;
    }

    private void playSound(int audioType, int rate) {
        float vol = (mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC) * 1.0f)
                / mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        Integer soundId = mSoundPoolMap.get(Integer.valueOf(audioType));
        if ((this.mSoundPool != null) && (this.mSoundPoolMap != null)
                && (this.mSoundPoolMap.size() > 0)) {
            this.mSoundPool.play(soundId, vol, vol, 1, 0, rate);
        }
    }


    @Override
    protected void onResume() {
        super.onResume();

        tv_tapToScan.setVisibility(View.INVISIBLE);
        startScanning();

    }

    @Override
    protected void onPause() {
        super.onPause();

        stopScanning();

        closeCamera();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        mDecodeLibrary.closeSharedObject();

        if (mSoundPool != null) {
            mSoundPool.release();
        }
        if (mSoundPoolMap != null) {
            mSoundPoolMap.clear();
        }

    }

    private void writeScanMode() {
        SharedPreferences sp = this.getSharedPreferences(kScanPSettings, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt(kScanModeKey, mScanMode);
        editor.commit();
    }


    class VersionViewHodler {
        TextView mTxtDecodeVersion;
        TextView tv_versioname;

        public VersionViewHodler(View view) {

            mTxtDecodeVersion = (TextView) view.findViewById(R.id.txtDecodeVersion);
            tv_versioname = (TextView) view.findViewById(R.id.tv_versionname);
        }

        public void setDecodeVersion(String version) {
            mTxtDecodeVersion.setText(version);
        }

        public void setVersionName(String versionName) {
            tv_versioname.setText(versionName);
        }

    }

 // MarshMallowPermission ( )
    class MarshMallowPermission {
        private Activity activity;

        public static final int CAMERA_PERMISSION_REQUEST_CODE = 100;

        public MarshMallowPermission(Activity activity) {
            this.activity = activity;
        }

        public boolean checkPermissionForCamera() {
            int result = activity.checkSelfPermission(android.Manifest.permission.CAMERA);
            return result == PackageManager.PERMISSION_GRANTED;
        }

        public void requestPermissionForCamera() {
            if (activity.shouldShowRequestPermissionRationale(android.Manifest.permission.CAMERA)) {
                Toast.makeText(activity, "相机权限需要被授予才能使用扫码功能", Toast.LENGTH_LONG).show();
            } else {
                activity.requestPermissions(new String[]{android.Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
            }
        }
    }
}