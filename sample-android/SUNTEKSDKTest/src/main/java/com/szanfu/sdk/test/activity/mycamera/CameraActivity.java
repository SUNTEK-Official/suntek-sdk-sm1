package com.szanfu.sdk.test.activity.mycamera;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.szanfu.sdk.test.R;

public class CameraActivity extends AppCompatActivity {

    private CameraHelper cameraHelper;
    private AutoFitTextureView autoFitTextureView;
    private Switch switchManualFocus;
    private SeekBar seekBarFocus;
    private TextView textFocusValue;
    private Button btnSaveFocus;

    private static final int REQUEST_CAMERA_PERMISSION = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        autoFitTextureView = findViewById(R.id.texture);
        switchManualFocus = findViewById(R.id.switch_manual_focus);
        seekBarFocus = findViewById(R.id.seekbar_focus);
        textFocusValue = findViewById(R.id.text_focus_value);
        btnSaveFocus = findViewById(R.id.btn_save_focus);

        cameraHelper = new CameraHelper(this, autoFitTextureView);

        setupUI();
        checkPermissions();
    }

    private void setupUI() {
        switchManualFocus.setOnCheckedChangeListener((buttonView, isChecked) -> {
            cameraHelper.updateFocusMode(isChecked);
            seekBarFocus.setEnabled(isChecked);
            updateFocusText(isChecked, seekBarFocus.getProgress());
        });

        seekBarFocus.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (switchManualFocus.isChecked()) {
                    float distance = calculateFocusDistance(progress);
                    cameraHelper.setFocusDistance(distance);
                    updateFocusText(true, progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        btnSaveFocus.setOnClickListener(v -> saveConfiguration());
    }

    private float calculateFocusDistance(int progress) {
        // Linear mapping for simplicity
        float minFocus = cameraHelper.getMinFocusDistance();
        return (progress / 100f) * minFocus;
    }

    private void updateFocusText(boolean isManual, int progress) {
        if (isManual) {
            float distance = calculateFocusDistance(progress);
            textFocusValue.setText(String.format("Focus: %.2f", distance));
        } else {
            textFocusValue.setText("Focus: Auto");
        }
    }

    private void saveConfiguration() {
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean("is_manual", switchManualFocus.isChecked());
        editor.putInt("focus_progress", seekBarFocus.getProgress());
        editor.apply();
        Toast.makeText(this, "Configuration Saved", Toast.LENGTH_SHORT).show();
    }

    private void loadConfiguration() {
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        boolean isManual = sharedPref.getBoolean("is_manual", false);
        int progress = sharedPref.getInt("focus_progress", 0);

        // We set UI state, listener will trigger helper
        switchManualFocus.setChecked(isManual);
        seekBarFocus.setProgress(progress);
        
        if (isManual) {
            float distance = calculateFocusDistance(progress);
            cameraHelper.updateFocusMode(true);
            cameraHelper.setFocusDistance(distance);
        }
    }

    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.CAMERA},
                REQUEST_CAMERA_PERMISSION);
        } else {
            cameraHelper.openCamera();
             new Handler(Looper.getMainLooper()).postDelayed(this::loadConfiguration, 1000); 
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                cameraHelper.openCamera();
                 new Handler(Looper.getMainLooper()).postDelayed(this::loadConfiguration, 1000);
            } else {
                Toast.makeText(this, "Camera permission needed", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
             cameraHelper.openCamera();
             new Handler(Looper.getMainLooper()).postDelayed(this::loadConfiguration, 1000);
        }
    }

    @Override
    protected void onPause() {
        cameraHelper.closeCamera();
        super.onPause();
    }
}
