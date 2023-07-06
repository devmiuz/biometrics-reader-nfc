package corn.cardreader.tech_card.util;

import corn.cardreader.utilities.BaseLDS;
import java.util.Map;
import org.apache.commons.io.IOUtils;
import org.jmrtd.PassportService;

import java.io.IOException;
import java.io.InputStream;

public class TechCardLDS extends BaseLDS {

    private static final String TAG = TechCardLDS.class.getName();

    public Map<String, String> getCustomDG1File() throws IOException {
        byte[] response = getResponseBytes(PassportService.EF_DG1);
        return TechCardMRZUtil.parseDG1(response);
        //return TechCardMRZUtil.parseDG1(response);
    }

    public byte[] getResponseBytes(final short fileID) throws IOException {
        InputStream is = getInputStream(fileID);
        byte[] response = IOUtils.toByteArray(is);
        return response;
    }
}
