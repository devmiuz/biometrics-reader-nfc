package corn.cardreader.utilities.drivingLicense;

import corn.cardreader.utilities.BaseService;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
//import net.sourceforge.scuba.smartcards.CardService;
//import net.sourceforge.scuba.smartcards.CardServiceException;
import net.sf.scuba.smartcards.CardService;
import net.sf.scuba.smartcards.CardServiceException;
import org.jmrtd.BACKeySpec;
import org.jmrtd.lds.MRZInfo;

public class DrivingLicenseService extends BaseService {

    private static final String TAG = DrivingLicenseService.class.getName();

    public DrivingLicenseService(CardService service) throws CardServiceException {
        super(service);
    }

    public void doBAC(BACKeySpec bacKeySpec) throws CardServiceException {
        String mrzInfo = String.format("%s%s%s%s%s%s",
                bacKeySpec.getDocumentNumber(),
                MRZInfo.checkDigit(bacKeySpec.getDocumentNumber()),
                bacKeySpec.getDateOfBirth(),
                MRZInfo.checkDigit(bacKeySpec.getDateOfBirth()),
                bacKeySpec.getDateOfExpiry(),
                MRZInfo.checkDigit(bacKeySpec.getDateOfExpiry()));

        String srt = String.format("1%s", mrzInfo).substring(0, 16);
        byte[] bytes = srt.getBytes(StandardCharsets.US_ASCII);

        try {
            doBAC(bytes);
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }
    }
}
