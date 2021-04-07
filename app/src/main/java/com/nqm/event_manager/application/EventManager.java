package com.nqm.event_manager.application;

import android.app.Application;
import android.content.Context;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.nqm.event_manager.BuildConfig;
import com.nqm.event_manager.R;
import com.nqm.event_manager.utils.Constants;

import org.acra.ACRA;
import org.acra.annotation.AcraCore;
import org.acra.annotation.AcraMailSender;
import org.acra.annotation.AcraToast;
import org.acra.data.StringFormat;

//@AcraCore(buildConfigClass = BuildConfig.class)
//@AcraMailSender(mailTo = "nguyenquangminhntp@gmail.com")
//@AcraToast(resText = R.string.crash_toast_text)
@AcraCore(buildConfigClass = BuildConfig.class)
@AcraMailSender(mailTo = "nguyenquangminhntp@gmail.com")
@AcraToast(resText=R.string.crash_toast_text, length = Toast.LENGTH_SHORT)
public class EventManager extends Application {

    @Override
    public void onCreate() {
//        ACRA.init(this);
        FirebaseApp.initializeApp(this);
        super.onCreate();
//        EventManager.context = getApplicationContext();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        ACRA.init(this);
    }

//    public static Context getAppContext() {
//        return EventManager.context;
//    }
}
