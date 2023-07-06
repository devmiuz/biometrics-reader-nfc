package corn.cardreader.utilities.drivingLicense;

import android.graphics.Bitmap;
import android.nfc.tech.IsoDep;
import android.os.AsyncTask;
import android.util.Log;
import corn.cardreader.R;
import corn.cardreader.model.dgFiles.DLicenseDG1File;
import corn.cardreader.model.dgFiles.DLicenseDG2File;
import corn.cardreader.utilities.BaseReaderService;
import java.io.IOException;
import net.sf.scuba.smartcards.CardFileInputStream;
import net.sf.scuba.smartcards.CardService;
import net.sf.scuba.smartcards.CardServiceException;
//import net.sourceforge.scuba.smartcards.CardFileInputStream;
//import net.sourceforge.scuba.smartcards.CardService;
//import net.sourceforge.scuba.smartcards.CardServiceException;
import org.jmrtd.BACKeySpec;
import org.jmrtd.PassportService;

public class DrivingLicenseReaderService extends BaseReaderService {

    private static final String TAG = DrivingLicenseReaderService.class.getName();

    private static DLicenseReaderDelegate readerDelegate;

    public static void parseIntent(IsoDep isoDep, BACKeySpec bacKeySpec, DLicenseReaderDelegate readerDelegate) {
        DrivingLicenseReaderService.readerDelegate = readerDelegate;
        ERROR_CODE = NO_ERROR;

        try{
//            Tag tag = getTag(intent);
            new ReadTask(isoDep, bacKeySpec).execute();
        }catch (IllegalArgumentException e){
            readerDelegate.onError(R.string.error_nfc_msg);
        }
    }

    private static class ReadTask extends AsyncTask<Void, Void, Exception> {

        private IsoDep isoDep;
        private BACKeySpec bacKey;

        private DLicenseDG1File customDG1File;
        private DLicenseDG2File customDG2File;
        private Bitmap userImage;
        private Bitmap signImage;

        public ReadTask(IsoDep isoDep, BACKeySpec bacKey) {
            this.isoDep = isoDep;
            this.bacKey = bacKey;
        }

        @Override
        protected Exception doInBackground(Void... params) {
            CardService cardService = CardService.getInstance(isoDep);
            try {
                cardService.open();
                DrivingLicenseService drivingLicenseService = new DrivingLicenseService(cardService);
                drivingLicenseService.open();

                drivingLicenseService.sendSelectApplet();

                try {
                    drivingLicenseService.doBAC(bacKey);
                }catch (CardServiceException e){
                    ERROR_CODE = R.string.auth_nfc_msg;
                    e.printStackTrace();
                    return null;
                }

                DrivingLDS lds = new DrivingLDS();

                Log.d(TAG, "getting DG1 file");
                CardFileInputStream dg1In = drivingLicenseService.getInputStream(PassportService.SF_DG1);
                lds.add(PassportService.EF_DG1, dg1In, dg1In.getLength());
                customDG1File = lds.getCustomDG1File();

                Log.d(TAG, "getting DG2 file");
                CardFileInputStream dg2In = drivingLicenseService.getInputStream(PassportService.SF_DG2);
                lds.add(PassportService.EF_DG2, dg2In, dg2In.getLength());
                customDG2File = lds.getCustomDG2File();

                Log.d(TAG, "getting DG4 file");
                CardFileInputStream dg4In = drivingLicenseService.getInputStream(PassportService.SF_DG4);
                lds.add(PassportService.EF_DG4, dg4In, dg4In.getLength());
                userImage = lds.getCustomDG4File();

                Log.d(TAG, "getting DG5 file");
                CardFileInputStream dg5In = drivingLicenseService.getInputStream(PassportService.SF_DG5);
                lds.add(PassportService.EF_DG5, dg5In, dg5In.getLength());
                signImage = lds.getCustomDG5File();

                dg1In.close();
                dg2In.close();
                dg4In.close();
                dg5In.close();

                drivingLicenseService.close();
                cardService.close();

            } catch (CardServiceException| IOException e) {
                e.printStackTrace();
                ERROR_CODE = R.string.error_nfc_msg;
            }

            return null;
        }

        @Override
        protected void onPostExecute(Exception result) {
            if(ERROR_CODE == NO_ERROR){
                readerDelegate.onUserImageRead(userImage);
                readerDelegate.onSignImageRead(signImage);
                readerDelegate.onFinish(customDG1File, customDG2File);
            }else{
                readerDelegate.onError(ERROR_CODE);
            }
        }
    }
}
