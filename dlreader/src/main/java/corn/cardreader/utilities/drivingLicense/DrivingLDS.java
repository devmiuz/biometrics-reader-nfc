package corn.cardreader.utilities.drivingLicense;

import android.graphics.Bitmap;
import android.util.Log;
import corn.cardreader.model.dgFiles.DLicenseDG1File;
import corn.cardreader.model.dgFiles.DLicenseDG2File;
import corn.cardreader.utilities.BaseLDS;
import org.apache.commons.io.IOUtils;
import org.jmrtd.PassportService;
import org.jmrtd.lds.*;

import java.io.IOException;
import java.io.InputStream;

public class DrivingLDS extends BaseLDS {

    private static final String TAG = DrivingLDS.class.getName();

    public DLicenseDG1File getCustomDG1File() throws IOException {
        byte[] response = getResponseBytes(PassportService.EF_DG1);
        return DrivingLicenseMRZUtil.parseDG1(response);
    }

    public DLicenseDG2File getCustomDG2File() throws IOException {
        byte[] response = getResponseBytes(PassportService.EF_DG2);
        return DrivingLicenseMRZUtil.parseDG2(response);
    }

    public Bitmap getCustomDG4File() throws IOException {
        byte[] response = getResponseBytes(PassportService.EF_DG4);
        Log.d(TAG, "DG4 response length = " + response.length);
        return DrivingLicenseMRZUtil.parseDG4(response);
    }

    public Bitmap getCustomDG5File() throws IOException {
        byte[] response = getResponseBytes(PassportService.EF_DG5);
        Log.d(TAG, "DG5 response length = " + response.length);
        return DrivingLicenseMRZUtil.parseDG5(response);
    }
}
