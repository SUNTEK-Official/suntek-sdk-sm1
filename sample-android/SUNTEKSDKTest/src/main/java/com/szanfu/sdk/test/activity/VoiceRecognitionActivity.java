package com.szanfu.sdk.test.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import com.szanfu.sdk.test.R;
import com.szanfu.sdk.test.service.VoiceCommandService;

public class VoiceRecognitionActivity extends AppCompatActivity {
    private static final String TAG = "VoiceRecognition";
    private EditText etResult;
    private ImageButton btnMic;
    private static final int PERMISSION_RECORD_AUDIO = 100;
    public static final String ACTION_VOICE_RESULT = "ACTION_VOICE_RESULT";
    public static final String EXTRA_VOICE_TEXT = "EXTRA_VOICE_TEXT";
    private BroadcastReceiver voiceReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_recognition);
        etResult = findViewById(R.id.et_result);
        btnMic = findViewById(R.id.btn_mic);

        voiceReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (ACTION_VOICE_RESULT.equals(intent.getAction())) {
                    String text = intent.getStringExtra(EXTRA_VOICE_TEXT);
                    if (text != null && !text.isEmpty()) {
                        etResult.setText(text);
                    }

                    stopVoiceService();
                }
            }
        };
        registerReceiver(voiceReceiver, new IntentFilter(ACTION_VOICE_RESULT));

        btnMic.setOnClickListener(v -> {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.RECORD_AUDIO}, PERMISSION_RECORD_AUDIO);
                return;
            }
            startVoiceService();
        });
    }

    private void startVoiceService() {
        Intent serviceIntent = new Intent(this, VoiceCommandService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }
        Toast.makeText(this, "开始识别", Toast.LENGTH_SHORT).show();
    }

    private void stopVoiceService() {
        Intent serviceIntent = new Intent(this, VoiceCommandService.class);
        stopService(serviceIntent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(voiceReceiver);
        stopVoiceService();
    }
}