package com.szanfu.sdk.test.util;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Android 本地文件存储工具类
 * 支持同步和异步的文件读写操作
 */
public class FileStorageUtils {

    private static final String TAG = "FileStorageUtils";


    private static volatile FileStorageUtils instance;
    private final Context context;
    private final Executor diskIOExecutor;
    private final Handler mainHandler;

    private FileStorageUtils(Context context) {
        this.context = context.getApplicationContext();
        this.diskIOExecutor = Executors.newSingleThreadExecutor();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    public static FileStorageUtils getInstance(Context context) {
        if (instance == null) {
            synchronized (FileStorageUtils.class) {
                if (instance == null) {
                    instance = new FileStorageUtils(context);
                }
            }
        }
        return instance;
    }

    /**
     * 同步写入文件到应用内部存储（私有目录）
     * @param fileName 文件名
     * @param content 文件内容
     * @param mode 写入模式：Context.MODE_PRIVATE 或 Context.MODE_APPEND
     * @return 是否写入成功
     */
    public boolean writeFileSync(String fileName, String content, int mode) {
        FileOutputStream fos = null;
        try {
            fos = context.openFileOutput(fileName, mode);
            fos.write(content.getBytes(StandardCharsets.UTF_8));
            fos.flush();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 同步读取文件内容
     * @param fileName 文件名
     * @return 文件内容，如果读取失败返回null
     */
    public String readFileSync(String fileName) {
        FileInputStream fis = null;
        BufferedReader reader = null;
        try {
            fis = context.openFileInput(fileName);
            reader = new BufferedReader(new InputStreamReader(fis, StandardCharsets.UTF_8));

            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
            return stringBuilder.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                if (reader != null) reader.close();
                if (fis != null) fis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 异步写入文件（使用线程池）
     * @param fileName 文件名
     * @param content 文件内容
     * @param mode 写入模式
     * @param callback 写入回调
     */
    public void writeFileAsync(String fileName, String content, int mode, WriteCallback callback) {
        diskIOExecutor.execute(() -> {
            boolean success = writeFileSync(fileName, content, mode);
            if (callback != null) {
                mainHandler.post(() -> callback.onComplete(success));
            }
        });
    }

    /**
     * 异步读取文件（使用线程池）
     * @param fileName 文件名
     * @param callback 读取回调
     */
    public void readFileAsync(String fileName, ReadCallback callback) {
        diskIOExecutor.execute(() -> {
            String content = readFileSync(fileName);
            if (callback != null) {
                mainHandler.post(() -> callback.onComplete(content));
            }
        });
    }

    /**
     * 检查文件是否存在
     * @param fileName 文件名
     * @return 文件是否存在
     */
    public boolean fileExists(String fileName) {
        File file = new File(context.getFilesDir(), fileName);
        return file.exists();
    }

    /**
     * 删除文件
     * @param fileName 文件名
     * @return 是否删除成功
     */
    public boolean deleteFile(String fileName) {
        File file = new File(context.getFilesDir(), fileName);
        return file.delete();
    }

    /**
     * 写入文件到指定目录（可以是外部存储或自定义目录）
     * @param directory 目录路径
     * @param fileName 文件名
     * @param content 文件内容
     * @param append 是否追加写入
     * @return 是否写入成功
     */
    public boolean writeFileToDirectory(File directory, String fileName, String content, boolean append) {
        if (!directory.exists() && !directory.mkdirs()) {
            return false;
        }

        File file = new File(directory, fileName);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file, append);
            fos.write(content.getBytes(StandardCharsets.UTF_8));
            fos.flush();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 从指定目录读取文件
     * @param directory 目录
     * @param fileName 文件名
     * @return 文件内容
     */
    public String readFileFromDirectory(File directory, String fileName) {
        File file = new File(directory, fileName);
        if (!file.exists()) {
            return null;
        }

        FileInputStream fis = null;
        BufferedReader reader = null;
        try {
            fis = new FileInputStream(file);
            reader = new BufferedReader(new InputStreamReader(fis, StandardCharsets.UTF_8));

            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
            return stringBuilder.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                if (reader != null) reader.close();
                if (fis != null) fis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 写入回调接口
     */
    public interface WriteCallback {
        void onComplete(boolean success);
    }

    /**
     * 读取回调接口
     */
    public interface ReadCallback {
        void onComplete(String content);
    }

 // ==================== ====================

    /**
     * 默认覆盖写入（MODE_PRIVATE）
     */
    public boolean writeFile(String fileName, String content) {
        return writeFileSync(fileName, content, Context.MODE_PRIVATE);
    }

    /**
     * 追加写入文件
     */
    public boolean appendFile(String fileName, String content) {
        return writeFileSync(fileName, content, Context.MODE_APPEND);
    }

    /**
     * 默认异步覆盖写入
     */
    public void writeFileAsync(String fileName, String content, WriteCallback callback) {
        writeFileAsync(fileName, content, Context.MODE_PRIVATE, callback);
    }

    /**
     * 获取内部存储文件路径
     */
    public File getInternalFile(String fileName) {
        return new File(context.getFilesDir(), fileName);
    }

    /**
     * 获取缓存目录文件路径
     */
    public File getCacheFile(String fileName) {
        return new File(context.getCacheDir(), fileName);
    }
}