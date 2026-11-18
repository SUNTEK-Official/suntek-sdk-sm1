package com.szanfu.sdk.test.entity;

/**
 * working key
 */
public class WorkingKeyEntity {

    public static final int SNK_KEY_ID = 0;
    public static final int TLK_KEY_ID = 1; // TLK
    public static final int TMK_KEY_ID = 2; //master key
    public static final int PIN_KEY_ID = 3; //pin key
    public static final int MAC_KEY_ID = 4; //mac key
    public static final int TDK_KEY_ID = 5; //track key (TDEA decrypt key)
    public static final int TCK_KEY_ID = 6; //data key (Communication key)
    //key type
    private int keyID;

    /**
     * Cipher Value of the key
     */
    private String keyData;
    /**
     * Check Value
     */
    private String keyCheckValues;

    public int getKeyID() {
        return keyID;
    }

    public String getKeyIDString() {
        if(keyID == TMK_KEY_ID)
            return "TMK";
        else if(keyID == PIN_KEY_ID)
            return "PIK";
        else if(keyID == MAC_KEY_ID)
            return "MAK";
        else if(keyID == TDK_KEY_ID)
            return "TDK";
        else if(keyID == TLK_KEY_ID)
            return "TLK";
        else if(keyID == TCK_KEY_ID)
            return "DataKey";
        else
            return "unknown";
    }
    public void setKeyID(int keyID) {
        this.keyID = keyID;
    }

    public String getKeyData() {
        return keyData;
    }

    public void setKeyData(String keyData) {
        this.keyData = keyData;
    }

    public String getKeyCheckValues() {
        return keyCheckValues;
    }

    public void setKeyCheckValues(String keyCheckValues) {
        this.keyCheckValues = keyCheckValues;
    }

    @Override
    public String toString() {
                
        String keyInfo = "";
        switch (keyID) {
            case 1:
                keyInfo = "PIK";
                break;
            case 2:
                keyInfo = "MAK";
                break;
            case 5:
                keyInfo = "TDK";
                break;
        }
        return keyInfo + "\t{" +
                "keyID='" + keyID + '\'' +
                ", keyData='" + keyData + '\'' +
                ", keyCheckValues='" + keyCheckValues + '\'' +
                '}';
    }
}
