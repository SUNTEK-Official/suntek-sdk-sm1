package com.szanfu.sdk.test.util;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.szanfu.sdk.test.R;


/**
 * description:自定义dialog
 */

public class CommonSetWorkKeyDialog extends Dialog {
    /**
     * 显示的图片
     */
    private ImageView imageIv;

    /**
     * 显示的标题
     */
    private TextView dialogtitleTv;

    /**
     * 显示的消息
     */
    private TextView messageTv;

    /**
     * 确认和取消按钮
     */
    private Button negtiveBn, positiveBn;
    private EditText ed_sn_tusn;


    /**
     * 按钮之间的分割线
     */
    private View columnLineView;

    public CommonSetWorkKeyDialog(Context context) {
        super(context, R.style.CustomDialog);
    }

    /**
     * 都是内容数据
     */
    private String message;
    private String title;
    private String positive, negtive;
    private int imageResId = -1;

    /**
     * 底部是否只有一个按钮
     */
//    private boolean isSingle = false;
    private boolean isSingle = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_setworkkey_view);

        setCanceledOnTouchOutside(false);

        initView();

        refreshView();

        initEvent();
    }

    /**
     * 初始化界面的确定和取消监听器
     */
    private void initEvent() {

        positiveBn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onClickBottomListener != null) {
                    String setTusn = ed_sn_tusn.getText().toString().trim();
                    if (setTusn.isEmpty() || "" == setTusn) {
                        ToastUtils.showToast(getContext(), "Please enter the Work Key to be set！", 1200);
//                        Toast.makeText(getContext(), "Please enter the Work Key to be set！",
//                                Toast.LENGTH_SHORT).show();
                    } else {
                        onClickBottomListener.onPositiveClick(setTusn);
                    }
                }
            }
        });

        negtiveBn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onClickBottomListener != null) {
                    onClickBottomListener.onNegtiveClick();
                }
            }
        });
    }

    /**
     * 初始化界面控件的显示数据
     */
    private void refreshView() {
 // title message
//        if (!TextUtils.isEmpty(title)) {
//            dialogtitleTv.setText("Please enter the SN to be set\n");
//            dialogtitleTv.setVisibility(View.VISIBLE);
//        }else {
//            dialogtitleTv.setVisibility(View.VISIBLE);
//        }
        if (!TextUtils.isEmpty(message)) {
            messageTv.setText(message);
        }

        if (!TextUtils.isEmpty(positive)) {
            positiveBn.setText(positive);
        } else {
            positiveBn.setText("Enable");
        }
        if (!TextUtils.isEmpty(negtive)) {
            negtiveBn.setText(negtive);
        } else {
            negtiveBn.setText("取消");
        }

        if (imageResId != -1) {
            imageIv.setImageResource(imageResId);
            imageIv.setVisibility(View.GONE);
        } else {
            imageIv.setVisibility(View.GONE);
        }
        /**
         * 只显示一个按钮的时候隐藏取消按钮，回掉只执行确定的事件
         */
        if (isSingle) {
            columnLineView.setVisibility(View.GONE);
            negtiveBn.setVisibility(View.GONE);
        } else {
            negtiveBn.setVisibility(View.VISIBLE);
            columnLineView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void show() {
        super.show();
        refreshView();
    }

    /**
     * 初始化界面控件
     */
    private void initView() {
        negtiveBn = (Button) findViewById(R.id.negtive);
        positiveBn = (Button) findViewById(R.id.positive);
        dialogtitleTv = (TextView) findViewById(R.id.dialog_title);
        messageTv = (TextView) findViewById(R.id.message);
        imageIv = (ImageView) findViewById(R.id.image);
        columnLineView = findViewById(R.id.column_line);
        ed_sn_tusn = (EditText) findViewById(R.id.ed_sn_tusn);
    }

    /**
     * 设置确定取消按钮的回调
     */
    public OnClickBottomListener onClickBottomListener;

    public CommonSetWorkKeyDialog setOnClickBottomListener(OnClickBottomListener onClickBottomListener) {
        this.onClickBottomListener = onClickBottomListener;
        return this;
    }

    public interface OnClickBottomListener {
        /**
         * 点击确定按钮事件
         */
        public void onPositiveClick(String strsn);

        /**
         * 点击取消按钮事件
         */
        public void onNegtiveClick();
    }

    public String getMessage() {
        return message;
    }

    public CommonSetWorkKeyDialog setMessage(String message) {
        this.message = message;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public CommonSetWorkKeyDialog setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getPositive() {
        return positive;
    }

    public CommonSetWorkKeyDialog setPositive(String positive) {
        this.positive = positive;
        return this;
    }

    public String getNegtive() {
        return negtive;
    }

    public CommonSetWorkKeyDialog setNegtive(String negtive) {
        this.negtive = negtive;
        return this;
    }

    public int getImageResId() {
        return imageResId;
    }

    public boolean isSingle() {
        return isSingle;
    }

    public CommonSetWorkKeyDialog setSingle(boolean single) {
        isSingle = single;
        return this;
    }

    public CommonSetWorkKeyDialog setImageResId(int imageResId) {
        this.imageResId = imageResId;
        return this;
    }

}
