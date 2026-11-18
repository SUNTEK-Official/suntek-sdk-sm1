package com.szanfu.sdk.test.activity.dialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.NumberPicker;

import androidx.annotation.NonNull;

import java.util.Calendar;

public class DateTimePickerDialog extends Dialog {

    public interface OnDateTimeSetListener {
        void onDateTimeSet(int year, int month, int day, int hour, int minute);
    }

    private final OnDateTimeSetListener listener;
    private final Calendar calendar;

    private NumberPicker yearPicker, monthPicker, dayPicker, hourPicker, minutePicker;

    public DateTimePickerDialog(@NonNull Context context, OnDateTimeSetListener listener) {
        super(context);
        this.listener = listener;
        this.calendar = Calendar.getInstance();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        LinearLayout mainLayout = new LinearLayout(getContext());
        mainLayout.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setPadding(32, 32, 32, 16);

 // ( )
        LinearLayout pickersLayout = new LinearLayout(getContext());
        pickersLayout.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        pickersLayout.setOrientation(LinearLayout.HORIZONTAL);


        yearPicker = createNumberPicker();
        monthPicker = createNumberPicker();
        dayPicker = createNumberPicker();
        hourPicker = createNumberPicker();
        minutePicker = createNumberPicker();


        setupYearPicker();
        setupMonthPicker();
        setupDayPicker();
        setupHourPicker();
        setupMinutePicker();


        pickersLayout.addView(yearPicker);
        pickersLayout.addView(monthPicker);
        pickersLayout.addView(dayPicker);
        pickersLayout.addView(hourPicker);
        pickersLayout.addView(minutePicker);


        LinearLayout buttonLayout = new LinearLayout(getContext());
        buttonLayout.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        buttonLayout.setOrientation(LinearLayout.HORIZONTAL);
        buttonLayout.setWeightSum(2);


        Button btnCancel = new Button(getContext());
        btnCancel.setText("Cancle");
        btnCancel.setLayoutParams(new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        btnCancel.setOnClickListener(v -> dismiss());

        Button btnConfirm = new Button(getContext());
        btnConfirm.setText("Commit");
        btnConfirm.setLayoutParams(new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        btnConfirm.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDateTimeSet(
                        yearPicker.getValue(),
                        monthPicker.getValue(),
                        dayPicker.getValue(),
                        hourPicker.getValue(),
                        minutePicker.getValue()
                );
            }
            dismiss();
        });


        buttonLayout.addView(btnCancel);
        buttonLayout.addView(btnConfirm);


        mainLayout.addView(pickersLayout);
        mainLayout.addView(buttonLayout);

        setContentView(mainLayout);
        setTitle("Please check time");
    }

    private NumberPicker createNumberPicker() {
        NumberPicker picker = new NumberPicker(getContext());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
        params.setMargins(4, 0, 4, 0);
        picker.setLayoutParams(params);
        return picker;
    }

    private void setupYearPicker() {
        int currentYear = calendar.get(Calendar.YEAR);
        yearPicker.setMinValue(currentYear - 10);
        yearPicker.setMaxValue(currentYear + 10);
        yearPicker.setValue(currentYear);
        yearPicker.setFormatter(value -> value + "Year");
        yearPicker.setOnValueChangedListener((picker, oldVal, newVal) -> updateDayPicker());
    }

    private void setupMonthPicker() {
        monthPicker.setMinValue(1);
        monthPicker.setMaxValue(12);
        monthPicker.setValue(calendar.get(Calendar.MONTH) + 1);
        monthPicker.setFormatter(value -> value + "Month");
        monthPicker.setOnValueChangedListener((picker, oldVal, newVal) -> updateDayPicker());
    }

    private void setupDayPicker() {
        updateDayPicker();
        dayPicker.setFormatter(value -> value + "Day");
    }

    private void setupHourPicker() {
        hourPicker.setMinValue(0);
        hourPicker.setMaxValue(23);
        hourPicker.setValue(calendar.get(Calendar.HOUR_OF_DAY));
        hourPicker.setFormatter(value -> value + "Hour");
    }

    private void setupMinutePicker() {
        minutePicker.setMinValue(0);
        minutePicker.setMaxValue(59);
        minutePicker.setValue(calendar.get(Calendar.MINUTE));
        minutePicker.setFormatter(value -> value + "Minute");
    }

    private void updateDayPicker() {
        int year = yearPicker.getValue();
        int month = monthPicker.getValue();
        Calendar temp = Calendar.getInstance();
        temp.set(year, month - 1, 1);
        int maxDay = temp.getActualMaximum(Calendar.DAY_OF_MONTH);


        int currentDay = Math.min(dayPicker.getValue(), maxDay);

        dayPicker.setMinValue(1);
        dayPicker.setMaxValue(maxDay);
        dayPicker.setValue(Math.max(1, currentDay));
    }


    /**
     * 调用示例
     *

 // Activity
     DateTimePickerDialog dialog = new DateTimePickerDialog(this,
     new DateTimePickerDialog.OnDateTimeSetListener() {
    @Override
    public void onDateTimeSet(int year, int month, int day, int hour, int minute) {

    String result = String.format(
    "选择的日期时间：%d年%d月%d日 %d时%d分",
    year, month, day, hour, minute
    );
    Toast.makeText(MainActivity.this, result, Toast.LENGTH_SHORT).show();
    }
    });
     dialog.show();


     *
     */
}