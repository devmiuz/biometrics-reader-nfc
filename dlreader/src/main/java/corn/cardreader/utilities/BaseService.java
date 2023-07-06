package corn.cardreader.utilities;

import android.util.Log;
import corn.cardreader.model.BACCalculator;
import net.sf.scuba.smartcards.APDUEvent;
import net.sf.scuba.smartcards.APDUListener;
import net.sf.scuba.smartcards.CardService;
import net.sf.scuba.smartcards.CardServiceException;
import net.sf.scuba.util.Hex;
//import net.sourceforge.scuba.smartcards.APDUEvent;
//import net.sourceforge.scuba.smartcards.APDUListener;
//import net.sourceforge.scuba.smartcards.CardService;
//import net.sourceforge.scuba.smartcards.CardServiceException;
//import net.sourceforge.scuba.util.Hex;
import org.jmrtd.BACKeySpec;
import org.jmrtd.PassportService;
import org.jmrtd.lds.MRZInfo;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;

public class BaseService extends PassportService {

    private static final byte[] APPLET_AID = {(byte) 0xA0, 0x00, 0x00, 0x02, 0x48, 0x02, 0x00};

    public BaseService(CardService service) throws CardServiceException {
        super(service);
    }

    public synchronized void sendSelectApplet() throws CardServiceException {
        super.sendSelectApplet(null, APPLET_AID);
    }

    protected void doBAC(byte[] bytes) throws CardServiceException, GeneralSecurityException {
        byte[] kEnc = BACCalculator.calculateKENC(bytes);
        byte[] kMac = BACCalculator.calculateKMAC(bytes);

        SecretKey kEncSK = new SecretKeySpec(kEnc, "DESEDE");
        SecretKey kMacSK = new SecretKeySpec(kMac, "DESEDE");

        doBAC(kEncSK, kMacSK);
    }

    public void attachListener() {
        addPlainTextAPDUListener(new APDUListener() {
            @Override
            public void exchangedAPDU(APDUEvent event) {
                logEvent(event);
            }
        });
        addAPDUListener(new APDUListener() {
            @Override
            public void exchangedAPDU(APDUEvent event) {
                logEvent(event);
            }
        });
    }

    public static void logEvent(APDUEvent event) {
        Log.d("NFC CHAT: Command", Hex.bytesToPrettyString(event.getCommandAPDU().getBytes()));
        Log.d("NFC CHAT: Response", Hex.bytesToPrettyString(event.getResponseAPDU().getBytes()));
    }
}
