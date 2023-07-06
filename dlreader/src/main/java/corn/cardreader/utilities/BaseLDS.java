package corn.cardreader.utilities;

import org.apache.commons.io.IOUtils;
import org.jmrtd.lds.LDS;

import java.io.IOException;
import java.io.InputStream;

public class BaseLDS extends LDS {

    public byte[] getResponseBytes(final short fileID) throws IOException {
        InputStream is = getInputStream(fileID);
        byte[] response = IOUtils.toByteArray(is);
        is.close();
        return response;
    }
}
