package com.szanfu.sdk.test.activity.sign;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import com.szanfu.sdk.test.R;
import com.szanfu.sdk.test.util.SignatureValidator;
import com.szanfu.sdk.test.util.ToastUtils;
import com.szanfu.sdk.test.view.HandwritingBoardView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class SignatureActivity extends AppCompatActivity {

    private HandwritingBoardView handwritingBoardView;
    public static final long MIN_SIGNATURE_LIMIT = 20000;
    public static final long MAX_SIGNATURE_LIMIT = 400000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signature);
//        LinearLayout linearLayout = findViewById(R.id.ll_sign_panel);
//        handwritingBoardView = new HandwritingBoardView(this);
//        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
//        handwritingBoardView.setLayoutParams(params);
//        linearLayout.addView(handwritingBoardView);
        handwritingBoardView = findViewById(R.id.ll_sign_panel);
//        handwritingBoardView.setPenSize(20);
//        handwritingBoardView.setPanelColor(Color.GREEN);
//        handwritingBoardView.setPenColor(Color.BLUE);

    }


    public void onConfirmClick(View view) {
        if (handwritingBoardView.getBitmap() != null) {
            Bitmap bitmap = handwritingBoardView.getBitmap();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
 // 100
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
            byte[] byteArray = baos.toByteArray();
           /* long countBits = countBits(byteArray);
            if (countBits <= MIN_SIGNATURE_LIMIT) {
                ToastUtils.showToast(SignatureActivity.this,getString(R.string.signature_too_simple),Toast.LENGTH_LONG);
                handwritingBoardView.redo();
            } else if (countBits >= MAX_SIGNATURE_LIMIT) {
                ToastUtils.showToast(SignatureActivity.this,getString(R.string.signature_too_complex),Toast.LENGTH_LONG);
                handwritingBoardView.redo();
            }else{
                Intent intent = new Intent();
                intent.putExtra("bitmap",byteArray);
                setResult(RESULT_OK,intent);
                finish();
            }*/


            SignatureValidator.ValidationResult result = SignatureValidator.validateSignature(bitmap, SignatureActivity.this);

            if (!result.isValid) {
                Toast.makeText(this, result.message, Toast.LENGTH_LONG).show();
                handwritingBoardView.redo();
            } else {
                //Toast.makeText(this, result.message, Toast.LENGTH_LONG).show();
                Intent intent = new Intent();
                intent.putExtra("bitmap", byteArray);
                setResult(RESULT_OK, intent);
                finish();
            }

        } else {
            ToastUtils.showToast(SignatureActivity.this, getString(R.string.signature_cannot_be_empty), Toast.LENGTH_LONG);
        }
    }


    public void onCancelClick(View view) {
        setResult(RESULT_CANCELED);
        finish();
    }


    public void undo(View view) {
        handwritingBoardView.undo();
    }


    public void redo(View view) {
        handwritingBoardView.redo();
    }

    public void SaveImage() {
        /* hardware note */
        String fileUrl = Environment.getExternalStorageDirectory()
                .toString() + "/android/data/test.png";
        try {
            if (handwritingBoardView.getBitmap() == null) return;
            Bitmap mBitmap = handwritingBoardView.getBitmap();
            FileOutputStream fos = new FileOutputStream(new File(fileUrl));
            mBitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 统计byte数组中1的个数
     *
     * @param bytes
     * @return
     */
    private long countBits(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return 0;
        }

        long count = 0;
        for (int i = 0; i < bytes.length; i++) {
            for (int j = 0; j < 8; j++) {
                if ((bytes[i] & (1 << j)) != 0) {
                    count++;
                }
            }
        }

        return count;
    }


}