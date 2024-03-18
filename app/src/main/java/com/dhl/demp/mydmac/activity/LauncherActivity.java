package com.dhl.demp.mydmac.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.MediaStore;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.dhl.demp.dmac.ui.MainActivity;
import com.dhl.demp.mydmac.LauncherApplication;
import com.dhl.demp.mydmac.LauncherPreference;
import com.dhl.demp.mydmac.adapter.GridAdapter;
import com.dhl.demp.mydmac.obj.Apka;
import com.dhl.demp.mydmac.utils.Constants.AppPackageName;
import com.dhl.demp.mydmac.utils.filter.Filter;
import com.dhl.demp.mydmac.utils.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import mydmac.R;

import static com.dhl.demp.mydmac.utils.Constants.StandardAppFlag.*;

/**
 * Created by rd on 10/10/16.
 */

public class LauncherActivity extends AppCompatActivity {
    private static final int[] APP_FLAGS = {
            FLAG_MAP,
            FLAG_CHROME,
            FLAG_SCAN_TO_CONNECT,
            FLAG_STAGE_NOW,
            FLAG_MOBICONTROL
    };
    private static final String[] APP_PACKAGE_NAMES = {
            "com.google.android.apps.maps",
            "com.android.chrome",
            AppPackageName.SCAN_TO_CONNECT,
            AppPackageName.STAGE_NOW,
            AppPackageName.MOBICONTROL
    };

    private static final String TAG = LauncherActivity.class.getCanonicalName();
    private static final int REQUEST_CODE_READ_EXTERNAL_STORAGE = 1;
    private final Filter<Apka> launcherApkaFilter = new LauncherApkaFilter();
    private GridAdapter adapter;
    private TextView title;
    private ImageView bgContainer;
    private View launcherActionPhone;
    private View launcherActionSms;
    private View launcherActionCamera;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.launcher_activity);

        initView();
    }

    private void initView() {
        int standardApps = LauncherPreference.getStandardApps(this);
        int listTitleColor = LauncherPreference.getWallpaperTextColor(this);
        GridView grid = (GridView) findViewById(R.id.applist);
        adapter = new GridAdapter(getApplicationContext(), listTitleColor, getAppsForLauncher(standardApps));
        grid.setAdapter(adapter);

        launcherActionPhone = findViewById(R.id.launcher_action_phone);
        launcherActionSms = findViewById(R.id.launcher_action_sms);
        launcherActionCamera = findViewById(R.id.launcher_action_camera);

        launcherActionPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                startActivity(intent);
            }
        });
        launcherActionSms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_APP_MESSAGING);
                startActivity(intent);
            }
        });
        launcherActionCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA);
                startActivity(intent);
            }
        });
        findViewById(R.id.launcher_action_personalization).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PersonalizationActivity.start(LauncherActivity.this);
            }
        });
        findViewById(R.id.launcher_action_app).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.start(LauncherActivity.this, Intent.FLAG_ACTIVITY_CLEAR_TOP);
            }
        });

        title = (TextView) findViewById(R.id.launcher_title);
        bgContainer = (ImageView) findViewById(R.id.bg);
    }

    private void adjustTitle() {
        title.setTextColor(LauncherPreference.getWallpaperTextColor(this));
        String text = LauncherPreference.getDescription(this);
        title.setVisibility(TextUtils.isEmpty(text) ? View.GONE : View.VISIBLE);
        title.setText(text);
    }

    private void adjustBg() {
        String backgroundPath = LauncherPreference.getWallpaper(this);
        if (TextUtils.isEmpty(backgroundPath)) {
            bgContainer.setImageDrawable(null);
        } else {
            if (hasExternalStoragePermission()) {
                Glide.with(getApplicationContext())
                        .load(new File(backgroundPath))
                        .into(bgContainer);
            } else {
                requestExternalStoragePermission();
            }
        }
    }

    private List<GridAdapter.AppInfo> getAppsForLauncher(int standardApps) {
        //LauncherApplication mApp = (LauncherApplication) getApplication();
        //TODO get apps. Use or delete LauncherApkaFilter
        //List<Apka> appsList = Collections.EMPTY_LIST;

        ArrayList<GridAdapter.AppInfo> result = new ArrayList();
        ArrayList<String> packageNames = LauncherPreference.getDMACPackageIds(this);
        ArrayList<String> appNames = LauncherPreference.getDMACAppNames(this);
        ArrayList<String> marketplaceIcon = LauncherPreference.getDMACAppMarketplaceIcon(this);
        for (int index = 0; index < (packageNames.size()); index++) {
            int appInstalledOrNot = Utils.appInstalledOrNot(this, packageNames.get(index), appNames.get(index));
            if(appInstalledOrNot!=0) {
                result.add(new GridAdapter.DMACAppInfo(packageNames.get(index), appNames.get(index), marketplaceIcon.get(index)));
            }
        }

        for (int i = 0; i < APP_FLAGS.length; i++) {
            if ((standardApps & APP_FLAGS[i]) == APP_FLAGS[i]) {
                GridAdapter.SystemAppInfo appInfo = findSystemAppInfo(APP_PACKAGE_NAMES[i]);
                if (appInfo != null) {
                    result.add(appInfo);
                }
            }
        }

        ArrayList<String> shortcutName;
        ArrayList<String> shortcutURL;
        ArrayList<String> shortcutIcon;
        shortcutName = LauncherPreference.getShortcutAppNameList(this);
        shortcutURL = LauncherPreference.getShortcutAppURLList(this);
        shortcutIcon = LauncherPreference.getIconPath(this);

        for(int i = 0; i<shortcutName.size(); i++){
            result.add(new GridAdapter.PwaAppInfo(shortcutURL.get(i), shortcutName.get(i),shortcutIcon.get(i)));
        }

        return result;
    }

    private GridAdapter.SystemAppInfo findSystemAppInfo(String packageName) {
        final PackageManager pm = getPackageManager();
        try {
            ApplicationInfo appInfo = pm.getApplicationInfo(packageName, PackageManager.GET_META_DATA);
            String appName = (String) pm.getApplicationLabel(appInfo);
            return new GridAdapter.SystemAppInfo(packageName, appName);
        } catch (PackageManager.NameNotFoundException e) {}

        return null;
    }

    @Override
    protected void onResume() {
        super.onResume();

        int standardApps = LauncherPreference.getStandardApps(this);
        updateList(standardApps);
        adjustTitle();
        adjustBg();
        adjustStandardApps(standardApps);
    }

    private void adjustStandardApps(int standardApps) {
        launcherActionPhone.setVisibility(((standardApps & FLAG_PHONE) == FLAG_PHONE) ? View.VISIBLE : View.INVISIBLE);
        launcherActionSms.setVisibility(((standardApps & FLAG_SMS) == FLAG_SMS) ? View.VISIBLE : View.INVISIBLE);
        launcherActionCamera.setVisibility(((standardApps & FLAG_CAMERA) == FLAG_CAMERA) ? View.VISIBLE : View.INVISIBLE);
    }

    private void updateList(int standardApps) {
        int listTitleColor = LauncherPreference.getWallpaperTextColor(this);
        adapter.setTitleColor(listTitleColor);

        adapter.update(getAppsForLauncher(standardApps));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE_READ_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                adjustBg();
            }
        }
    }

    private final class LauncherApkaFilter implements Filter<Apka> {
        @Override
        public boolean match(Apka apka) {
            if (!apka.launcher) {
                return false;
            }

            int installationState = Utils.appInstalledOrNot(LauncherActivity.this, apka.packageId, apka.appVersion);
            return installationState != Utils.APP_NOT_INSTALLED;
        }
    }

    private boolean hasExternalStoragePermission() {
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        return (permissionCheck == PackageManager.PERMISSION_GRANTED);
    }

    private void requestExternalStoragePermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                REQUEST_CODE_READ_EXTERNAL_STORAGE);
    }

    @Override
    public void onBackPressed() {
        //Block back button
    }
}
