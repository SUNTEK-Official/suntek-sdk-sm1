package com.szanfu.sdk.test.util;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.szanfu.sdk.test.R;

import java.lang.ref.WeakReference;

public class ToastUtils {
    private static WeakReference<LinearLayout> toastLayoutRef;

    @SuppressLint({"RtlHardcoded", "UseCompatLoadingForDrawables"})
    public static void showToast(Context context, String content, int duration) {
        if (toastLayoutRef == null) {
            WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            LinearLayout linearLayout = new LinearLayout(context);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            linearLayout.setLayoutParams(lp);
            linearLayout.setOrientation(LinearLayout.VERTICAL);
            WindowManager.LayoutParams winLp = new WindowManager.LayoutParams();
            winLp.gravity = Gravity.RIGHT | Gravity.CENTER_VERTICAL;
            winLp.type = WindowManager.LayoutParams.TYPE_APPLICATION;
            winLp.width = WindowManager.LayoutParams.MATCH_PARENT;
            winLp.height = WindowManager.LayoutParams.MATCH_PARENT;
            winLp.flags = WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
            winLp.format = PixelFormat.RGBA_8888;
            windowManager.addView(linearLayout, winLp);
            toastLayoutRef = new WeakReference<>(linearLayout);
            showToast(context, content, duration);
            return;
        }
        LinearLayout toastItem = new LinearLayout(context);
        toastItem.setHorizontalGravity(Gravity.CENTER_HORIZONTAL);
        LinearLayout.LayoutParams toastLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 56);
        toastLp.setMargins(0,0,0, 16);
        toastItem.setLayoutParams(toastLp);
        TextView toastContent = new TextView(context);
        toastContent.setText(content);
        toastContent.setPadding(10, 10, 10, 10);
        toastContent.setMinWidth(200);
        toastContent.setHeight(56);
        toastContent.setGravity(Gravity.CENTER);
        toastContent.setBackground(context.getResources().getDrawable(R.drawable.toast_bg));
        toastContent.setTextColor(Color.parseColor("#FFFFFF"));
        toastItem.addView(toastContent);
        toastItem.animate()
                .setStartDelay(duration)
                .setDuration(1200)
                .alpha(0)
                .translationY(0)
                .setUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    private float oldHeight = 0;
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        float currentProgress = Float.parseFloat(animation.getAnimatedValue().toString());
                        if (oldHeight == 0){
                            oldHeight = toastItem.getMeasuredHeight();
                        }
                        LinearLayout.LayoutParams toastLP = (LinearLayout.LayoutParams) toastItem.getLayoutParams();
                        int newMarginBottom = (int) (-1 * currentProgress * oldHeight);
                        toastLP.setMargins(0,0,0,newMarginBottom);
                        toastItem.post(new Runnable() {
                            @Override
                            public void run() {
                                toastItem.setLayoutParams(toastLP);
                            }
                        });
                    }
                })
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        toastLayoutRef.get().removeView(toastItem);
                    }
                })
                .start();
        toastLayoutRef.get()
                .addView(toastItem);
    }
}
