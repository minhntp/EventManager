package com.nqm.event_manager.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.nqm.event_manager.R;

public class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);

//        Thread.setDefaultUncaughtExceptionHandler(handleAppCrash);
    }

    private final Thread.UncaughtExceptionHandler handleAppCrash = (thread, exception) -> {
        System.out.println( ": " + exception.toString());
        String[] TO = {"nguyenquangminhntp@gmail.com"};
        String[] CC = {""};
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setDataAndType(Uri.parse("mailto:"), "text/plain");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, TO);
        emailIntent.putExtra(Intent.EXTRA_CC, CC);
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Event Manager Bug");
        emailIntent.putExtra(Intent.EXTRA_TEXT, "Thread:\n" + thread.toString() +
                "\n\nException:\n" + exception.toString());

        try {
            startActivity(Intent.createChooser(emailIntent, "Send mail..."));
            finish();
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, "No email client installed!", Toast.LENGTH_SHORT).show();
        }
    };
}