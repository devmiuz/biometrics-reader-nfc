package corn.cardreader.tech_card.util;

import corn.cardreader.utilities.BaseService;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import net.sf.scuba.smartcards.CardService;
import net.sf.scuba.smartcards.CardServiceException;
//import net.sourceforge.scuba.smartcards.CardService;
//import net.sourceforge.scuba.smartcards.CardServiceException;

public class TechCardService extends BaseService {

    private static final String TAG = TechCardService.class.getName();

    public TechCardService(CardService service) throws CardServiceException {
        super(service);
    }

    public void doBAC(TechCardBACKey bacKeySpec) throws CardServiceException {
        String mrzInfo = String.format("1%s%s%s",
                bacKeySpec.getRegNumber(), bacKeySpec.getIssueDate(), bacKeySpec.getLicenseNum());

        String srt = mrzInfo.substring(0, 16);
        byte[] bytes = srt.getBytes(StandardCharsets.US_ASCII);

        try {
            doBAC(bytes);
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }
    }

}
