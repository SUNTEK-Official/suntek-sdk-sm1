package com.szanfu.sdk.test.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import androidx.core.app.NotificationCompat;
import com.szanfu.sdk.test.activity.VoiceRecognitionActivity;
import java.util.ArrayList;

public class VoiceCommandService extends Service {
    private static final String TAG = "VoiceCommandService";
    private SpeechRecognizer speechRecognizer;
    private boolean isListening = false;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "服务创建");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForeground(1001, createNotification());
        }
        initSpeechRecognizer();
    }

    private void initSpeechRecognizer() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "zh-CN");
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "zh-CN");
        intent.putExtra(RecognizerIntent.EXTRA_SUPPORTED_LANGUAGES, "zh-CN");
        intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);

        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {}
            @Override
            public void onBeginningOfSpeech() {
                isListening = true;
            }
            @Override
            public void onRmsChanged(float rmsdB) {}
            @Override
            public void onBufferReceived(byte[] buffer) {}
            @Override
            public void onEndOfSpeech() {
                isListening = false;

                stopCurrentListen();
            }

            @Override
            public void onError(int error) {
                isListening = false;
                stopCurrentListen();
                Log.e(TAG, "识别错误：" + error);
            }

            @Override
            public void onResults(Bundle results) {
                ArrayList<String> list = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (list != null && !list.isEmpty()) {
                    String text = list.get(0);
                    sendVoiceResult(text);
                }

                stopCurrentListen();
            }

            @Override
            public void onPartialResults(Bundle partialResults) {}
            @Override
            public void onEvent(int eventType, Bundle params) {}
        });
        speechRecognizer.startListening(intent);
        isListening = true;
    }


    private void stopCurrentListen() {
        if (speechRecognizer != null && isListening) {
            speechRecognizer.stopListening();
            isListening = false;
        }
    }

    private void sendVoiceResult(String text) {
        Intent intent = new Intent(VoiceRecognitionActivity.ACTION_VOICE_RESULT);
        intent.putExtra(VoiceRecognitionActivity.EXTRA_VOICE_TEXT, text);
        sendBroadcast(intent);
    }

    private Notification createNotification() {
        String channelId = "VOICE_SERVICE_CHANNEL";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "语音识别服务",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
        return new NotificationCompat.Builder(this, channelId)
                .setContentTitle("语音识别运行中")
                .setContentText("待命状态")
                .setSmallIcon(android.R.drawable.ic_btn_speak_now)
                .build();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (speechRecognizer != null) {
            speechRecognizer.stopListening();
            speechRecognizer.cancel();
            speechRecognizer.destroy();
            speechRecognizer = null;
        }
        isListening = false;
    }

    @Override
    public android.os.IBinder onBind(Intent intent) {
        return null;
    }
}