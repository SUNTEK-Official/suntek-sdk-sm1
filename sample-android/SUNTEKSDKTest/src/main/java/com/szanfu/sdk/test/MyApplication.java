package com.szanfu.sdk.test;

import android.app.Application;
import android.content.Intent;

import com.szanfu.sdk.test.db.RealmDbOpenHelper;
import com.szanfu.sdk.test.util.LogUtils;

import io.github.suzp1984.jbig.JniJbigCodec;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.exceptions.RealmError;
import io.realm.exceptions.RealmFileException;

public class MyApplication extends Application {
    RealmDbOpenHelper dbOpenHelper;
    @Override
    public void onCreate() {
        LogUtils.init(this);
        new JniJbigCodec();

        Realm.init(this);
        RealmConfiguration config = new RealmConfiguration.Builder()
                .deleteRealmIfMigrationNeeded()
                .build();
        Realm.setDefaultConfiguration(config);

        try{
            Realm.getDefaultInstance();
        } catch(RealmError | RealmFileException error) {
            rebuildRealm(config);
        } finally {
            dbOpenHelper = new RealmDbOpenHelper(Realm.getDefaultInstance());
        }


        super.onCreate();
    }

    public RealmDbOpenHelper getDbOpenHelper() {
        return dbOpenHelper;
    }

    private void rebuildRealm(RealmConfiguration configuration){
        Realm.deleteRealm(configuration);
        Realm.setDefaultConfiguration(configuration);
    }


}
