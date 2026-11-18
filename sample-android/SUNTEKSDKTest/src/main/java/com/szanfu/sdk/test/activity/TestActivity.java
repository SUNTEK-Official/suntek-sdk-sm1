package com.szanfu.sdk.test.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageView;

import com.szanfu.sdk.test.R;
import com.szanfu.sdk.test.util.TextToImageUtil;

public class TestActivity extends AppCompatActivity {

    private ImageView iv_print;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        iv_print = findViewById(R.id.iv_print);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int[] printMsgConfig = new int[5];
                printMsgConfig[0]=0; // 0: , 1: ; 1
                printMsgConfig[1]=0; // 0: 1
                printMsgConfig[2]=0; // 0: 1
                printMsgConfig[3]=1;
                printMsgConfig[4]=1;
                StringBuilder printMsgBuilder=new StringBuilder();
                printMsgBuilder.append("                          TEST\n");

                printMsgBuilder.append("\t\t\t" + "date_time" + "\n");

                printMsgBuilder.append("                          SALE");

                printMsgBuilder.append("MERCHANT NAME:" + "\n001420183990573\n");
                printMsgBuilder.append("TERMINAL NO:" + "\n00026715\n");
                printMsgBuilder.append("OPERATOR NO:" + "\n12345678\n");
                printMsgBuilder.append("ISSUER:" + "\n01020001\n");
                printMsgBuilder.append("CARD NO:" + "\n9558803602109503920\n");
                printMsgBuilder.append("ACQUIRER:" + "\n03050011\n");
                printMsgBuilder.append("TXN. TYPE:" + "\nSALE\n");
                printMsgBuilder.append("EXP. DATE:" + "\n2023/08\n");
                printMsgBuilder.append("REF NO:" + "\n" + "refId" + "\n");

                printMsgBuilder.append("AMOUNT:" + "RMB:2.55" + "\n");
                printMsgBuilder.append("CARDHOLDER SIGNATURE" + "\n\n");
                String strPrintFont = printMsgBuilder.toString();



                TextToImageUtil.TextConfig config = new TextToImageUtil.TextConfig();
                config.textSize = 14;
                config.textColor = Color.BLACK;
                config.backgroundColor = Color.WHITE;
                config.padding = 30;
                config.align = printMsgConfig[0];
                config.underline = printMsgConfig[1];
                config.underlineColor = Color.RED;
                config.underlineWidth = 1f;
                config.bold = printMsgConfig[2];
                config.orientation = 0;
                config.scaleX = printMsgConfig[3];
                config.scaleY = printMsgConfig[4];


                Bitmap imageFont = TextToImageUtil.createTextImage(
                        strPrintFont,
                        384,
                        config
                );


                iv_print.setImageBitmap(imageFont);


            }
        });



    }
}