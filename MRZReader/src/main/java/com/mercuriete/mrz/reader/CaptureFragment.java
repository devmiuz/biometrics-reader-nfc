package com.mercuriete.mrz.reader;

import static android.app.Activity.RESULT_CANCELED;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.googlecode.tesseract.android.TessBaseAPI;
import com.mercuriete.mrz.reader.camera.CameraManager;
import java.io.File;
import java.io.IOException;
import java.util.Random;

public class CaptureFragment extends Fragment implements Callback {

    private static final String TAG = CaptureFragment.class.getSimpleName();

    /**
     * Languages for which Cube data is available.
     */
    static final String[] CUBE_SUPPORTED_LANGUAGES = {
            "ara", // Arabic
            "hin" // Hindi
    };

    static final public String MRZ_RESULT = "MRZ_RESULT";

    private static final int PERMISSIONS_REQUEST_CAMERA = 1;

    public static String KEY_OCR_ENGINE_MODE = "KEY_OCR_ENGINE_MODE";

    public static String KEY_CONTINUOUS_PREVIEW = "KEY_CONTINUOUS_PREVIEW";

    public static String KEY_TOGGLE_LIGHT = "KEY_TOGGLE_LIGHT";

    private CameraManager cameraManager;

    private CaptureActivityHandler handler;

    private SurfaceView surfaceView;

    private SurfaceHolder surfaceHolder;

    private boolean hasSurface;

    private TessBaseAPI baseApi; // Java interface for the Tesseract OCR engine

    private String sourceLanguageCodeOcr; // ISO 639-3 language code

    private String sourceLanguageReadable; // Language name, for example, "English"

    private int pageSegmentationMode = TessBaseAPI.PageSegMode.PSM_AUTO_OSD;

    private int ocrEngineMode = TessBaseAPI.OEM_TESSERACT_ONLY;

    private String characterBlacklist;

    private String characterWhitelist;

    private boolean isContinuousModeActive = true; // Whether we are doing OCR in continuous mode

    private SharedPreferences prefs;

    private OnSharedPreferenceChangeListener listener;

    private ProgressDialog dialog; // for initOcr - language download & unzip

    private ProgressDialog indeterminateDialog; // also for initOcr - init OCR engine

    private boolean isEngineReady;

    private boolean isToggleLight;

    Handler getHandler() {
        return handler;
    }

    TessBaseAPI getBaseApi() {
        return baseApi;
    }

    CameraManager getCameraManager() {
        return cameraManager;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable final ViewGroup container,
            @Nullable final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.capture, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        this.requestPermission();

        handler = null;
        hasSurface = false;

        cameraManager = new CameraManager(requireContext());

        isEngineReady = false;
        isToggleLight = PreferenceManager.getDefaultSharedPreferences(requireContext()).getBoolean(KEY_TOGGLE_LIGHT, false);
    }

    public void switchFlashlight(boolean torchOn) {
        this.isToggleLight = torchOn;
        if (cameraManager != null)
        cameraManager.setTorch(torchOn);
    }

    private void requestPermission() {
        if (ContextCompat.checkSelfPermission(requireActivity(),
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(),
                    Manifest.permission.CAMERA)) {
            } else {
                ActivityCompat.requestPermissions(requireActivity(),
                        new String[]{Manifest.permission.CAMERA},
                        PERMISSIONS_REQUEST_CAMERA);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
            String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CAMERA: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(requireActivity(), "Permission granted", Toast.LENGTH_LONG).show();

                } else {
                    Toast.makeText(requireActivity(), "Permission not granted", Toast.LENGTH_LONG).show();
                    requireActivity().setResult(RESULT_CANCELED);
                    requireActivity().finish();
                }
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        this.requestPermission();

        String previousSourceLanguageCodeOcr = sourceLanguageCodeOcr;
        int previousOcrEngineMode = ocrEngineMode;

        retrievePreferences();

        // Set up the camera preview surface.
        surfaceView = (SurfaceView) getView().findViewById(R.id.preview_view);
        surfaceHolder = surfaceView.getHolder();
        if (!hasSurface) {
            surfaceHolder.addCallback(this);
            surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }

        // Comment out the following block to test non-OCR functions without an SD card

        // Do OCR engine initialization, if necessary
        boolean doNewInit = (baseApi == null) || !sourceLanguageCodeOcr.equals(previousSourceLanguageCodeOcr) ||
                ocrEngineMode != previousOcrEngineMode;
        if (doNewInit) {
            // Initialize the OCR engine
            File storageDirectory = getStorageDirectory();
            if (storageDirectory != null) {
                initOcrEngine(storageDirectory, sourceLanguageCodeOcr, sourceLanguageReadable);
            }
        } else {
            resumeOCR();
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                switchFlashlight(isToggleLight);
            }
        }, 1000);
    }

    /**
     * Method to start or restart recognition after the OCR engine has been initialized,
     * or after the app regains focus. Sets state related settings and OCR engine parameters,
     * and requests camera initialization.
     */
    public void resumeOCR() {
        Log.d(TAG, "resumeOCR()");

        // This method is called when Tesseract has already been successfully initialized, so set
        // isEngineReady = true here.
        isEngineReady = true;

        if (handler != null) {
            handler.resetState();
        }
        if (baseApi != null) {
            baseApi.setPageSegMode(pageSegmentationMode);
            baseApi.setVariable(TessBaseAPI.VAR_CHAR_BLACKLIST, characterBlacklist);
            baseApi.setVariable(TessBaseAPI.VAR_CHAR_WHITELIST, characterWhitelist);
        }

        if (hasSurface) {
            // The activity was paused but not stopped, so the surface still exists. Therefore
            // surfaceCreated() won't be called, so init the camera here.
            initCamera(surfaceHolder);
        }
    }

    /**
     * Called to resume recognition after translation in continuous mode.
     */
    @SuppressWarnings("unused")
    void resumeContinuousDecoding() {
        DecodeHandler.resetDecodeState();
        handler.resetState();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
//        if (isMrzMode) {
        Log.d(TAG, "surfaceCreated()");

        if (holder == null) {
            Log.e(TAG, "surfaceCreated gave us a null surface");
        }

        // Only initialize the camera if the OCR engine is ready to go.
        if (!hasSurface && isEngineReady) {
            Log.d(TAG, "surfaceCreated(): calling initCamera()...");
            initCamera(holder);
        }
//        }
        hasSurface = true;
    }

    /**
     * Initializes the camera and starts the handler to begin previewing.
     */
    private void initCamera(SurfaceHolder surfaceHolder) {
        Log.d(TAG, "initCamera()");
        if (surfaceHolder == null) {
            throw new IllegalStateException("No SurfaceHolder provided");
        }
        try {

            // Open and initialize the camera
            cameraManager.openDriver(surfaceHolder);

            // Creating the handler starts the preview, which can also throw a RuntimeException.
            handler = new CaptureActivityHandler(this, cameraManager, isContinuousModeActive);

        } catch (IOException ioe) {
            showErrorMessage("Error", "Could not initialize camera. Please try restarting device.");
        } catch (RuntimeException e) {
            // Barcode Scanner has seen crashes in the wild of this variety:
            // java.?lang.?RuntimeException: Fail to connect to camera service
            showErrorMessage("Error", "Could not initialize camera. Please try restarting device.");
        }
    }

    @Override
    public void onPause() {
        if (handler != null) {
            handler.quitSynchronously();
        }

        // Stop using the camera, to avoid conflicting with other camera-based apps
        cameraManager.closeDriver();

        if (!hasSurface) {
            SurfaceView surfaceView = (SurfaceView) getView().findViewById(R.id.preview_view);
            SurfaceHolder surfaceHolder = surfaceView.getHolder();
            surfaceHolder.removeCallback(this);
        }
        super.onPause();
    }

    void stopHandler() {
        if (handler != null) {
            handler.stop();
        }
    }

    @Override
    public void onDestroy() {
        if (baseApi != null) {
            baseApi.end();
        }
        super.onDestroy();
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        hasSurface = false;
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    /**
     * Sets the necessary language code values for the given OCR language.
     */
    private boolean setSourceLanguage(String languageCode) {
        sourceLanguageCodeOcr = languageCode;
        return true;
    }

    /**
     * Sets the necessary language code values for the translation target language.
     */
    private boolean setTargetLanguage(String languageCode) {
        return true;
    }

    /**
     * Finds the proper location on the cache dir where we can save files.
     */
    private File getStorageDirectory() {
        return requireActivity().getCacheDir();
    }

    /**
     * Requests initialization of the OCR engine with the given parameters.
     *
     * @param storageRoot  Path to location of the tessdata directory to use
     * @param languageCode Three-letter ISO 639-3 language code for OCR
     * @param languageName Name of the language for OCR, for example, "English"
     */
    private void initOcrEngine(File storageRoot, String languageCode, String languageName) {
        isEngineReady = false;

        // Set up the dialog box for the thermometer-style download progress indicator
        if (dialog != null) {
            dialog.dismiss();
        }
        dialog = new ProgressDialog(requireContext());

        // If our language doesn't support Cube, then set the ocrEngineMode to Tesseract
        if (ocrEngineMode != TessBaseAPI.OEM_TESSERACT_ONLY) {
            boolean cubeOk = false;
            for (String s : CUBE_SUPPORTED_LANGUAGES) {
                if (s.equals(languageCode)) {
                    cubeOk = true;
                }
            }
            if (!cubeOk) {
                ocrEngineMode = TessBaseAPI.OEM_TESSERACT_ONLY;
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());
                prefs.edit().putString(KEY_OCR_ENGINE_MODE, getOcrEngineModeName()).commit();
            }
        }

        // Display the name of the OCR engine we're initializing in the indeterminate progress dialog box
        indeterminateDialog = new ProgressDialog(requireContext());
        indeterminateDialog.setTitle("Please wait");
        String ocrEngineModeName = getOcrEngineModeName();
        if (ocrEngineModeName.equals("Both")) {
            indeterminateDialog.setMessage("Initializing Cube and Tesseract OCR engines for " + languageName + "...");
        } else {
            indeterminateDialog
                    .setMessage("Initializing " + ocrEngineModeName + " OCR engine for " + languageName + "...");
        }
        indeterminateDialog.setCancelable(false);
        indeterminateDialog.show();

        if (handler != null) {
            handler.quitSynchronously();
        }

        // Disable continuous mode if we're using Cube. This will prevent bad states for devices
        // with low memory that crash when running OCR with Cube, and prevent unwanted delays.
        if (ocrEngineMode == TessBaseAPI.OEM_CUBE_ONLY || ocrEngineMode == TessBaseAPI.OEM_TESSERACT_CUBE_COMBINED) {
            Log.d(TAG, "Disabling continuous preview");
            isContinuousModeActive = true;
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());
            prefs.edit().putBoolean(KEY_CONTINUOUS_PREVIEW, true);
        }

        // Start AsyncTask to install language data and init OCR
        baseApi = new TessBaseAPI();
        new OcrInitAsyncTask(this, baseApi, dialog, indeterminateDialog,
                languageCode, languageName, ocrEngineMode)
                .execute(storageRoot.toString());
    }

    /**
     * Displays information relating to the result of OCR, and requests a translation if necessary.
     *
     * @param ocrResult Object representing successful OCR results
     * @return True if a non-null result was received for OCR
     */
    boolean handleOcrDecode(OcrResult ocrResult) {
        // Test whether the result is null
        if (ocrResult.getText() == null || ocrResult.getText().equals("")) {
            Toast toast = Toast.makeText(requireContext(), "OCR failed. Please try again.", Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.TOP, 0, 0);
            toast.show();
            return false;
        }
        // Turn off capture-related UI elements
        return true;
    }

    /**
     * Displays information relating to the results of a successful real-time OCR request.
     *
     * @param ocrResult Object representing successful OCR results
     */
    synchronized void  handleOcrContinuousDecode(OcrResult ocrResult) {
        String result = ocrResult.getText();
        if (result != null && !"".equals(result)) {
            result.replaceAll(" ", "");
            String[] textResultTmpArr = result.split("\n");
            result = "";
            for (int i = 0; i < textResultTmpArr.length; i++) {
                if (textResultTmpArr[i].length() > 10) {
                    result += textResultTmpArr[i] + '\n';
                }
            }
            result = result.replaceAll(" ", "");
            ocrResult.setText(result);

            isValidMRZ(result);
        }
    }

    private synchronized boolean isValidMRZ(String data) {
        try {
            MrzData mrzData = new MrzData(data);
            Intent intent = new Intent();
            intent.putExtra("data", mrzData);
            requireActivity().setResult(Activity.RESULT_OK, intent);
            requireActivity().finish();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }

    /**
     * Returns a string that represents which OCR engine(s) are currently set to be run.
     *
     * @return OCR engine mode
     */
    String getOcrEngineModeName() {
        String ocrEngineModeName = "";
        String[] ocrEngineModes = getResources().getStringArray(R.array.ocrenginemodes);
        if (ocrEngineMode == TessBaseAPI.OEM_TESSERACT_ONLY) {
            ocrEngineModeName = ocrEngineModes[0];
        } else if (ocrEngineMode == TessBaseAPI.OEM_CUBE_ONLY) {
            ocrEngineModeName = ocrEngineModes[1];
        } else if (ocrEngineMode == TessBaseAPI.OEM_TESSERACT_CUBE_COMBINED) {
            ocrEngineModeName = ocrEngineModes[2];
        }
        return ocrEngineModeName;
    }

    /**
     * Gets values from shared preferences and sets the corresponding data members in this activity.
     */
    private void retrievePreferences() {
        prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());

        // Retrieve from preferences, and set in this Activity, the language preferences
        PreferenceManager.setDefaultValues(requireContext(), R.xml.preferences, false);
        setSourceLanguage(CaptureActivity.DEFAULT_SOURCE_LANGUAGE_CODE);
        setTargetLanguage(CaptureActivity.DEFAULT_TARGET_LANGUAGE_CODE);

        isContinuousModeActive = true;

        // Retrieve from preferences, and set in this Activity, the page segmentation mode preference
        String[] pageSegmentationModes = getResources().getStringArray(R.array.pagesegmentationmodes);
        String pageSegmentationModeName = pageSegmentationModes[0];
        if (pageSegmentationModeName.equals(pageSegmentationModes[0])) {
            pageSegmentationMode = TessBaseAPI.PageSegMode.PSM_AUTO_OSD;
        }

        // Retrieve from preferences, and set in this Activity, the OCR engine mode
        String[] ocrEngineModes = getResources().getStringArray(R.array.ocrenginemodes);
        String ocrEngineModeName = prefs.getString(KEY_OCR_ENGINE_MODE, ocrEngineModes[0]);
        if (ocrEngineModeName.equals(ocrEngineModes[0])) {
            ocrEngineMode = TessBaseAPI.OEM_TESSERACT_ONLY;
        } else if (ocrEngineModeName.equals(ocrEngineModes[1])) {
            ocrEngineMode = TessBaseAPI.OEM_CUBE_ONLY;
        } else if (ocrEngineModeName.equals(ocrEngineModes[2])) {
            ocrEngineMode = TessBaseAPI.OEM_TESSERACT_CUBE_COMBINED;
        }

        // Retrieve from preferences, and set in this Activity, the character blacklist and whitelist
        characterBlacklist = OcrCharacterHelper.getBlacklist(prefs, sourceLanguageCodeOcr);
        characterWhitelist = OcrCharacterHelper.getWhitelist(prefs, sourceLanguageCodeOcr);

        prefs.registerOnSharedPreferenceChangeListener(listener);
    }

    void displayProgressDialog() {
        // Set up the indeterminate progress dialog box
        indeterminateDialog = new ProgressDialog(requireContext());
        indeterminateDialog.setTitle("Please wait");
        String ocrEngineModeName = getOcrEngineModeName();
        if (ocrEngineModeName.equals("Both")) {
            indeterminateDialog.setMessage("Performing OCR using Cube and Tesseract...");
        } else {
            indeterminateDialog.setMessage("Performing OCR using " + ocrEngineModeName + "...");
        }
        indeterminateDialog.setCancelable(false);
        indeterminateDialog.show();
    }

    /**
     * Displays an error message dialog box to the user on the UI thread.
     *
     * @param title   The title for the dialog box
     * @param message The error message to be displayed
     */
    void showErrorMessage(String title, String message) {
        new AlertDialog.Builder(requireContext())
                .setTitle(title)
                .setMessage(message)
                .setOnCancelListener(new FinishListener(requireActivity()))
                .setPositiveButton("Done", new FinishListener(requireActivity()))
                .show();
    }

    ProgressDialog getProgressDialog() {
        return indeterminateDialog;
    }

}
