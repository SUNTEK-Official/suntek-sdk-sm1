package com.szanfu.sdk.test.activity;



        import android.os.Bundle;
        import android.os.Handler;
        import android.os.Looper;
        import android.view.View;
        import android.widget.Button;
        import android.widget.TextView;
        import android.widget.Toast;
        import androidx.appcompat.app.AppCompatActivity;

        import com.szanfu.sdk.test.R;

        import java.util.Random;
        import java.util.concurrent.atomic.AtomicBoolean;

public class ThreadActivity extends AppCompatActivity {


    private TextView tvStatus;
    private TextView tvResult;
    private TextView tvCounter;
    private Button btnStart;
    private Button btnStop;


    private volatile boolean isThreadRunning = false;
    private Thread subThread;
    private final Object lock = new Object();


    private final AtomicBoolean shouldStop = new AtomicBoolean(false);

 // Handler
    private final Handler mainHandler = new Handler(Looper.getMainLooper());


    private int attemptCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thread);


        initViews();


        btnStart.setOnClickListener(v -> startReadingThread());
        btnStop.setOnClickListener(v -> stopReadingThread());
    }

    private void initViews() {
        tvStatus = findViewById(R.id.tv_status);
        tvResult = findViewById(R.id.tv_result);
        tvCounter = findViewById(R.id.tv_counter);
        btnStart = findViewById(R.id.btn_start);
        btnStop = findViewById(R.id.btn_stop);
    }

    /**
     * 启动读取线程（线程安全，确保不会二次开启）
     */
    private void startReadingThread() {
        synchronized (lock) {
            if (isThreadRunning) {
                showToast("线程已在运行中，请勿重复开启！");
                return;
            }


            resetUI();
            shouldStop.set(false);
            isThreadRunning = true;
            attemptCount = 0;


            updateUIStatus("线程启动中...", "等待读取结果", "0/20");
            setButtonStates(false, true);


            subThread = new Thread(new ReadingRunnable());
            subThread.start();

            showToast("读取线程已启动");
        }
    }

    /**
     * 停止读取线程
     */
    private void stopReadingThread() {
        synchronized (lock) {
            if (!isThreadRunning) {
                showToast("没有运行中的线程");
                return;
            }

            shouldStop.set(true);


            new Thread(() -> {
                try {
                    if (subThread != null && subThread.isAlive()) {
                        subThread.join(1000);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {

                    mainHandler.post(() -> {
                        isThreadRunning = false;
                        setButtonStates(true, false);
                        updateUIStatus("线程已停止", "用户手动停止", "已停止");
                        showToast("线程已停止");
                    });
                }
            }).start();
        }
    }

    /**
     * 重置UI状态
     */
    private void resetUI() {
        tvStatus.setText("等待开始...");
        tvResult.setText("结果: 未开始");
        tvCounter.setText("尝试次数: 0/20");
    }

    /**
     * 更新UI状态
     */
    private void updateUIStatus(String status, String result, String counter) {
        runOnUiThread(() -> {
            tvStatus.setText(status);
            tvResult.setText("结果: " + result);
            tvCounter.setText("尝试次数: " + counter);
        });
    }

    /**
     * 设置按钮状态
     */
    private void setButtonStates(boolean startEnabled, boolean stopEnabled) {
        runOnUiThread(() -> {
            btnStart.setEnabled(startEnabled);
            btnStop.setEnabled(stopEnabled);
        });
    }

    /**
     * 显示Toast消息
     */
    private void showToast(final String message) {
        runOnUiThread(() -> Toast.makeText(
                ThreadActivity.this,
                message,
                Toast.LENGTH_SHORT
        ).show());
    }

    /**
     * 模拟的read方法
     * 在实际应用中，这里可能是读取网络、文件、蓝牙设备等操作
     */
    private int read() {
 // 1, 0
        Random random = new Random();

 // 70% -1( ), 20% 0( ), 10% ( )
        int probability = random.nextInt(100);

        if (probability < 70) {
            return -1;
        } else if (probability < 90) {
            return 0;
        } else {
            return random.nextInt(100) + 1; // 1-100
        }
    }

    /**
     * 读取任务的Runnable实现
     */
    private class ReadingRunnable implements Runnable {
        @Override
        public void run() {

            mainHandler.post(() -> {
                tvStatus.setText("线程运行中...");
                tvResult.setText("结果: 读取中...");
            });

            final int MAX_ATTEMPTS = 20;
            boolean success = false;
            int lastReadValue = -1;

            for (attemptCount = 1; attemptCount <= MAX_ATTEMPTS; attemptCount++) {

                if (shouldStop.get()) {
                    updateUIStatus("线程被中断", "用户中断", "中断于 " + (attemptCount - 1) + "/20");
                    break;
                }


                final int currentCount = attemptCount;
                mainHandler.post(() ->
                        tvCounter.setText("尝试次数: " + currentCount + "/20")
                );


                int ret = read();
                lastReadValue = ret;


                System.out.println("第 " + attemptCount + " 次读取: " + ret);


                if (ret > 0) {
                    success = true;


                    final int finalValue = ret;
                    mainHandler.post(() -> {
                        tvStatus.setText("读取成功！");
                        tvResult.setText("结果: 成功读取到值 " + finalValue);
                        showToast("读取成功！值: " + finalValue);
                    });
                    break;
                }


                try {
                    Thread.sleep(500); // 500
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }


            synchronized (lock) {
                isThreadRunning = false;
            }
        }
    }

    /**
     * Activity销毁时确保线程停止
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopReadingThread();

 // Handler
        mainHandler.removeCallbacksAndMessages(null);
    }
}