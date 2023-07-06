/*
 * Copyright (C) 2008 ZXing authors
 * Copyright 2011 Robert Theis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mercuriete.mrz.reader;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

//import com.journeyapps.barcodescanner.CaptureManager;
//import com.journeyapps.barcodescanner.DecoratedBarcodeView;

/**
 * This activity opens the camera and does the actual scanning on a background thread. It draws a
 * viewfinder to help the user place the text correctly, shows feedback as the image processing
 * is happening, and then overlays the results when a scan is successful.
 * <p>
 * The code for this class was adapted from the ZXing project: http://code.google.com/p/zxing/
 */
public final class CaptureActivity extends AppCompatActivity {

    public static final int CAPTURE_REQUEST_CODE = 101;

    public static void start(Activity context, int type) {
        Intent intent = new Intent(context, CaptureActivity.class);
        intent.putExtra("type", type);
        context.startActivityForResult(intent, type);
    }

    /**
     * ISO 639-3 language code indicating the default recognition language.
     */
    public static final String DEFAULT_SOURCE_LANGUAGE_CODE = "eng";

    /**
     * ISO 639-1 language code indicating the default target language for translation.
     */
    public static final String DEFAULT_TARGET_LANGUAGE_CODE = "eng";

    private static final int PERMISSIONS_REQUEST_CAMERA = 1;

    public static String KEY_TOGGLE_LIGHT = "KEY_TOGGLE_LIGHT";

    private boolean isToggleLight;

    private ImageView ivLight;
    private ImageView ivMrzMode;
    private ImageView ivQrMode;

    private boolean isMrzMode = true;

    private Runnable clickCameraModeRunnable = new Runnable() {
        @Override
        public void run() {
            ivMrzMode.setOnClickListener(mOnClickListener);
            ivQrMode.setOnClickListener(mOnClickListener);
        }
    };
    private OnClickListener mOnClickListener = new OnClickListener() {
        @Override
        public void onClick(final View v) {
            isMrzMode = !isMrzMode;
            selectCameraMode();
            ivMrzMode.setOnClickListener(null);
            ivQrMode.setOnClickListener(null);
            ivMrzMode.postDelayed(clickCameraModeRunnable, 1000);
            ivQrMode.postDelayed(clickCameraModeRunnable, 1000);
        }
    };

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        this.requestPermission();

        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_capture);

        ivLight = findViewById(R.id.ivLight);
        isToggleLight = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(KEY_TOGGLE_LIGHT, false);
//
        findViewById(R.id.ivBack).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                finish();
            }
        });
        findViewById(R.id.ivKeyboard).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                Intent data = new Intent();
                data.putExtra("manual_enter", true);
                setResult(Activity.RESULT_OK, data);
                finish();
            }
        });
//
        ivLight.setImageResource(isToggleLight ? R.drawable.vector_lighting_off : R.drawable.vector_lightning);
        ivLight.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                isToggleLight = !isToggleLight;
                ivLight.setImageResource(isToggleLight ? R.drawable.vector_lighting_off : R.drawable.vector_lightning);

                Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.flContainer);
                if (fragment instanceof QrCodeReadFragment)
                    ((QrCodeReadFragment) fragment).switchFlashlight(isToggleLight);
                else if (fragment instanceof CaptureFragment)
                    ((CaptureFragment) fragment).switchFlashlight(isToggleLight);
            }
        });

        // if the device does not have flashlight in its camera,
        // then remove the switch flashlight button...
        if (!hasFlash()) {
            ivLight.setVisibility(View.INVISIBLE);
        }

        ivMrzMode = findViewById(R.id.ivMrzMode);
        ivQrMode = findViewById(R.id.ivQrMode);
        ivMrzMode.setOnClickListener(mOnClickListener);
        ivQrMode.setOnClickListener(mOnClickListener);
        if ((isMrzMode = getIntent().getIntExtra("type", MrzData.PASSPORT) == MrzData.PASSPORT)) {
            findViewById(R.id.cvSwithMode).setVisibility(View.INVISIBLE);
        }
        selectCameraMode();
    }

    private void selectCameraMode() {
        Fragment captureFragment = getSupportFragmentManager().findFragmentByTag("mrzFragment");
        Fragment qrCodeReadFragment = getSupportFragmentManager().findFragmentByTag("qrCodeReadFragment");
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if (isMrzMode) {
            if (qrCodeReadFragment != null)
                transaction.hide(qrCodeReadFragment);
            if (captureFragment == null) {
                captureFragment = new CaptureFragment();
                transaction.replace(R.id.flContainer, captureFragment, "mrzFragment");
            } else {
                transaction.show(captureFragment);
            }

            ivMrzMode.setBackgroundResource(R.drawable.bg_scan_mode);
            ivQrMode.setBackgroundResource(0);
        } else {
            if (captureFragment != null)
                transaction.hide(captureFragment);
            if (qrCodeReadFragment == null) {
                qrCodeReadFragment = new QrCodeReadFragment();
                transaction.replace(R.id.flContainer, qrCodeReadFragment, "qrCodeReadFragment");
            } else {
                transaction.show(qrCodeReadFragment);
            }
            ivQrMode.setBackgroundResource(R.drawable.bg_scan_mode);
            ivMrzMode.setBackgroundResource(0);
        }
        transaction.commitAllowingStateLoss();
    }

    private void requestPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.CAMERA)) {
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA},
                        PERMISSIONS_REQUEST_CAMERA);
            }
        }
    }

    private boolean hasFlash() {
        return getApplicationContext().getPackageManager()
                .hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
            String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CAMERA: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permission granted", Toast.LENGTH_LONG).show();

                } else {
                    Toast.makeText(this, "Permission not granted", Toast.LENGTH_LONG).show();
                    setResult(RESULT_CANCELED);
                    finish();
                }
            }
        }
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, @Nullable final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }
}
