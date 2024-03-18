package com.dhl.demp.mydmac.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;


import java.io.IOException;

import mydmac.R;

/**
 * Created by petlebed on 7/28/2017.
 */

public class ScanCodeActivity extends AppCompatActivity {
    public final static String SCAN1 = "scan1";
    public final static String SCAN2 = "scan2";
    final static int SCANNEDCODE_REQ = 1313;


    TextView tvscanval1,tvscanval2;
    SurfaceView mCameraView;
    private int scanPos = 0;
    Button mSend;

    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_scan_code);

            tvscanval1 = (TextView)findViewById(R.id.tvscanval1);
            tvscanval2 = (TextView)findViewById(R.id.tvscanval2);
            mCameraView = (SurfaceView)findViewById(R.id.mCameraView);

            Button mClose = (Button)findViewById(R.id.mClose);
            mClose.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onBackPressed();
                }
            });

            Button mReset = (Button)findViewById(R.id.mReset);
            mReset.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    reset();
                }
            });

            mSend = (Button)findViewById(R.id.mSend);
            mSend.setVisibility(View.GONE);
        mSend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    send();
                }
            });

        BarcodeDetector barcodeDetector = new BarcodeDetector.Builder(this).setBarcodeFormats(Barcode.ALL_FORMATS).build();
        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {

            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                final SparseArray<Barcode> barcodes = detections.getDetectedItems();
                if(barcodes.size()>0){
                    if(tvscanval1.getText().toString().equals(barcodes.valueAt(0).displayValue))
                        return;
                    if(tvscanval2.getText().toString().equals(barcodes.valueAt(0).displayValue))
                        return;


                    if(scanPos==0){
                        tvscanval1.post(new Runnable() {
                            @Override
                            public void run() {
                                tvscanval1.setText(barcodes.valueAt(0).displayValue);
                                scanPos = 1;
                            }
                        });
                    }else if(scanPos==1){
                        scanPos = 2;
                        tvscanval2.post(new Runnable() {
                            @Override
                            public void run() {
                                tvscanval2.setText(barcodes.valueAt(0).displayValue);
                                mSend.setVisibility(View.VISIBLE);
                            }
                        });
                    }
                }

//                if(barcodes.size()>1){
//                    code_info.post(new Runnable() {
//                        @Override
//                        public void run() {
//                            StringBuilder str = new StringBuilder();
//                            str.append(barcodes.valueAt(0).displayValue);
//                            str.append(",");
//                            str.append(barcodes.valueAt(1).displayValue);
//                            code_info.setText(str, TextView.BufferType.EDITABLE);
//                            if (code_info.getText().toString().length()>0) {
//                                Intent i = new Intent();
//                                i.putExtra(SCANNEDCODE, code_info.getText().toString());
//                                setResult(Activity.RESULT_OK, i);
//                                finish();
//                            }
//                        }
//                    });
//                }
            }
        });

        final CameraSource cameraSource = new CameraSource.Builder(this, barcodeDetector).setRequestedPreviewSize(640, 360).setAutoFocusEnabled(true).build();
        mCameraView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {
                try {
                    cameraSource.start(mCameraView.getHolder());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                 catch (SecurityException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
                cameraSource.stop();
            }
        });

    }

        private void send() {
            Intent i = new Intent();
            i.putExtra(SCAN1,tvscanval1.getText());
            i.putExtra(SCAN2,tvscanval2.getText());
            setResult(Activity.RESULT_OK,i);
            finish();
        }

    private void reset() {
        mSend.setVisibility(View.GONE);
        tvscanval1.setText("");
        tvscanval2.setText("");
        scanPos = 0;
    }

}
