package corn.cardreader.utilities;

import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.util.Log;
import corn.cardreader.R;
//import corn.cardreader.utilities.cadastre.CadastreReaderService;
import org.jmrtd.PassportService;

import java.util.Arrays;

public class BaseReaderService {

    public static final int NO_ERROR = 0;
    public static int ERROR_CODE;

    protected static Tag getTag(Intent intent){
        if (!NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
            throw new IllegalArgumentException();
        }

        Tag tag = intent.getExtras().getParcelable(NfcAdapter.EXTRA_TAG);

        if (!Arrays.asList(tag.getTechList()).contains("android.nfc.tech.IsoDep")) {
            throw new IllegalArgumentException();
        }

        return tag;
    }


}
