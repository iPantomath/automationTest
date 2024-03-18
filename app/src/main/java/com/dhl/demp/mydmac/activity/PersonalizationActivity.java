package com.dhl.demp.mydmac.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.dhl.demp.mydmac.LauncherPreference;
import com.dhl.demp.mydmac.adapter.AppsShortcutAdapter;
import com.dhl.demp.mydmac.db.InstallableApp;
import com.dhl.demp.mydmac.dialog.AddingShortcutDialog;
import com.dhl.demp.mydmac.ui.colorpicker.ColorPickerDialog;
import com.dhl.demp.mydmac.ui.colorpicker.ColorPickerSwatch;
import com.dhl.demp.mydmac.utils.Constants;
import com.dhl.demp.mydmac.utils.FileUtils;
import com.dhl.demp.mydmac.utils.Utils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.microsoft.identity.common.java.cache.IListTypeToken;

import static com.dhl.demp.mydmac.utils.Constants.StandardAppFlag.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import mydmac.R;

/**
 * Created by frogggias on 28.05.15.
 */
public class PersonalizationActivity extends AppCompatActivity implements AddingShortcutDialog.UserInputListener, AppsShortcutAdapter.AdapterInterface {
    private static final String TAG = PersonalizationActivity.class.getSimpleName();
    private static final int REQUEST_CODE_READ_EXTERNAL_STORAGE = 1;
    private static final int PICK_IMAGE = 1;
    private static final int PICK_ICON = 2;

    private EditText bgTextEditor;
    private TextView wallpaperPath;
    private View deleteWallpaper;
    private View colorPreview;
    private CheckBox phoneApp;
    private CheckBox smsApp;
    private CheckBox cameraApp;
    private CheckBox mapsApp;
    private CheckBox chromeApp;
    private CheckBox scanToConnectApp;
    private CheckBox stageNowApp;
    private CheckBox mobiControlApp;
    private SwitchCompat launcherSwitcher;

    private Button addShortcut;

    private TextView noShortcutExist;
    File wallpaperDst;

    ArrayList<String> appNameList = new ArrayList<>();
    ArrayList<String> appUrlList = new ArrayList<>();
    ArrayList<String> appIconList = new ArrayList<>();
    ListView appShortcutListview;

    AppsShortcutAdapter adapter;
    DialogFragment dialogFragment= new AddingShortcutDialog();

    Integer getPosition;


    public static void start(Context context) {
        Intent starter = new Intent(context, PersonalizationActivity.class);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);

        onLoadShortcutList();
        initView();
    }

    private void initView() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_back);
        toolbar.setBackgroundColor(getColor(R.color.build_type_color));
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        boolean isLauncherEnabled = Utils.isLauncherEnabled(this);
        launcherSwitcher = (SwitchCompat) findViewById(R.id.launcher_switcher);
        if (getResources().getBoolean(R.bool.is_launcher_switchable) && !isLauncherEnabled) {
            //restore correct state
            launcherSwitcher.setChecked(false);
            launcherSwitcher.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    Utils.setLauncherEnabled(PersonalizationActivity.this, isChecked);

                    if (isChecked) {
                        launcherSwitcher.setVisibility(View.GONE);
                    }
                }
            });
        } else {
            launcherSwitcher.setVisibility(View.GONE);
        }

        bgTextEditor = findViewById(R.id.bg_text_editor);
        wallpaperPath = findViewById(R.id.wallpaper_path);
        deleteWallpaper = findViewById(R.id.delete_wallpaper);
        colorPreview = findViewById(R.id.color_preview);

        phoneApp = findViewById(R.id.phone_app);
        smsApp = findViewById(R.id.sms_app);
        cameraApp = findViewById(R.id.camera_app);
        mapsApp = findViewById(R.id.maps_app);
        chromeApp = findViewById(R.id.chrome_app);
        scanToConnectApp = findViewById(R.id.scan_to_connect_app);
        setAppCheckboxVisibility(scanToConnectApp, Constants.AppPackageName.SCAN_TO_CONNECT);
        stageNowApp = findViewById(R.id.stagenow_app);
        setAppCheckboxVisibility(stageNowApp, Constants.AppPackageName.STAGE_NOW);
        mobiControlApp = findViewById(R.id.mobicontrol_app);
        setAppCheckboxVisibility(mobiControlApp, Constants.AppPackageName.MOBICONTROL);

        bgTextEditor.setText(LauncherPreference.getDescription(this));
        setWallpaperPath(LauncherPreference.getWallpaper(this));
        setWallpaperTextColor(LauncherPreference.getWallpaperTextColor(this));

        int standardApps = LauncherPreference.getStandardApps(this);
        phoneApp.setChecked((standardApps & FLAG_PHONE) == FLAG_PHONE);
        smsApp.setChecked((standardApps & FLAG_SMS) == FLAG_SMS);
        cameraApp.setChecked((standardApps & FLAG_CAMERA) == FLAG_CAMERA);
        mapsApp.setChecked((standardApps & FLAG_MAP) == FLAG_MAP);
        chromeApp.setChecked((standardApps & FLAG_CHROME) == FLAG_CHROME);
        scanToConnectApp.setChecked((standardApps & FLAG_SCAN_TO_CONNECT) == FLAG_SCAN_TO_CONNECT);
        stageNowApp.setChecked((standardApps & FLAG_STAGE_NOW) == FLAG_STAGE_NOW);
        mobiControlApp.setChecked((standardApps & FLAG_MOBICONTROL) == FLAG_MOBICONTROL);

        deleteWallpaper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setWallpaperPath("");
            }
        });
        findViewById(R.id.wallpaper_select).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSelectWallpaper();
            }
        });
        findViewById(R.id.save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSave();
            }
        });
        findViewById(R.id.color_selector).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectColor();
            }
        });



        addShortcut = findViewById(R.id.add_shortcut);
        appShortcutListview = findViewById(R.id.shortcut_list);
        noShortcutExist = findViewById(R.id.no_shortcut_exist);

        addShortcut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                dialogFragment.show(getSupportFragmentManager(),"ShortcutFragment");
                LauncherPreference.setEditApp(getApplicationContext(), false);
                LauncherPreference.setIsChangeIcon(getApplicationContext(), false);
            }
        });

        if(!appNameList.isEmpty() || !appUrlList.isEmpty()) {
            adapter = new AppsShortcutAdapter(this, appNameList, appUrlList,appIconList);
            appShortcutListview.setAdapter(adapter);

            noShortcutExist.setVisibility(View.GONE);

        }else{
            noShortcutExist.setVisibility(View.VISIBLE);
            appShortcutListview.setVisibility(View.GONE);
        }

    }

    private void setAppCheckboxVisibility(CheckBox cb, String appPackageName) {
        cb.setVisibility(Utils.isAppInstalled(this, appPackageName) ? View.VISIBLE : View.INVISIBLE);
    }

    private void selectColor() {
        int selectedColor = bgTextEditor.getCurrentTextColor();
        int[] colors = getColors();

        ColorPickerDialog colorPickerDialog = new ColorPickerDialog();
        colorPickerDialog.initialize(R.string.configuration_text_color, colors, selectedColor, 5, colors.length);

        colorPickerDialog.setOnColorSelectedListener(new ColorPickerSwatch.OnColorSelectedListener() {
            @Override
            public void onColorSelected(int color) {
                setWallpaperTextColor(color);
            }
        });

        colorPickerDialog.show(getFragmentManager(), null);
    }

    private void onSelectWallpaper() {
        if (hasExternalStoragePermission()) {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
        } else {
            requestExternalStoragePermission();
        }
    }

    private void onSave() {
        if(wallpaperPath.getText() == "" && LauncherPreference.getWallpaper(this) != null){
            deleteWallpaperExtStorage();
        }
        LauncherPreference.setDescription(this, bgTextEditor.getText().toString());
        LauncherPreference.setWallpaper(this, wallpaperPath.getText().toString());
        LauncherPreference.setWallpaperTextColor(this, bgTextEditor.getCurrentTextColor());

        int standardApps = 0;
        if (phoneApp.isChecked()) {
            standardApps |= FLAG_PHONE;
        }
        if (smsApp.isChecked()) {
            standardApps |= FLAG_SMS;
        }
        if (cameraApp.isChecked()) {
            standardApps |= FLAG_CAMERA;
        }
        if (mapsApp.isChecked()) {
            standardApps |= FLAG_MAP;
        }
        if (chromeApp.isChecked()) {
            standardApps |= FLAG_CHROME;
        }
        if (scanToConnectApp.isChecked()) {
            standardApps |= FLAG_SCAN_TO_CONNECT;
        }
        if (stageNowApp.isChecked()) {
            standardApps |= FLAG_STAGE_NOW;
        }
        if (mobiControlApp.isChecked()) {
            standardApps |= FLAG_MOBICONTROL;
        }
        LauncherPreference.setStandardApps(this, standardApps);

        finish();
    }

    protected void setWallpaperPath(String path) {
        wallpaperPath.setText(path);

        deleteWallpaper.setVisibility(TextUtils.isEmpty(path) ? View.INVISIBLE : View.VISIBLE);
    }

    protected void setWallpaperTextColor(int color) {
        colorPreview.setBackgroundColor(color);
        bgTextEditor.setTextColor(color);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == PICK_IMAGE) {

            setWallpaper(data);
            String path = wallpaperDst.toString();
            setWallpaperPath(path);
        }

        dialogFragment.onActivityResult(requestCode, resultCode, data);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_READ_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                onSelectWallpaper();
            }
        }
    }

    private final int[] getColors() {
        return new int[]{getResources().getColor(R.color.text_color_red),
                getResources().getColor(R.color.text_color_pink),
                getResources().getColor(R.color.text_color_purple),
                getResources().getColor(R.color.text_color_deep_purple),
                getResources().getColor(R.color.text_color_indigo),
                getResources().getColor(R.color.text_color_blue),
                getResources().getColor(R.color.text_color_light_blue),
                getResources().getColor(R.color.text_color_cyan),
                getResources().getColor(R.color.text_color_teal),
                getResources().getColor(R.color.text_color_green),
                getResources().getColor(R.color.text_color_light_green),
                getResources().getColor(R.color.text_color_lime),
                getResources().getColor(R.color.text_color_yellow),
                getResources().getColor(R.color.text_color_amber),
                getResources().getColor(R.color.text_color_orange),
                getResources().getColor(R.color.text_color_deep_orange),
                getResources().getColor(R.color.text_color_brown),
                getResources().getColor(R.color.text_color_dark_grey),
                getResources().getColor(R.color.text_color_grey),
                getResources().getColor(R.color.text_color_blue_grey)};
    }

    private boolean hasExternalStoragePermission() {
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        return (permissionCheck == PackageManager.PERMISSION_GRANTED);
    }

    private void requestExternalStoragePermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                REQUEST_CODE_READ_EXTERNAL_STORAGE);
    }

    private void setWallpaper(Intent data){
        Uri selectedImage = data.getData();
        try{
            InputStream inputStream = getContentResolver().openInputStream(selectedImage);
            File file = new File(getExternalFilesDir("Wallpaper").toURI());
            if (!file.exists()) {
                file.mkdirs();
            }

            String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
            wallpaperDst = new File(file, "launcherWallpaper_"+timestamp);
            OutputStream outputStream = new FileOutputStream(wallpaperDst);
            byte[] buffer = new byte[1024];
            int length;
            while((length = inputStream.read(buffer)) > 0){
                outputStream.write(buffer, 0, length);
            }
            inputStream.close();
            outputStream.close();

        }catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void deleteWallpaperExtStorage(){
        File deleteWallpaper = new File(LauncherPreference.getWallpaper(this));
        if (deleteWallpaper.exists()) {
            deleteWallpaper.delete();
        }
    }

    @Override
    public void sendShortcutValue(String appShortcutName, String appShortcutURL, String iconPath, int editType) {
        appNameList = LauncherPreference.getShortcutAppNameList(this);
        appUrlList = LauncherPreference.getShortcutAppURLList(this);
        appIconList = LauncherPreference.getIconPath(this);
        //differentiate the values from edit values function and change icon function by
        // passing different integers instead of  boolean
            if(editType==2){
             appNameList.set(getPosition, appShortcutName);
             appUrlList.set(getPosition, appShortcutURL);
             appIconList.set(getPosition, iconPath);
             onSaveShortcutList();
             adapter = new AppsShortcutAdapter(this, appNameList, appUrlList, appIconList);
             appShortcutListview.setAdapter(adapter);
            }else if(editType==3) {
                appIconList.set(getPosition, iconPath);
                LauncherPreference.setIconPath(this, appIconList);//onSaveShortcutList();
                adapter = new AppsShortcutAdapter(this, appNameList, appUrlList, appIconList);
                appShortcutListview.setAdapter(adapter);
            }else{
                    appNameList.add(appShortcutName);
                    appUrlList.add(appShortcutURL);
                    appIconList.add(iconPath);
                    onSaveShortcutList();
                    adapter = new AppsShortcutAdapter(this, appNameList, appUrlList, appIconList);
                    appShortcutListview.setAdapter(adapter);
            }
            appShortcutListview.setVisibility(View.VISIBLE);
            noShortcutExist.setVisibility(View.GONE);
    }

    public void onSaveShortcutList(){
        LauncherPreference.setAppNameList(this, appNameList);
        LauncherPreference.setAppURLList(this, appUrlList);
        LauncherPreference.setIconPath(this, appIconList);
    }

    public void onLoadShortcutList(){

            appNameList = LauncherPreference.getShortcutAppNameList(this);
            appUrlList = LauncherPreference.getShortcutAppURLList(this);
            appIconList = LauncherPreference.getIconPath(this);
    }

    @Override
    public void sendEditData(String editAppName, String editAppUrl, int position) {
        String editName = editAppName;
        String editUrl = editAppUrl;
        getPosition = position;

        Bundle bundle = new Bundle();

        if(LauncherPreference.getEditApp(getApplicationContext())){
            bundle.putString("editAppName", editAppName);
            bundle.putString("editAppUrl", editAppUrl);
            bundle.putInt("position", position);
            dialogFragment.setArguments(bundle);
            dialogFragment.show(getSupportFragmentManager(),"ShortcutFragment");
        }else if(LauncherPreference.getIsChangeIcon(getApplicationContext())){
            bundle.putInt("position", position);
            dialogFragment.setArguments(bundle);
            dialogFragment.show(getSupportFragmentManager(),"ShortcutFragment");
        }

    }

    @Override
    public void listIsEmpty() {
        if(LauncherPreference.getShortcutAppNameList(getApplicationContext()).size()==0){
            appShortcutListview.setVisibility(View.GONE);
            noShortcutExist.setVisibility(View.VISIBLE);
        }

    }
}
