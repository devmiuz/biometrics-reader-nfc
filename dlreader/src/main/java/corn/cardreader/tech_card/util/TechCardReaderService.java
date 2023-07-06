package corn.cardreader.tech_card.util;

import android.content.Intent;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.os.AsyncTask;
import android.util.Log;
import corn.cardreader.R;
import corn.cardreader.utilities.BaseReaderService;
import java.util.Map;
import net.sf.scuba.smartcards.CardFileInputStream;
import net.sf.scuba.smartcards.CardService;
import net.sf.scuba.smartcards.CardServiceException;
//import net.sourceforge.scuba.smartcards.CardFileInputStream;
//import net.sourceforge.scuba.smartcards.CardService;
//import net.sourceforge.scuba.smartcards.CardServiceException;
import org.jmrtd.BACKeySpec;
import org.jmrtd.PassportService;

import java.io.IOException;

public class TechCardReaderService extends BaseReaderService {

    private static final String TAG = TechCardReaderService.class.getName();

    private static TechCardReaderDelegate readerDelegate;

    public static void parseIntent(IsoDep isoDep, TechCardBACKey bacKeySpec, TechCardReaderDelegate readerDelegate) {
        TechCardReaderService.readerDelegate = readerDelegate;
        ERROR_CODE = NO_ERROR;

        try {
            new ReadTask(isoDep, bacKeySpec).execute();
        } catch (IllegalArgumentException e) {
            readerDelegate.onError(R.string.error_nfc_msg);
        }
    }

    private static class ReadTask extends AsyncTask<Void, Void, Exception> {

        private IsoDep isoDep;
        private TechCardBACKey bacKey;
        private Map<String, String> data;

        public ReadTask(IsoDep isoDep, TechCardBACKey bacKey) {
            this.isoDep = isoDep;
            this.bacKey = bacKey;
        }

        @Override
        protected Exception doInBackground(Void... params) {
            CardService cardService = CardService.getInstance(isoDep);
            try {
                cardService.open();
                TechCardService drivingLicenseService = new TechCardService(cardService);
                drivingLicenseService.open();

                drivingLicenseService.sendSelectApplet();

                try {
                    drivingLicenseService.doBAC(bacKey);
                } catch (CardServiceException e) {
                    ERROR_CODE = R.string.auth_nfc_msg;
                    e.printStackTrace();
                    return null;
                }

                TechCardLDS lds = new TechCardLDS();

                Log.d(TAG, "getting DG1 file");
                CardFileInputStream dg1In = drivingLicenseService.getInputStream(PassportService.SF_DG1);
                lds.add(PassportService.EF_DG1, dg1In, dg1In.getLength());
                data = lds.getCustomDG1File();

                dg1In.close();
                drivingLicenseService.close();
                cardService.close();

            } catch (CardServiceException | IOException e) {
                e.printStackTrace();
                ERROR_CODE = R.string.error_nfc_msg;
            }

            return null;
        }

        @Override
        protected void onPostExecute(Exception result) {
            if (ERROR_CODE == NO_ERROR) {
                readerDelegate.onFinish(data);
            } else {
                readerDelegate.onError(ERROR_CODE);
            }
        }
    }
}
