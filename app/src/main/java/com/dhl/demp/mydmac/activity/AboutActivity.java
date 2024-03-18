package com.dhl.demp.mydmac.activity;

import android.annotation.SuppressLint;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import android.text.Html;
import android.text.Spanned;
import android.view.View;
import android.widget.TextView;

import com.dhl.demp.mydmac.LauncherPreference;
import com.dhl.demp.mydmac.utils.Constants;
import com.dhl.demp.mydmac.utils.RootUtil;
import com.dhl.demp.mydmac.utils.Utils;

import java.util.Locale;
import java.util.TimeZone;

import mydmac.BuildConfig;
import mydmac.R;

/**
 * Created by rd on 08/09/16.
 */
public class AboutActivity extends AppCompatActivity {
    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_back);
        toolbar.setBackgroundColor(getColor(R.color.build_type_color));
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        Typeface typeFaceNew =Typeface.createFromAsset(getAssets(), Constants.MAIN_FONT_PATH);
        Typeface typeFaceBold =Typeface.createFromAsset(getAssets(), Constants.MAIN_FONT_BOLD_PATH);


        TextView textView = (TextView) findViewById(R.id.about_header);
        if(BuildConfig.FLAVOR.equals("dmac")
                || BuildConfig.FLAVOR.equals("express")
                || BuildConfig.FLAVOR.equals("dmacTip")
                || BuildConfig.FLAVOR.equals("expressTip")
                || BuildConfig.FLAVOR.equals("tca")){
            textView.setTextColor(ContextCompat.getColor(this, R.color.toolbar_title_color));
        }
        textView.setText(getString(R.string.app_name) /*+ getEnvironmentLabel()*/);
        textView.setTypeface(typeFaceBold);

        textView = (TextView) findViewById(R.id.about_build_title);
        textView.setTypeface(typeFaceBold);

        textView = (TextView) findViewById(R.id.time_zone_title);
        textView.setTypeface(typeFaceBold);

        textView = (TextView) findViewById(R.id.time_zone_value);
        textView.setTypeface(typeFaceNew);
        textView.setText(TimeZone.getDefault().getDisplayName(false, TimeZone.SHORT, Locale.US));

        textView = (TextView) findViewById(R.id.os_version_title);
        textView.setTypeface(typeFaceBold);

        textView = (TextView) findViewById(R.id.os_version_value);
        textView.setTypeface(typeFaceNew);
        textView.setText("Android " + Build.VERSION.RELEASE);

        textView = (TextView) findViewById(R.id.kernel_version_title);
        textView.setTypeface(typeFaceBold);

        textView = (TextView) findViewById(R.id.kernel_version_value);
        textView.setTypeface(typeFaceNew);
        textView.setText(System.getProperty("os.version"));

        textView = (TextView) findViewById(R.id.patch_version_title);
        textView.setTypeface(typeFaceBold);

        textView = (TextView) findViewById(R.id.patch_version_value);
        textView.setTypeface(typeFaceNew);
        String patchLevel = Utils.getSecurityPatchLevel();
        textView.setText(patchLevel);

        textView = (TextView) findViewById(R.id.manufacturer_title);
        textView.setTypeface(typeFaceBold);

        textView = (TextView) findViewById(R.id.manufacturer_value);
        textView.setTypeface(typeFaceNew);
        textView.setText(Build.MANUFACTURER);

        textView = (TextView) findViewById(R.id.about_build_number);
        textView.setTypeface(typeFaceNew);
        textView.setText(BuildConfig.VERSION_NAME);

        textView = (TextView) findViewById(R.id.androidIDLabel);
        textView.setTypeface(typeFaceBold);

        String device_unique_id = Utils.getDeviceUniqueId(this);
        textView = (TextView) findViewById(R.id.androidIDVal);
        textView.setTypeface(typeFaceNew);
        textView.setText(device_unique_id);

        textView = (TextView) findViewById(R.id.androidMODELLabel);
        textView.setTypeface(typeFaceBold);

        textView = (TextView) findViewById(R.id.androidMODELVal);
        textView.setTypeface(typeFaceNew);
        textView.setText(Build.MODEL);

        boolean autoreg = LauncherPreference.isAutoreg(this);
        if (autoreg) {
            //IMEI
            textView = (TextView) findViewById(R.id.about_imei_title);
            textView.setVisibility(View.VISIBLE);
            textView.setTypeface(typeFaceBold);

            textView = (TextView) findViewById(R.id.about_imei);
            textView.setVisibility(View.VISIBLE);
            textView.setTypeface(typeFaceNew);
            if(Utils.isAndroid10()){
                textView.setText(LauncherPreference.getImeiManual(this));
            }else {
                textView.setText(Utils.getIMEI(this));
            }
            //SN
            textView = (TextView) findViewById(R.id.about_sn_title);
            textView.setVisibility(View.VISIBLE);
            textView.setTypeface(typeFaceBold);

            textView = (TextView) findViewById(R.id.about_sn);
            textView.setVisibility(View.VISIBLE);
            textView.setTypeface(typeFaceNew);
            textView.setTypeface(typeFaceNew);
            if(Utils.isAndroid10()){
                textView.setText(LauncherPreference.getSnManual(this));
            }else {
                textView.setText(Utils.getSerialNumber());
            }
        } else {
            textView = (TextView) findViewById(R.id.about_email_title);
            textView.setVisibility(View.VISIBLE);
            textView.setTypeface(typeFaceBold);

            textView = (TextView) findViewById(R.id.about_email_address);
            textView.setVisibility(View.VISIBLE);
            textView.setTypeface(typeFaceNew);
            textView.setText(LauncherPreference.getClientEmail(this));
        }

        findViewById(R.id.rooted_device_message).setVisibility(RootUtil.isDeviceRooted() ? View.VISIBLE : View.GONE);
    }

    @SuppressWarnings("deprecation")
    public Spanned fromHtml(int source) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Html.fromHtml(getString(source), Html.FROM_HTML_MODE_LEGACY);
        } else {
            return Html.fromHtml(getString(source));
        }
    }

//    private String getEnvironmentLabel() {
//        String nameSuffix = "";
//        //if (BuildConfig.FLAVOR.equals("dmac")) {
//            if (BuildConfig.BUILD_TYPE.equals(Constants.BuildType.DEBUG)) {
//                nameSuffix = " TEST";
//            }
//            else if (BuildConfig.BUILD_TYPE.equals(Constants.BuildType.UAT)) {
//                nameSuffix = " UAT";
//            }
////        }else if (!BuildConfig.FLAVOR.equals("dmac")){
////            if (BuildConfig.BUILD_TYPE.equals(Constants.BuildType.DEBUG)) {
////                nameSuffix = " (TEST)";
////            }
////            else if (BuildConfig.BUILD_TYPE.equals(Constants.BuildType.UAT)) {
////                nameSuffix = " (UAT)";
////            }
////        }
//        return nameSuffix;
//    }
}
