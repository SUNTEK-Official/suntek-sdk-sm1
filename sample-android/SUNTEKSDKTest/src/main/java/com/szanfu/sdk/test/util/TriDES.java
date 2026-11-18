package com.szanfu.sdk.test.util;

import android.inputmethodservice.Keyboard;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;
import javax.crypto.spec.IvParameterSpec;

public class TriDES {
    public static byte[] des3EncodeECB(byte[] key, byte[] data)
            throws Exception {
        SecretKey deskey = null;
        byte[] newkey = new byte[24];
        if (key.length < 24) {
            System.arraycopy(key, 0, newkey, 0, 16);
            System.arraycopy(key, 0, newkey, 16, 8);
        } else {
            System.arraycopy(key, 0, newkey, 0, 24);
        }
        DESedeKeySpec spec = new DESedeKeySpec(newkey);
        SecretKeyFactory keyfactory = SecretKeyFactory.getInstance("desede");
        deskey = keyfactory.generateSecret(spec);
        Cipher cipher = Cipher.getInstance("desede" + "/ECB/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, deskey);
        byte[] bOut = cipher.doFinal(data);
        return bOut;
    }

    public static byte[] des3DecodeECB(byte[] key, byte[] data)
            throws Exception {

        //Keyboard.Key deskey = null;
        SecretKey deskey = null;
        byte[] newkey = new byte[24];
        if (key.length < 24) {
            System.arraycopy(key, 0, newkey, 0, 16);
            System.arraycopy(key, 0, newkey, 16, 8);
        } else {
            System.arraycopy(key, 0, newkey, 0, 24);
        }
        DESedeKeySpec spec = new DESedeKeySpec(newkey);
        SecretKeyFactory keyfactory = SecretKeyFactory.getInstance("desede");
        //deskey = (Keyboard.Key) keyfactory.generateSecret(spec);
        deskey = keyfactory.generateSecret(spec);
        Cipher cipher = Cipher.getInstance("desede" + "/ECB/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, (java.security.Key) deskey);
        byte[] bOut = cipher.doFinal(data);
        return bOut;
    }

    public static byte[] des3EncodeCBC(byte[] key, byte[] keyiv, byte[] data)
            throws Exception {

        SecretKey deskey = null;
        byte[] newkey = new byte[24];
        byte[] newiv = new byte[8];
        if (key.length < 24) {
            System.arraycopy(key, 0, newkey, 0, 16);
            System.arraycopy(key, 0, newkey, 16, 8);
        } else {
            System.arraycopy(key, 0, newkey, 0, 24);
        }
        if (keyiv.length > 8) {
            System.arraycopy(keyiv, 0, newiv, 0, 8);

        } else {
            System.arraycopy(keyiv, 0, newiv, 0, 8);
        }
        DESedeKeySpec spec = new DESedeKeySpec(newkey);
        SecretKeyFactory keyfactory = SecretKeyFactory.getInstance("desede");
        deskey = keyfactory.generateSecret(spec);

        Cipher cipher = Cipher.getInstance("desede" + "/CBC/NoPadding");
        IvParameterSpec ips = new IvParameterSpec(newiv);
        cipher.init(Cipher.ENCRYPT_MODE, deskey, ips);
        byte[] bOut = cipher.doFinal(data);

        return bOut;
    }

    public static byte[] des3DecodeCBC(byte[] key, byte[] keyiv, byte[] data)
            throws Exception {

        Keyboard.Key deskey = null;
        DESedeKeySpec spec = new DESedeKeySpec(key);
        SecretKeyFactory keyfactory = SecretKeyFactory.getInstance("desede");
        deskey = (Keyboard.Key) keyfactory.generateSecret(spec);

        Cipher cipher = Cipher.getInstance("desede" + "/CBC/NoPadding");
        IvParameterSpec ips = new IvParameterSpec(keyiv);

        cipher.init(Cipher.DECRYPT_MODE, (java.security.Key) deskey, ips);

        byte[] bOut = cipher.doFinal(data);

        return bOut;
    }
}
