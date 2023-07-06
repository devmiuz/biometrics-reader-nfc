package corn.cardreader.utilities.drivingLicense;

import android.graphics.Bitmap;
import corn.cardreader.model.dgFiles.DLicenseDG1File;
import corn.cardreader.model.dgFiles.DLicenseDG2File;
import corn.cardreader.utilities.ReaderDelegate;

public interface DLicenseReaderDelegate extends ReaderDelegate {

    void onFinish(DLicenseDG1File customDG1File, DLicenseDG2File customDG2File);
    void onUserImageRead(Bitmap userImageBitmap);
    void onSignImageRead(Bitmap signBitmap);
}
