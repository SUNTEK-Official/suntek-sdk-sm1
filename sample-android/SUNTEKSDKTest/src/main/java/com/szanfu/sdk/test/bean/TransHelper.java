package com.szanfu.sdk.test.bean;

import com.szanfu.sdk.test.util.CardSearchType;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class TransHelper {


    //////////////////////////////////////////////////////////////
    //Trans Type
    //////////////////////////////////////////////////////////////
    public static final int TRANS_TYPE_WITHDRAWL = 1;
    public static final int TRANS_TYPE_PURCHASE = 2;
    public static final int TRANS_TYPE_ENQUIRY = 3;

    public static final int TRANS_TYPE_SIGNIN = 10;
    public static final int TRANS_TYPE_DOWNLOAD_EMV_PARAMETERS = 11;
    public static final int TRANS_TYPE_UPDATE_FIRMWARE = 12;

    public static final int TRANS_TYPE_PERSONALIZE = 20;

    public static final int TRANS_TYPE_INJECT_KEY_F = 21;
    public static final int TRANS_TYPE_MISC = 22;

    //////////////////////////////////////////////////////////////
    //Kernel Type (Contactless)
    //////////////////////////////////////////////////////////////

    //Contact
    public static final int C_CARD_EMV = 0;         //Contact ENV

    //Contactless
    public static final int CL_CARD_ENTRY_POINT = 10;
    public static final int CL_CARD_QPASS = 11;		//Qpass
    public static final int CL_CARD_VISA = 12;		//Visa
    public static final int CL_CARD_MASTER = 13;	    //Master
    public static final int CL_CARD_DPASS = 14;		//DISCOVER
    public static final int CL_CARD_AMEX = 15;		//Amex
    public static final int CL_CARD_JCB = 16;		    //Jcb
    public static final int CL_CARD_MIR = 17;		    //MIR
    public static final int CL_CARD_RUPAY = 18;		//Rupay qSparc
    public static final int CL_CARD_MAESTRO = 19;     //Maestro

    //////////////////////////////////////////////////////////////
    //Events
    //////////////////////////////////////////////////////////////

    //public Event egroup_AF_Listener = new Event("AF");
    public final int AF_LISTENER__GET_DEVINFO = 0x01;
    public final int AF_LISTENER__FORMAT_PINPAD = 0x02;
    public final int AF_LISTENER__SET_DATETIME_SUCCESSFUL = 0x04;
    public final int AF_LISTENER__MODIFY_DEVICE_TITLENAME_SUCCESS = 0x08;
    public final int AF_LISTENER__MIDIFY_BLUETOOTHNAME_SUCCESS = 0x10;
    public final int AF_LISTENER__DEVICE_BATTERY_PARAM = 0x20;
    public final int AF_LISTENER__UPDATE_AID_SUCCESSFUL = 0x40;
    public final int AF_LISTENER__GET_EMV_TLV_DATA_LIST = 0x80;
    public final int AF_LISTENER__SET_EMV_TLV_DATA_SUCCESSFUL = 0x100;
	//NOTE-2021030301
    public final int AF_LISTENER__LOAD_PARAMETER_SUCCESS = 0x200;
    public final int AF_LISTENER__LOAD_PARAMETER_FAIL = 0x400;
    public final int AF_LISTENER__LOAD_CADATA_SUCCESS = 0x800;
    public final int AF_LISTENER__LOAD_CADATA_FAIL = 0x1000;

    public final int AF_LISTENER__ON_ERROR = 0x10000;
    public final int AF_LISTENER__ON_TIMEOUT = 0x40000;

    //////////////////////////////////////////////////////////////
    //Variable
    //////////////////////////////////////////////////////////////

    public String g_last_random = null;
    public String g_TERMINALSN = null;
    public String g_MACAddress = null;

    public Map<String, String> deviceInfos = null;
    public Map<String, Integer> deviceBatteryParam = null;
    public String cur_datetime = "";
    public ArrayList<String> devicelog = null;

    public HashMap<Integer, byte[]> hashmap_tlv_db = null;
    public String swap_tlv_data_lists;

    public int trans_type;
    public String trans_amount;
    public String cashback_amount;
    public BigDecimal trans_amount_b;
    public BigDecimal cashback_amount_b;

    public boolean need_fallabck = false;

    public int card_type;
    public int c_kernel_type = 0;

    public Map<String, String> cardInfos;
    public boolean has_chip_oncard = false;
    public String card_pan;
    public String card_aid;      //4F:   Application Dedicated File (ADF) Name
    public String card_expired;  //5F24: Application Expiration Date
    public String card_track1;   //9F1F: Track 1 Discretionary Data
    public String card_track2;   //9F20: Track 2 Discretionary Data
    public String card_track3;
    public String card_CSN;     //5F34: Application Primary Account Number (PAN) Sequence Number
    public String card_icdata;
    public byte[] tagAID = null;                  // 9F06 AID
    public byte[] tagIssuerScriptResult = null;   //DF31
    public boolean need_onlinepin = true;
    public boolean need_signature = false;
    public boolean need_online = true;
    public boolean has_gac1 = false;
	//#2021050601
    public int pin_type = 0;    //0: online PIN  1: offline plaintext PIN
    public int pin_leftRetryTimes = 3;

    public Map<String, String> pinInfo;
    public String pinblock;
    public boolean bypassPIN; //for RUPAY.DEV.A.039
    public boolean manual_pin; //for RUPAY.DEV.A.039

    public int host_resp_onlineResult = 0;
    public String host_resp_chipdata = "";
    public String host_resp_tag91 = "";
    public String host_resp_tag8A = "";
    public String host_resp_tag71 = "";
    public String host_resp_tag72 = "";
    public String host_resp_Authorization_Response_Code = "";  //normally it's DE39 of 8583

    public int gac2_result = 0;
    public String gac2_chipdata = "";

    public String last_error_code;
    public int last_error_type;

    public String DE39_Value;

    public static String terminal_country_code = "";  //9F1A:  "Terminal Country Code"

    //////////////////////////////////////////////////////////////
    //Transaction State Machine
    //////////////////////////////////////////////////////////////

    public static final int STATE__INIT = 0;
    public static final int STATE__CONNECT_DEVICE = 1;
    public static final int STATE__PRESEND_CARD = 2;

    public static final int STATE__ENTER_ONLINE_PIN = 20;
    public static final int STATE__ONLINE_COMMU = 21;

    public static final int STATE__APPROVE = 90;
    public static final int STATE__DECLINE = 91;
    public static final int STATE__TERMINATE = 92;

    private int cur_state = STATE__INIT;

    //////////////////////////////////////////////////////////////
    //Message
    //////////////////////////////////////////////////////////////

    private static final int msg_base = 1000;

    public static final int MSG_ON_DEVICE_SCANNING = msg_base + 0;
    public static final int MSG_ON_DEVICE_LIST_REFRESH = msg_base + 1;
    public static final int MSG_ON_DEVICE_SCAN_STOPPED = msg_base + 2;

    public static final int MSG_ON_DEVICE_CONNECTED = msg_base + 10;
    public static final int MSG_ON_DEVICE_DISCONNECTED = msg_base + 11;
    public static final int MSG_ON_DEVICE_CONNECT_FAILED = msg_base + 12;
    public static final int MSG_ON_WAITING_FOR_DEVICE = msg_base + 13;

    public static final int MSG_START_TRANSCTION =  msg_base + 14;
    public static final int MSG_FORCE_USING_CHIPCARD =  msg_base + 15;
    public static final int MSG_FALLBACK =  msg_base + 16;
    public static final int MSG_GET_BATTERY_INFO =  msg_base + 17;

    public static final int MSG_ON_NEED_INSERT_ICC = msg_base + 20;
    public static final int MSG_ON_WAITING_FOR_CARD_SWIPE = msg_base + 21;
    public static final int MSG_ON_DETECT_CARD = msg_base + 22;
    public static final int MSG_ON_RETURN_CARD_INFO = msg_base + 23;

    public static final int MSG_ENTER_ONLINE_PIN = msg_base + 24;
    public static final int MSG_ON_RETURN_PINBLOCK = msg_base + 25;
    public static final int MSG_ONLINE_COMM_MSR = msg_base + 26;
    public static final int MSG_ONLINE_COMM_PICC = msg_base + 27;

    public static final int MSG_ON_PRESS_CANCEL_KEY = msg_base + 30;
    public static final int MSG_ON_CANCEL_SWIPE = msg_base + 31;

    public static final int MSG_ON_PBOC_TWO = msg_base + 40;
    public static final int MSG_SIGNATURE = msg_base + 41;

    public static final int MSG_ON_TIMEOUT = msg_base + 50;
    public static final int MSG_ON_ERROR = msg_base + 51;

    public static final int MSG_ON_GET_ESIGNATURE_DATA = msg_base + 60;
    public static final int MSG_ON_GENERATE_QRCODE_SUCCESS = msg_base + 61;

    public static final int MSG_ON_SET_DATETIME_SUCCESS = msg_base + 62;
    public static final int MSG_ON_DEVICE_CURRENT_TIME = msg_base + 63;

    public static final int MSG_ON_RETURN_DEVICEINFO = msg_base + 64;
    public static final int MSG_ON_DEVICE_BATTERY_PARAM = msg_base + 65;

    public static final int MSG_ON_MIDIFY_DEVICE_KSN_SUCCESS = msg_base + 66;
    public static final int MSG_ON_MODIFY_DEVICE_TITLENAME_SUCCESS = msg_base + 67;
    public static final int MSG_ON_MIDIFY_BLUETOOTHNAME_SUCCESS = msg_base + 68;

    public static final int MSG_ON_RETURN_DEVICE_LOG = msg_base + 69;

    public static final int MSG_ON_UPDATE_FIRMWARE_SUCCESS = msg_base + 70;
    public static final int MSG_ON_UPDATE_FIRMWARE_PROCESS = msg_base + 71;

    public static final int MSG_ON_RETRUN_MONEY = msg_base + 80;

    public static final int MSG_ON_UPDATE_AID_SUCCESS = msg_base + 81;

    public static final int MSG_ON_GET_EMV_TLV_DATA_LIST = msg_base + 82;
    public static final int MSG_ON_SET_EMV_TLV_DATA_SUCCESS = msg_base + 83;

	//NOTE-2021030301
    public static final int MSG_ON_LOAD_PARAMETER_SUCCESS = msg_base + 84;
    public static final int MSG_ON_LOAD_PARAMETER_FAILED = msg_base + 85;

	//NOTE-2021030301
    public static final int MSG_ON_LOAD_CADATA_SUCCESS = msg_base + 86;
    public static final int MSG_ON_LOAD_CADATA_FAILED = msg_base + 87;

    public static final int MSG_ON_CANCEL = msg_base + 88;

    //////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////

    private static TransHelper INSTANCE = null;

    void TransHelper() {
    }

    public static TransHelper getInstance()
    {
        if(INSTANCE == null)
        {
            synchronized (TransHelper.class)
            {
                if(INSTANCE == null)
                {
                    INSTANCE = new TransHelper();
                }
            }
        }

        return INSTANCE;
    }

    //////////////////////////////////////////////////////////////

    public void reset() {

        if(hashmap_tlv_db != null)
            hashmap_tlv_db.clear();
        hashmap_tlv_db = new HashMap<Integer, byte[]>();

        swap_tlv_data_lists = null;

        trans_type = TRANS_TYPE_PURCHASE;
        trans_amount = "";
        cashback_amount = "";

        /* don't clear it, maybe other trans will share this info
        if(deviceInfos != null) {
            deviceInfos.clear();
            deviceInfos = null;
        }

        if(deviceBatteryParam != null) {
            deviceBatteryParam.clear();
            deviceBatteryParam = null;
        }
        */
        cur_datetime = "";
        if(devicelog != null) {
            devicelog.clear();
            devicelog = null;
        }

        need_fallabck = false;

        if(cardInfos != null) {
            cardInfos.clear();
            cardInfos = null;
        }

        if(pinInfo != null) {
            pinInfo.clear();
            pinInfo = null;
        }
        pinblock = "";
        bypassPIN = false; //for RUPAY.DEV.A.039
        manual_pin = false;//for RUPAY.DEV.A.039

        has_chip_oncard = false;
        card_pan = "";
        card_aid = "";
        card_expired = "";
        card_track1 = "";
        card_track2 = "";
        card_track3 = "";
        card_CSN = "";
        card_icdata = "";
        tagAID = null;
        tagIssuerScriptResult = null;
        need_onlinepin = true;
        need_signature = false;
        need_online = true;
        has_gac1 = false;
		//#2021050601
        pin_type = 0;
        pin_leftRetryTimes = 3;

        host_resp_onlineResult = 0;
        host_resp_chipdata = "";
        host_resp_tag91 = "";
        host_resp_tag8A = "";
        host_resp_tag71 = "";
        host_resp_tag72 = "";
        host_resp_Authorization_Response_Code = "";

        gac2_result = 0;
        gac2_chipdata = "";

        card_type = CardSearchType.MAGNETIC_STRIPE_CARD;
        c_kernel_type = 0;
        last_error_code = "";
        last_error_type = -1;

        DE39_Value = "";
        cur_state = STATE__INIT;

        terminal_country_code = "";

        terminalCapabilities = new TerminalCapabilities();
    }

    public void setState(int state) {
        this.cur_state = state;
    }

    public int getState() {
        return cur_state;
    }

    //////////////////////////////////////////////////////////////////////////////////////////

    public TerminalCapabilities terminalCapabilities = new TerminalCapabilities();

    public int Parse_9F33(byte[] tag_9F33) {
        if(tag_9F33 == null || tag_9F33.length != 3)
            return -1;

        if( (tag_9F33[0] & 0x80) == 0x80)
            terminalCapabilities.manual_key_entry = true;
        if( (tag_9F33[0] & 0x40) == 0x40)
            terminalCapabilities.magnetic_stripe = true;
        if( (tag_9F33[0] & 0x20) == 0x20)
            terminalCapabilities.ic_with_contacts = true;

        if( (tag_9F33[1] & 0x80) == 0x80)
            terminalCapabilities.plaintext_pin_for_icc_verification = true;
        if( (tag_9F33[1] & 0x40) == 0x40)
            terminalCapabilities.enciphered_pin_for_online_verification = true;
        if( (tag_9F33[1] & 0x20) == 0x20)
            terminalCapabilities.signature_paper = true;
        if( (tag_9F33[1] & 0x10) == 0x10)
            terminalCapabilities.enciphered_pin_for_offline_verification = true;
        if( (tag_9F33[1] & 0x08) == 0x08)
            terminalCapabilities.no_cvm_required = true;

        if( (tag_9F33[2] & 0x80) == 0x80)
            terminalCapabilities.sda = true;
        if( (tag_9F33[2] & 0x40) == 0x40)
            terminalCapabilities.dda = true;
        if( (tag_9F33[2] & 0x20) == 0x20)
            terminalCapabilities.card_capture = true;
        if( (tag_9F33[2] & 0x08) == 0x08)
            terminalCapabilities.cda = true;

        return 0;
    }

    public class TerminalCapabilities {
        //9F33.B0
        public boolean manual_key_entry = false;
        public boolean magnetic_stripe = false;
        public boolean ic_with_contacts = false;

        //9F33.B1
        public boolean plaintext_pin_for_icc_verification = false;
        public boolean enciphered_pin_for_online_verification = false;
        public boolean signature_paper = false;
        public boolean enciphered_pin_for_offline_verification = false;
        public boolean no_cvm_required = false;

        //9F33.B3
        public boolean sda = false;
        public boolean dda = false;
        public boolean card_capture = false;
        public boolean cda = false;

        public TerminalCapabilities() {
        }

        public String toString() {
            return toString("");
        }
        public String toString(String prefix) {
            StringBuffer sb = new StringBuffer();

            sb.append(prefix + "manual_key_entry=" + manual_key_entry);
            sb.append("\n" + prefix + "magnetic_stripe=" + magnetic_stripe);
            sb.append("\n" + prefix + "ic_with_contacts=" + ic_with_contacts);

            sb.append("\n" + prefix + "plaintext_pin_for_icc_verification=" + plaintext_pin_for_icc_verification);
            sb.append("\n" + prefix + "enciphered_pin_for_online_verification=" + enciphered_pin_for_online_verification);
            sb.append("\n" + prefix + "signature_paper=" + signature_paper);
            sb.append("\n" + prefix + "enciphered_pin_for_offline_verification=" + enciphered_pin_for_offline_verification);
            sb.append("\n" + prefix + "no_cvm_required=" + no_cvm_required);

            sb.append("\n" + prefix + "sda=" + sda);
            sb.append("\n" + prefix + "dda=" + dda);
            sb.append("\n" + prefix + "card_capture=" + card_capture);
            sb.append("\n" + prefix + "cda=" + cda);

            return sb.toString();
        }
    }
}
