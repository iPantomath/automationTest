package com.dhl.demp.mydmac.dialog;

import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.Glide;
import com.dhl.demp.mydmac.LauncherPreference;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import mydmac.R;

public class AddingShortcutDialog extends DialogFragment{

    private EditText appNameEditText, appURLEditText;

    private Button createShortcutButton, cancelShortcutButton;
    private ImageView shortcutIcon, resetIcon;
    private LinearLayout appNameLayout, appUrlLayout;

    String appShortcutName, appShortcutURL, editAppName, editAppUrl, tempName, tempURL;
    String currentIcon = "";
    Integer position;
    String iconPath = "default";
    int editType = 0;   // NewApp = 1 || Edit = 2 || ChangeIcon = 3

    Boolean newAppIcon = false;
    private static final int PICK_ICON = 2;
    private static final int REQUEST_CODE_READ_EXTERNAL_STORAGE = 1;
    private static String TAG = AddingShortcutDialog.class.getCanonicalName();
    Bitmap bmp = null;

    ArrayList<String> appIconList = new ArrayList<>();


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.adding_shortcut_dialog, container, false);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        appNameEditText = view.findViewById(R.id.shortcut_name_edittext);
        appURLEditText =  view.findViewById(R.id.shortcut_url_edittext);
        createShortcutButton = view.findViewById(R.id.create_shortcut);
        cancelShortcutButton = view.findViewById(R.id.cancel_shortcut);
        shortcutIcon = view.findViewById(R.id.shortcut_icon);
        resetIcon = view.findViewById(R.id.reset_icon);
        appNameLayout = view.findViewById(R.id.appNameLinearLayout);
        appUrlLayout = view.findViewById(R.id.appUrlLinearLayout);

        shortcutIcon.setImageResource(R.drawable.shortcut_app_icon);

        createShortcutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Boolean appNameExist = false;
                Boolean appUrlExist = false;
                appShortcutName = appNameEditText.getText().toString();
                appShortcutURL = appURLEditText.getText().toString();
                if(position!=null) {
                    appNameExist = (!LauncherPreference.getShortcutAppNameList(getActivity()).get(position).equals(appShortcutName)
                            && LauncherPreference.getShortcutAppNameList(getActivity()).contains(appShortcutName));
                    appUrlExist = (!LauncherPreference.getShortcutAppURLList(getActivity()).get(position).equals(appShortcutURL)
                            && LauncherPreference.getShortcutAppNameList(getActivity()).contains(appShortcutURL));
                }else{
                    appNameExist = LauncherPreference.getShortcutAppNameList(getActivity()).contains(appShortcutName);
                    appUrlExist = LauncherPreference.getShortcutAppNameList(getActivity()).contains(appShortcutURL);
                }
                if( (appShortcutName.isEmpty() && appShortcutURL.isEmpty() && !LauncherPreference.getIsChangeIcon(getActivity())) ) {
                    Toast.makeText(getActivity(), "Please fill in Application Name and URL!", Toast.LENGTH_SHORT).show();
                }else if (appNameExist || appUrlExist) {
                    Toast.makeText(getActivity(), "Shortcut exist!", Toast.LENGTH_SHORT).show();
                }else{
                    if(createShortcutButton.getText().equals("Save")/* && (!appNameExist && !appUrlExist)*/){
                            UserInputListener userInputListener = (UserInputListener) getActivity();
                            userInputListener.sendShortcutValue(appShortcutName, appShortcutURL, iconPath, editType);
                            LauncherPreference.setEditApp(getActivity(), false);
                            LauncherPreference.setIsChangeIcon(getActivity(), false);
                            deleteIconExtStorage();
                            createShortcutButton.setText("Create");
                            position = null;
                    }else{
                        UserInputListener userInputListener = (UserInputListener) getActivity();
                        userInputListener.sendShortcutValue(appShortcutName, appShortcutURL, iconPath, editType);
                    }
                    position=null;
                    newAppIcon=false;
                     iconPath="default";
                    dismiss();
                }
            }
        });

        cancelShortcutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                position=null;
                LauncherPreference.setEditApp(getActivity(), false);
                LauncherPreference.setIsChangeIcon(getActivity(), false);
                dismiss();
            }
        });

        shortcutIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                onSelectIcon();
            }
        });

        resetIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                iconPath = "default";
                setIcon();
            }
        });
    }

    @Override
    public void dismiss() {
        super.dismiss();
        editType=0;
        newAppIcon=false;
        iconPath="default";
    }

    @Override
    public void onCancel(@NonNull DialogInterface dialog) {
        super.onCancel(dialog);
        editType=0;
        newAppIcon=false;
        iconPath="default";
    }

    @Override
    public void onResume() {
        super.onResume();
        setEditTextValues();
    }

    public void setEditTextValues(){

        if(LauncherPreference.getEditApp(getContext())) {
            try {
                Bundle bundle = getArguments();
                editAppName = bundle.getString("editAppName", "");
                editAppUrl = bundle.getString("editAppUrl", "");

                position=bundle.getInt("position", 0);

                appNameEditText.setText(editAppName);
                appURLEditText.setText(editAppUrl);
                createShortcutButton.setText("Save");
                if(newAppIcon==false) {
                    iconPath = LauncherPreference.getIconPath(getActivity()).get(position);
                    LauncherPreference.setCurrentIcon(getActivity(), iconPath);
                }
                setIcon();
                editType = 2;


            } catch (Exception ex) {
                Log.i(TAG, "User field is empty");
            }
        }else if(LauncherPreference.getIsChangeIcon(getActivity())) {
            Bundle bundle = getArguments();
            appNameLayout.setVisibility(View.INVISIBLE);
            appUrlLayout.setVisibility(View.GONE);
            createShortcutButton.setText("Save");
            try {
                position = bundle.getInt("position", 0);
            }catch (Exception ex){
            }
            if(!newAppIcon) {
                iconPath = LauncherPreference.getIconPath(getActivity()).get(position);
                LauncherPreference.setCurrentIcon(getActivity(), iconPath);
           }
            setIcon();
            editType = 3;
        }else{
            if(editType==1){
                appNameEditText.setText(LauncherPreference.getTempName(getActivity()));
                appURLEditText.setText(LauncherPreference.getTempUrl(getActivity()));
            }else {
                appNameEditText.setVisibility(View.VISIBLE);
                appURLEditText.setVisibility(View.VISIBLE);
                appNameEditText.setText("");
                appURLEditText.setText("");
                editType = 1;
            }
        }


    }

    public void onSelectIcon(){
        if(hasExternalStoragePermission()) {
            if(editType==1){
                LauncherPreference.setTempName(getActivity(), appNameEditText.getText().toString());
                LauncherPreference.setTempUrl(getActivity(), appURLEditText.getText().toString());
            }
            Intent intent = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            getActivity().startActivityForResult(intent, PICK_ICON);
        }
        else {
            requestExternalStoragePermission();
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {

            if (requestCode == PICK_ICON) {

                File dstFile;
                Uri selectedImage = data.getData();
                try {
                    InputStream inputStream = getActivity().getContentResolver().openInputStream(selectedImage);
                    File file = new File(getActivity().getExternalFilesDir("ShortcutIcon").toURI());
                    if (!file.exists()) {
                        file.mkdirs();
                    }
                    File[] fileList = file.listFiles();
                    String fileName;
                    String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
                    if(fileList.length != 0){
                        if(editType==1) {
                            fileName = fileList.length + 1 +"_"+timestamp;
                        }else{
                            fileName = position +"_"+timestamp;
                        }
                        dstFile = new File(file, fileName);
                    }else{
                        fileName = 0 +"_"+timestamp;
                        dstFile = new File(file, fileName);
                    }

                    OutputStream outputStream = new FileOutputStream(dstFile);
                    //Toast.makeText(getActivity(), dstFile.toString(), Toast.LENGTH_SHORT).show();


                    byte[] buffer = new byte[1024];
                    int length;
                    while((length = inputStream.read(buffer)) > 0){
                        outputStream.write(buffer, 0, length);
                    }
                    inputStream.close();
                    outputStream.close();
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                iconPath = dstFile.toString();

                newAppIcon=true;
                setIcon();
            }
        }
    }


    private boolean hasExternalStoragePermission() {
        int permissionCheck = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE);
        return (permissionCheck == PackageManager.PERMISSION_GRANTED);
    }

    private void requestExternalStoragePermission() {
        requestPermissions(
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                2000);
    }


    private void setIcon() {

        if (hasExternalStoragePermission()) {
            if (iconPath.equals("default")) {
                shortcutIcon.setImageResource(R.drawable.shortcut_app_icon);
            }else {
                Glide.with(getActivity())
                        .load(iconPath)
                        .into(shortcutIcon);
            }
        } else {
            requestExternalStoragePermission();
        }
    }

    private void deleteIconExtStorage(){
        File iconToDelete = new File(LauncherPreference.getCurrentIcon(getActivity()));
        if (iconToDelete.exists()) {
            iconToDelete.delete();
        }
    }

    public interface UserInputListener{

        void sendShortcutValue(String appName, String appUrl, String iconPath, int editType);
    }
}
