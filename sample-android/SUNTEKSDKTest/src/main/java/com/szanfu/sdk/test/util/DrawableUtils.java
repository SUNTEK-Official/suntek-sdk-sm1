package com.szanfu.sdk.test.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

public class DrawableUtils {

    /**
     * 从 drawable 资源获取 Bitmap 对象
     *
     * @param context 上下文对象
     * @param drawableId 资源ID (例如: R.drawable.peiqi)
     * @return 对应的 Bitmap 对象，解码失败返回 null
     */
    public static Bitmap getBitmapFromDrawable(@NonNull Context context, @DrawableRes int drawableId) {
 // 1: BitmapFactory ( )
        try {
            return BitmapFactory.decodeResource(context.getResources(), drawableId);
        } catch (Exception e) {
 // Drawable
            return convertDrawableToBitmap(context, drawableId);
        }
    }

    /**
     * 备选方案：通过 Drawable 对象转换为 Bitmap
     */
    private static Bitmap convertDrawableToBitmap(Context context, @DrawableRes int drawableId) {
        try {
            Drawable drawable = ContextCompat.getDrawable(context, drawableId);
            if (drawable == null) return null;

 // Bitmap
            Bitmap bitmap = Bitmap.createBitmap(
                    drawable.getIntrinsicWidth(),
                    drawable.getIntrinsicHeight(),
                    Bitmap.Config.ARGB_8888
            );

 // Drawable
            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());

 // Drawable Bitmap
            android.graphics.Canvas canvas = new android.graphics.Canvas(bitmap);
            drawable.draw(canvas);

            return bitmap;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}