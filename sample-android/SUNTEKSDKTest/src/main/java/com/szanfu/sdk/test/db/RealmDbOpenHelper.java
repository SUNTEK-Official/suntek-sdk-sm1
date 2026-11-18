package com.szanfu.sdk.test.db;

import android.util.Log;

import com.szanfu.sdk.test.util.LogUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;

/**
 * Created by moses on 8/31/15.
 */
public class RealmDbOpenHelper implements DataBaseHelper {

    private Realm mRealm;

    private final List<byte[]> mJbigList = new ArrayList<>();

    public RealmDbOpenHelper(Realm realm) {
        mRealm = realm;
    }

    @Override
    public List<byte[]> getJbigs() {
        List<byte[]> ret = new ArrayList<>();

        RealmQuery<JbigItem> query = mRealm.where(JbigItem.class);
        RealmResults<JbigItem> results = query.findAll();

        for (JbigItem item : results) {
            ret.add(item.jbig);
        }

        return ret;
    }

    @Override
    public byte[] getJbig(int position) {
        RealmQuery<JbigItem> query = mRealm.where(JbigItem.class);
        RealmResults<JbigItem> results = query.findAll();

        JbigItem item = results.get(position);

        if (item != null) {
            return item.jbig;
        }

        return null;
    }

    @Override
    public void put(byte[] jbig) {
        LogUtils.d("ReamlDb"+ "save realm object.");
        mRealm.beginTransaction();
        JbigItem item = mRealm.createObject(JbigItem.class);
        item.tag = "";
        item.jbig = jbig;
        mRealm.commitTransaction();
    }

    @Override
    public void put(Collection<byte[]> jbigs) {
        for(byte[] item : jbigs) {
            put(item);
        }
    }

    @Override
    public void delete(byte[] jbig) {
        RealmQuery<JbigItem> query = mRealm.where(JbigItem.class);
        RealmResults<JbigItem> results = query.findAll();

        for (JbigItem item : results) {
            if (item.jbig == jbig) {
                mRealm.beginTransaction();
                item.deleteFromRealm();
                mRealm.commitTransaction();
                break;
            }
        }
    }

    @Override
    public void delete(int position) {
        mRealm.beginTransaction();

        RealmQuery<JbigItem> query = mRealm.where(JbigItem.class);
        RealmResults<JbigItem> results = query.findAll();
        results.deleteFromRealm(position);

        mRealm.commitTransaction();
    }

    @Override
    public void deleteAll() {
        mRealm.beginTransaction();

        RealmQuery<JbigItem> query = mRealm.where(JbigItem.class);
        RealmResults<JbigItem> results = query.findAll();
        results.deleteAllFromRealm();

        mRealm.commitTransaction();
    }
}
