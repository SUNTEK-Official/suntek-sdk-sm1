package com.szanfu.sdk.test.util;
/**
 * LED 状态枚举
 */
public enum LedState {
    OFF(0x00), // LED
    RED_ON(0x01),
    GREEN_ON(0x02),
    YELLOW_ON(0x04),
    BLUE_ON(0x08),
    RED_BLINK(0x10),
    GREEN_BLINK(0x20),
    YELLOW_BLINK(0x40),
    BLUE_BLINK(0x80);

    private final int value;

    LedState(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    /**
     * 根据整数值获取 LED 状态,默认返回 OFF
     */
    public static LedState fromValue(int value) {
        for (LedState state : values()) {
            if (state.value == value) {
                return state;
            }
        }
        return OFF;
    }
}
