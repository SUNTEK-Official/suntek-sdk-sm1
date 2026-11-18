package com.szanfu.sdk.test.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.MediaScannerConnection;
import android.os.Environment;
import android.os.RemoteException;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.Log;

import com.szanfu.sdk.test.entity.LcdInfo;
import com.szanfu.sdk.api.system.ISystemBinderService;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Locale;

public class LcdUtils {

    private static final String TAG = "LcdUtils";
    private static LcdUtils instance;
    private ISystemBinderService mSystemBinderService;

    /**
     * 私有构造方法，防止外部实例化
     */
    private LcdUtils() {
    }


    /**
     * 获取单例实例
     */
    public static LcdUtils getInstance() {
        if (instance == null) {
            synchronized (LcdUtils.class) {
                if (instance == null) {
                    instance = new LcdUtils();
                }
            }
        }
        return instance;
    }

    /**
     * 初始化 Service
     */
    public void init(ISystemBinderService service) {
        this.mSystemBinderService = service;
    }

    /**
     * 初始化 LCD 屏幕
     *
     * @return int 初始化结果，0 表示成功，其他值表示失败
     */
    public int lcdInit() {
        if (mSystemBinderService == null) {
            Log.e(TAG, "lcdInit: Service is null");
            return -1;
        }

        try {
            int result = mSystemBinderService.lcdInit();
            Log.e(TAG, "lcdInit Result: " + result);
            return result;
        } catch (RemoteException e) {
            e.printStackTrace();
            Log.e(TAG, "lcdInit Error: " + e.getMessage());
            return -2; // 2 RemoteException
        }
    }

    private LcdInfo cachedLcdInfo = null;

    /**
     * 获取 LCD 屏幕属性信息
     *
     * @return LcdInfo 对象，包含宽度、高度、类型及描述信息；若获取失败则返回 null
     */
    public LcdInfo lcdGetDevLcdStatus() {
        Log.e(TAG, "lcdGetDevLcdStatus: ");


        if (cachedLcdInfo != null) {
            Log.e(TAG, "Returning cached LcdInfo: " + cachedLcdInfo.toString());
            return cachedLcdInfo;
        }

        if (mSystemBinderService == null) return null;

        if (lcdInit() != 0) {
            return null;
        }

        try {
            int[] pixelWidthMax = new int[1];
            int[] pixelHeightMax = new int[1];
            int[] lcdType = new int[1];

            int result = mSystemBinderService.lcdGetDevLcdStatus(pixelWidthMax, pixelHeightMax, lcdType);
            Log.e(TAG, "lcdGetDevLcdStatus: result-->" + result);

            if (result == 0) {
                int width = pixelWidthMax[0];
                int height = pixelHeightMax[0];
                int type = lcdType[0];

                String typeDescription;
                switch (type) {
                    case 1:
                        ////width=128, height=32, type=1
                        typeDescription = "LCD_TYPE_BLACK_WRITE_ST7539";
                        break;
                    case 2:
                        //Width: 320, Height: 170, Type: 2
                        typeDescription = "LCD_TYPE_COLOR_SCREEN_JD9853";
                        break;
                    default:
                        typeDescription = "Unknown LCD Type";
                        break;
                }

                cachedLcdInfo = new LcdInfo(width, height, type, typeDescription);
                Log.e(TAG, "LcdInfo: " + cachedLcdInfo.toString());
                return cachedLcdInfo;
            } else {
                Log.e(TAG, "lcdGetDevLcdStatus Error: " + result);
                return null;
            }

        } catch (RemoteException e) {
            e.printStackTrace();
            Log.e(TAG, "lcdGetDevLcdStatus Error: " + e.getMessage());
        }

        return null;
    }


    /**
     * 显示字符串
     */
    public void lcdDisplayString(String text, int fontSize, int xPos, int yPos) {
        if (mSystemBinderService == null) return;
        try {
            int result = mSystemBinderService.lcdDisplayString(xPos, yPos, text, fontSize, 0xFFFF, 0x0000);
            if (result != 0) {
                Log.e(TAG, "lcdDisplayString Error: " + result);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
            Log.e(TAG, "lcdDisplayString Error: " + e.getMessage());
        }
    }

    /**
     * 清除屏幕
     */
    public void lcdClearScreen() {
        try {
            int fontColor = (lcdGetDevLcdStatus().getType() == 1) ? 0x0000 : 0xFFFF;
            int result = mSystemBinderService.lcdClearScreen(fontColor);
            Log.e(TAG, "lcdClearScreen Result: " + result);
        } catch (RemoteException e) {
            e.printStackTrace();
            Log.e(TAG, e.getMessage());
        }
    }

    /**
     * 显示交易金额，格式：$0000123.45
     */
  /*  public void displayTransactionAmount(double amount) {
        String formattedAmount = String.format("$%08.2f", amount);
        lcdClearScreen();
        lcdDisplayString(formattedAmount, 24, 5, 10);
    }*/
    public void displayTransactionAmount(double amount) {
        String formattedAmount = String.format("$%08.2f", amount);


        lcdClearScreen();


        LcdInfo lcdInfo = lcdGetDevLcdStatus();
        if (lcdInfo == null) return;

        int screenWidth = lcdInfo.getWidth();
        int screenHeight = lcdInfo.getHeight();

 // 3. ( )
        int fontSize = calculateOptimalFontSize(
                formattedAmount,
                screenWidth,
                screenHeight
        );


        int x = 0; // lcdDisplayString x
        int y = (screenHeight - fontSize) / 2;

        lcdDisplayString(formattedAmount, fontSize, x, y);
    }


    /**
     * 绘制图片
     */
    public void lcdDrawPicture(Bitmap bitmap) {
        if (mSystemBinderService == null) return;
        try {
            byte[] bmpData = PrintUtil.getBitmapData(bitmap);
            int result = mSystemBinderService.lcdDrawPicture(0, 0, bmpData, bmpData.length, bitmap.getWidth(), bitmap.getHeight());
            if (result != 0) {
                Log.e(TAG, "lcdDrawPicture Error: " + result);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
            Log.e(TAG, "lcdDrawPicture Error: " + e.getMessage());
        }
    }

    /**
     * 刷新屏幕
     */
    public void lcdRefreshScreen() {
        if (mSystemBinderService == null) return;
        try {
            int result = mSystemBinderService.lcdRefreshScreen();
            if (result != 0) {
                Log.e(TAG, "lcdRefreshScreen Error: " + result);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
            Log.e(TAG, "lcdRefreshScreen Error: " + e.getMessage());
        }
    }

    /**
     * 创建 Bitmap，用于显示文本
     */
    public Bitmap createTextBitmapX(String text, int width, int height, int textSize) {

//        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.WHITE);

        TextPaint textPaint = new TextPaint();
        textPaint.setColor(Color.BLACK);
//        textPaint.setTextSize(textSize);
        textPaint.setTextSize(calculateOptimalFontSize(text, width, height));
        textPaint.setAntiAlias(true);

        StaticLayout sl = new StaticLayout(text, textPaint, width, Layout.Alignment.ALIGN_CENTER, 1.0f, 0.0f, false);
        canvas.translate(0, (height - sl.getHeight()) / 2);
        sl.draw(canvas);

        return bitmap;
    }


    /**
     * 彩屏专用：带缓存的 Bitmap 显示
     * - 缓存目录：APP cache
     * - 文件存在直接显示
     * - 不存在才生成 BMP
     */
    public void drawBitmapForColorLcdWithCache(Context context, Bitmap bitmap) {
        if (context == null || bitmap == null) {
            Log.e(TAG, "drawBitmapForColorLcdWithCache: context or bitmap is null");
            return;
        }

        try {
 // 1) APP cache ( )
            File cacheDir = context.getCacheDir();
            File bmpFile = new File(cacheDir, "_lcd_color_text.bmp");

 // 2) ( )
            if (!bmpFile.exists()) {
                Log.i(TAG, "Color LCD cache not found, creating bmp: " + bmpFile.getAbsolutePath());
                saveBitmapAsBmpFile(bitmap, bmpFile.getAbsolutePath());
            } else {
                Log.i(TAG, "Color LCD cache hit: " + bmpFile.getAbsolutePath());
            }


            displayBmpOnLcd(bmpFile.getAbsolutePath());

        } catch (Exception e) {
            Log.e(TAG, "drawBitmapForColorLcdWithCache Error: " + e.getMessage());
        }
    }

    /**
     * 如果文本变了，主动清缓存
     * @param context
     */
    public void clearColorLcdCache(Context context) {
        File bmpFile = new File(context.getCacheDir(), "_lcd_color_text.bmp");
        if (bmpFile.exists()) bmpFile.delete();
    }



    /**
     * 彩屏专用：按 displayBmpOnLcd 的稳定链路显示 Bitmap
     * - 不缓存
     * - 使用固定临时文件（覆盖写）
     */
    public void drawBitmapForColorLcd(Context context, Bitmap bitmap) {
        if (bitmap == null || context == null) {
            Log.e(TAG, "drawBitmapForColorLcd: bitmap or context is null");
            return;
        }

        try {
 // 1) Context files ( )
            File tmpFile = new File(context.getFilesDir(), "_tmp_color_lcd.bmp");
            String tmpBmpPath = tmpFile.getAbsolutePath();

 // 2) BMP( )
            saveBitmapAsBmpFile(bitmap, tmpBmpPath);


            displayBmpOnLcd(tmpBmpPath);

        } catch (Exception e) {
            Log.e(TAG, "drawBitmapForColorLcd Error: " + e.getMessage());
        }
    }



    /**
     * 生成文本图片
     *
     * @param text            显示内容
     * @param controlTextSize 是否控制字体大小
     * @param textSize        显示文本大小
     * @return 文本图片
     */
    public Bitmap createTextBitmap(String text, boolean controlTextSize, int textSize) {
        LcdInfo lcdInfo = lcdGetDevLcdStatus();
        if (lcdInfo == null) {
            Log.i(TAG, "createTextBitmap: Error.....");
            return null;
        }
        Log.i(TAG, "lcdInfo: " + lcdInfo.toString());
        int width = lcdInfo.getWidth();
        int height = lcdInfo.getHeight();
        Bitmap bitmap = null;
        switch (lcdInfo.getType()) {
            case 1:
                bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                break;
            case 2:
                bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
                break;
        }
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.WHITE);

        TextPaint textPaint = new TextPaint();
        textPaint.setColor(Color.BLACK);

        if (controlTextSize) {
            textPaint.setTextSize(textSize);
        } else {
            textPaint.setTextSize(calculateOptimalFontSize(text, width, height));
        }
        textPaint.setAntiAlias(true);

        if (lcdInfo.getType() == 2) {
            textPaint.setFakeBoldText(true);

            Typeface typeface = Typeface.create("sans-serif-medium", Typeface.BOLD);
            textPaint.setTypeface(typeface);
 // ( : 2px, : 3px)
            textPaint.setShadowLayer(3f, 2f, 2f, Color.GRAY);
        }



        StaticLayout sl = new StaticLayout(text, textPaint, width, Layout.Alignment.ALIGN_CENTER, 1.0f, 0.0f, false);

        float translateY = (height - sl.getHeight()) / 2f;
        canvas.translate(0, translateY);
        sl.draw(canvas);

        return bitmap;
    }

    /**
     * 根据屏幕宽高和文本长度计算最佳字体大小（仅限 12, 16, 24, 48, 64）
     *
     * @param text         文本内容
     * @param screenWidth  屏幕宽度
     * @param screenHeight 屏幕高度
     * @return 最佳字体大小
     */
    public int calculateOptimalFontSize(String text, int screenWidth, int screenHeight) {
        int[] allowedFontSizes = {64, 48, 24, 16, 12};
//        int[] allowedFontSizes = {16, 24, 48, 64};


        for (int fontSize : allowedFontSizes) {
            int charWidth = fontSize / 2;
            int totalWidth = text.length() * charWidth;
            int totalHeight = fontSize;

            if (totalWidth <= screenWidth && totalHeight <= screenHeight) {
                Log.i(TAG, "Selected Font Size: " + fontSize);
                return fontSize;
            }
        }


        Log.e(TAG, "Text too large, fallback to minimum font size 12");
        return 12;
    }


    /**
     * 将 Bitmap 保存为 RGB565 格式的 BMP 文件
     *
     * @param bitmap   要保存的 Bitmap 对象 (RGB_565 格式)
     * @param filePath 保存路径
     */
    public void saveBitmapAsBmpFile(Bitmap bitmap, String filePath) {
        try {
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            int bytesPerPixel = 2; // RGB565 2
            int bmpDataSize = width * height * bytesPerPixel;
            int fileSize = 54 + bmpDataSize;

            FileOutputStream fos = new FileOutputStream(filePath);
            DataOutputStream dos = new DataOutputStream(fos);

 // BMP (14 bytes)
            dos.writeShort(0x4D42); // "BM"
            dos.writeInt(Integer.reverseBytes(fileSize));
            dos.writeShort(0);
            dos.writeShort(0);
            dos.writeInt(Integer.reverseBytes(54));

            // DIB Header (40 bytes)
            dos.writeInt(Integer.reverseBytes(40)); // DIB Header
            dos.writeInt(Integer.reverseBytes(width));
            dos.writeInt(Integer.reverseBytes(height)); // ( )
            dos.writeShort(Short.reverseBytes((short) 1)); // (1)
            dos.writeShort(Short.reverseBytes((short) 16)); // 16 (RGB565)
            dos.writeInt(0);
            dos.writeInt(Integer.reverseBytes(bmpDataSize));
            dos.writeInt(0); // ( / )
            dos.writeInt(0); // ( / )
            dos.writeInt(0);
            dos.writeInt(0);

 // (RGB565 , )
            ByteBuffer buffer = ByteBuffer.allocate(bmpDataSize);
            bitmap.copyPixelsToBuffer(buffer);
            byte[] pixelData = buffer.array();

            for (int y = height - 1; y >= 0; y--) {
                for (int x = 0; x < width; x++) {
                    int index = (y * width + x) * bytesPerPixel;
                    byte lowByte = pixelData[index];
                    byte highByte = pixelData[index + 1];
                    dos.writeByte(lowByte);
                    dos.writeByte(highByte);
                }
            }

            dos.close();
            fos.close();
            Log.i(TAG, "BMP file saved: " + filePath);

        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "Error saving BMP: " + e.getMessage());
        }
    }

    /**
     * 在彩屏设备上显示 BMP 文件
     *
     * @param filePath BMP 文件路径
     */
    public void displayBmpOnLcd(String filePath) {
        Log.i(TAG, "displayBmpOnLcd:filePath " + filePath);
        try {
            FileInputStream fis = new FileInputStream(filePath);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();

            byte[] buffer = new byte[1024];
            int bytesRead;

            while ((bytesRead = fis.read(buffer)) != -1) {
                bos.write(buffer, 0, bytesRead);
            }

            byte[] bmpData = bos.toByteArray();
            fis.close();
            bos.close();

 // BMP
            int width = readBmpInt(bmpData, 18);
            int rawHeight = readBmpInt(bmpData, 22);
            int height = Math.abs(rawHeight);
            int offset = readBmpInt(bmpData, 10);
            int bitsPerPixel = readBmpShort(bmpData, 28);
            int compression = readBmpInt(bmpData, 30);

            if (width <= 0 || height <= 0 || offset <= 0 || offset >= bmpData.length) {
                Log.e(TAG, "Invalid BMP header");
                return;
            }

            if (compression != 0) {
                Log.e(TAG, "Unsupported BMP compression: " + compression);
                return;
            }

            byte[] rgb565Data = convertBmpToRgb565(bmpData, offset, width, rawHeight, bitsPerPixel);
            if (rgb565Data == null) {
                Log.e(TAG, "Unsupported BMP bitsPerPixel: " + bitsPerPixel);
                return;
            }

 // API
            int result = mSystemBinderService.lcdDrawPicture(0, 0, rgb565Data, rgb565Data.length, width, height);
            Log.e(TAG, "Display BMP Result: " + result);

        } catch (IOException | RemoteException e) {
            e.printStackTrace();
            Log.e(TAG, "Error displaying BMP: " + e.getMessage());
        }
    }

    private int readBmpInt(byte[] data, int offset) {
        return (data[offset] & 0xFF)
                | ((data[offset + 1] & 0xFF) << 8)
                | ((data[offset + 2] & 0xFF) << 16)
                | ((data[offset + 3] & 0xFF) << 24);
    }

    private int readBmpShort(byte[] data, int offset) {
        return (data[offset] & 0xFF) | ((data[offset + 1] & 0xFF) << 8);
    }

    private byte[] convertBmpToRgb565(byte[] bmpData, int pixelOffset, int width, int rawHeight, int bitsPerPixel) {
        int height = Math.abs(rawHeight);
        boolean topDown = rawHeight < 0;
        int sourceRowSize = ((width * bitsPerPixel + 31) / 32) * 4;
        byte[] rgb565Data = new byte[width * height * 2];

        for (int y = 0; y < height; y++) {
            int sourceY = topDown ? y : height - y - 1;
            int sourceRowStart = pixelOffset + sourceY * sourceRowSize;
            int destRowStart = y * width * 2;

            for (int x = 0; x < width; x++) {
                int destIndex = destRowStart + x * 2;

                if (bitsPerPixel == 16) {
                    int sourceIndex = sourceRowStart + x * 2;
                    if (sourceIndex + 1 >= bmpData.length) {
                        return null;
                    }
                    rgb565Data[destIndex] = bmpData[sourceIndex];
                    rgb565Data[destIndex + 1] = bmpData[sourceIndex + 1];
                } else if (bitsPerPixel == 24) {
                    int sourceIndex = sourceRowStart + x * 3;
                    if (sourceIndex + 2 >= bmpData.length) {
                        return null;
                    }
                    writeRgb565(rgb565Data, destIndex, bmpData[sourceIndex + 2], bmpData[sourceIndex + 1], bmpData[sourceIndex]);
                } else if (bitsPerPixel == 32) {
                    int sourceIndex = sourceRowStart + x * 4;
                    if (sourceIndex + 2 >= bmpData.length) {
                        return null;
                    }
                    writeRgb565(rgb565Data, destIndex, bmpData[sourceIndex + 2], bmpData[sourceIndex + 1], bmpData[sourceIndex]);
                } else {
                    return null;
                }
            }
        }

        return rgb565Data;
    }

    private void writeRgb565(byte[] output, int offset, byte redByte, byte greenByte, byte blueByte) {
        int red = redByte & 0xFF;
        int green = greenByte & 0xFF;
        int blue = blueByte & 0xFF;
        int rgb565 = ((red & 0xF8) << 8)
                | ((green & 0xFC) << 3)
                | (blue >> 3);
        output[offset] = (byte) (rgb565 & 0xFF);
        output[offset + 1] = (byte) ((rgb565 >> 8) & 0xFF);
    }


    /**
     * 显示 BMP 图片，如果图片不存在则创建并保存
     *
     * @param context  上下文
     * @param fileName BMP 文件名（不包含路径，如 "transaction_in.bmp"）
     * @param text     显示文本
     * @param textSize 文本字体大小
     */
    public void displayBmp(Context context, String fileName, String text, int textSize) {
        String bmpPath = context.getFilesDir() + "/" + fileName;
        File bmpFile = new File(bmpPath);

        if (!bmpFile.exists()) {
            Log.i(TAG, "BMP file not found, creating new BMP file: " + bmpPath);
            Bitmap bitmapColor = createTextBitmap(text, true, textSize);
            saveBitmapAsBmpFile(bitmapColor, bmpPath);
        } else {
            Log.i(TAG, "BMP file already exists, using existing file: " + bmpPath);
        }
 // BMP
        displayBmpOnLcd(bmpPath);
    }


    /**
     * 输入金额跟随系统语言切换
     *
     * @param context  上下文
     * @param fileName BMP 文件名（不包含路径，如 "transaction_in.bmp"）
     * @param text     显示文本
     * @param textSize 文本字体大小
     */
    public void displayInputAmountsBmp(Context context, String fileName, String text, int textSize) {
 // 1) : =zh, = en( )
        String lang = resolveLang(context); // "zh" or "en"

 // 2) : xxx.bmp -> xxx_zh.bmp / xxx_en.bmp
        String localizedName = appendLangSuffix(fileName, lang);

 // 3) ( )
        File dir = new File(context.getFilesDir(), "lcd/" + lang);
        if (!dir.exists()) dir.mkdirs();

        String bmpPath = new File(dir, localizedName).getAbsolutePath();
        File bmpFile = new File(bmpPath);

        if (!bmpFile.exists()) {
            Log.i(TAG, "BMP not found, creating: " + bmpPath);
            Bitmap bitmapColor = createTextBitmap(text, true, textSize);
            //PrintUtil.saveBitmapToInternalStorage(context, bitmapColor);
            saveBitmapAsBmpFile(bitmapColor, bmpPath);
        } else {
            Log.i(TAG, "BMP exists, using: " + bmpPath);
        }

 // 4) BMP
        displayBmpOnLcd(bmpPath);
    }

    /**
     * 解析当前系统语言，中文→"zh"，其他→"en"（默认英文）
     */
    private String resolveLang(Context context) {
        Locale locale;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            locale = context.getResources().getConfiguration().getLocales().get(0);
        } else {
            locale = context.getResources().getConfiguration().locale;
        }
        if (locale != null && "zh".equalsIgnoreCase(locale.getLanguage())) {
            return "zh";
        }
        return "en";
    }

    /**
     * 给文件名加语言后缀：xxx.bmp → xxx_{lang}.bmp
     */
    private String appendLangSuffix(String fileName, String lang) {
        int dot = fileName.lastIndexOf('.');
        if (dot > 0) {
            String name = fileName.substring(0, dot);
            String ext = fileName.substring(dot); // ".bmp"
            return name + "_" + lang + ext;
        }
        return fileName + "_" + lang;
    }


    /**
     * 从 raw 目录中读取 BMP 文件
     *
     * @param context 上下文
     * @param resId   资源 ID（如 R.raw.color_screen）
     * @return BMP 文件的字节数组
     */
    public byte[] readBmpFromRaw(Context context, int resId) {
        Log.e(TAG, "readBmpFromRaw: ");
        try {
            InputStream inputStream = context.getResources().openRawResource(resId);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();

            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                bos.write(buffer, 0, bytesRead);
            }

            inputStream.close();
            return bos.toByteArray();

        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "Error reading BMP from raw: " + e.getMessage());
            return null;
        }
    }

    /**
     * 将 raw 资源文件复制到内部存储，并返回路径
     *
     * @param context  上下文
     * @param rawResId raw 资源 ID，例如 R.raw.color_screen
     * @param fileName 保存到内部存储的文件名，例如 "color_screen.bmp"
     * @return 文件路径
     */
    public String copyRawToFile(Context context, int rawResId, String fileName) {
        File file = new File(context.getFilesDir(), fileName);

        if (!file.exists()) {
            try (InputStream inputStream = context.getResources().openRawResource(rawResId);
                 FileOutputStream outputStream = new FileOutputStream(file)) {

                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }

                Log.i(TAG, "File copied to: " + file.getAbsolutePath());
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, "Error copying file: " + e.getMessage());
            }
        } else {
            Log.i(TAG, "File already exists: " + file.getAbsolutePath());
        }

        return file.getAbsolutePath();
    }


    /**
     * 读取 .bin 文件为字节数组
     *
     * @param filePath 文件路径
     * @return 字节数组
     */
    /*private byte[] readBinFile( String filePath) {
        try {

            Log.i(TAG, "File Path: " + filePath);
            Log.i(TAG, "File Exists: " + new File(filePath).exists());
            Log.i(TAG, "Can Read: " + new File(filePath).canRead());

            File file = new File(filePath);
            if (!file.exists()) {
                Log.e(TAG, "File not found: " + filePath);
                return null;
            }

            FileInputStream fis = new FileInputStream(file);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                bos.write(buffer, 0, bytesRead);
            }

            fis.close();
            return bos.toByteArray();

        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "Error reading bin file: " + e.getMessage());
            return null;
        }
    }*/
    private byte[] readBinFile(String filePath) {
        try {
            File file = new File(filePath);
            Log.i(TAG, "File Size: " + file.length());

            Log.i(TAG, "File Path: " + filePath);
            Log.i(TAG, "File Exists: " + new File(filePath).exists());
            Log.i(TAG, "Can Read: " + new File(filePath).canRead());

            if (!file.exists()) {
                Log.e(TAG, "File not found: " + filePath);
                return null;
            }

            FileInputStream fis = new FileInputStream(file);
            FileChannel channel = fis.getChannel();
            ByteBuffer buffer = ByteBuffer.allocate((int) file.length());
            channel.read(buffer);
            channel.close();
            fis.close();
            return buffer.array();

        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "Error reading bin file: " + e.getMessage());
            return null;
        }
    }


    /**
     * 在彩屏设备上显示 .bin 文件
     *
     * @param filePath .bin 文件路径
     * @param width    图片宽度
     * @param height   图片高度
     */
    public void displayBinOnLcd(String filePath, int width, int height) {
        Log.i(TAG, "displayBinOnLcd: filePath==>" + filePath);
        try {
            byte[] binData = readBinFile(filePath);
            if (binData == null) {
                Log.e(TAG, "Bin data is null");
                return;
            }


            int result = mSystemBinderService.lcdDrawPicture(0, 0, binData, binData.length, width, height);
            Log.e(TAG, "lcdDrawPicture Result: " + result);

        } catch (RemoteException e) {
            e.printStackTrace();
            Log.e(TAG, "RemoteException: " + e.getMessage());
        }
    }


    /**
     * 清屏（返回 result，便于 TEST 展示）
     */
    public int lcdClearScreenWithResult() {
        if (mSystemBinderService == null) return -1;
        try {
            int fontColor = (lcdGetDevLcdStatus().getType() == 1) ? 0x0000 : 0xFFFF;
            return mSystemBinderService.lcdClearScreen(fontColor);
        } catch (Exception e) {
            Log.e(TAG, "lcdClearScreenWithResult Error: " + e.getMessage());
            return -1;
        }
    }

    /**
     * 刷新屏幕（返回 result，便于 TEST 展示）
     */
    public int lcdRefreshScreenWithResult() {
        if (mSystemBinderService == null) return -1;
        try {
            return mSystemBinderService.lcdRefreshScreen();
        } catch (Exception e) {
            Log.e(TAG, "lcdRefreshScreenWithResult Error: " + e.getMessage());
            return -1;
        }
    }

    /**
     * 绘制图片（返回 result，便于 TEST 展示）
     */
    public int lcdDrawPictureWithResult(Bitmap bitmap) {
        if (mSystemBinderService == null) return -1;
        if (bitmap == null) return -2;
        try {
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            byte[] bmpData = PrintUtil.getBitmapData(bitmap);
            return mSystemBinderService.lcdDrawPicture(5, 5, bmpData, bmpData.length, width, height);
        } catch (Exception e) {
            Log.e(TAG, "lcdDrawPictureWithResult Error: " + e.getMessage());
            return -1;
        }
    }

    /**
     * 填充矩形区域
     */
    public int lcdFillScreen(int x1, int y1, int x2, int y2, int color) {
        if (mSystemBinderService == null) return -1;
        try {
            return mSystemBinderService.lcdFillScreen(x1, y1, x2, y2, color);
        } catch (Exception e) {
            Log.e(TAG, "lcdFillScreen Error: " + e.getMessage());
            return -1;
        }
    }

    /**
     * 绘制矩形边框
     */
    public int lcdDrawRectangle(int x1, int y1, int x2, int y2, int color) {
        if (mSystemBinderService == null) return -1;
        try {
            return mSystemBinderService.lcdDrawRectangle(x1, y1, x2, y2, color);
        } catch (Exception e) {
            Log.e(TAG, "lcdDrawRectangle Error: " + e.getMessage());
            return -1;
        }
    }

    /**
     * 绘制直线
     */
    public int lcdDrawLine(int x1, int y1, int x2, int y2, int color) {
        if (mSystemBinderService == null) return -1;
        try {
            return mSystemBinderService.lcdDrawLine(x1, y1, x2, y2, color);
        } catch (Exception e) {
            Log.e(TAG, "lcdDrawLine Error: " + e.getMessage());
            return -1;
        }
    }


    public String saveBitmapToPng(Context context, Bitmap bitmap, String fileName) {
        if (context == null || bitmap == null || TextUtils.isEmpty(fileName)) {
            return null;
        }

        File dir = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "lcd_debug");
        if (!dir.exists() && !dir.mkdirs()) {
            return null;
        }

        File file = new File(dir, fileName);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            boolean ok = bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            if (!ok) {
                return null;
            }

            MediaScannerConnection.scanFile(
                    context,
                    new String[]{file.getAbsolutePath()},
                    new String[]{"image/png"},
                    null
            );

            return file.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException ignore) {
                }
            }
        }
    }


}
