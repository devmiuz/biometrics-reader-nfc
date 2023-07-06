package com.mercuriete.mrz.reader;

import static com.mercuriete.mrz.reader.CaptureActivity.KEY_TOGGLE_LIGHT;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.zxing.ResultPoint;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.CaptureManager;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;
import com.journeyapps.barcodescanner.ViewfinderView;
import com.journeyapps.barcodescanner.camera.CameraSettings;
import java.util.List;
import java.util.Random;

public class QrCodeReadFragment extends Fragment {

    private CaptureManager capture;
    private DecoratedBarcodeView barcodeScannerView;

    private boolean torchOn;

    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable final ViewGroup container,
            @Nullable final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_qr, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        barcodeScannerView = view.findViewById(R.id.zxing_barcode_scanner);
        capture = new CaptureManager(getActivity(), barcodeScannerView);
        capture.initializeFromIntent(getActivity().getIntent(), savedInstanceState);
        barcodeScannerView.decodeSingle(new BarcodeCallback() {
            @Override
            public void barcodeResult(final BarcodeResult result) {
                try {
                    MrzData mrzData = new MrzData(result.getText());
                    Intent data = new Intent();
                    data.putExtra("data", mrzData);
                    getActivity().setResult(Activity.RESULT_OK, data);
                    getActivity().finish();
                } catch (Exception e) {
                    Log.d("ssss", "" + e);
                }
            }

            @Override
            public void possibleResultPoints(final List<ResultPoint> resultPoints) {
                Log.d("ssss", "" + resultPoints);
            }
        });
        torchOn = PreferenceManager
                .getDefaultSharedPreferences(requireContext()).getBoolean(KEY_TOGGLE_LIGHT, false);
        switchFlashlight(torchOn);
    }

    public void switchFlashlight(boolean torchOn) {
        this.torchOn = torchOn;
        if (torchOn) {
            barcodeScannerView.setTorchOn();
        } else {
            barcodeScannerView.setTorchOff();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        capture.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        capture.onPause();
    }

    @Override
    public void onSaveInstanceState(@NonNull final Bundle outState) {
        super.onSaveInstanceState(outState);
        capture.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        capture.onDestroy();
    }
}
