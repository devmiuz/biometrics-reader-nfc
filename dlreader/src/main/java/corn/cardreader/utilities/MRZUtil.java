package corn.cardreader.utilities;

//import net.sourceforge.scuba.util.Hex;

import net.sf.scuba.util.Hex;

public class MRZUtil {

    public static byte[] decode(byte[] packet) {
        int i = packet.length - 1;
        while (packet[i] == 0) {
            --i;
        }

        byte[] temp = new byte[i + 1];
        System.arraycopy(packet, 0, temp, 0, i + 1);
        return temp;
    }

    /// <summary>
    ///
    /// </summary>
    /// <param name="ba"></param>
    /// <returns></returns>
    public static String ByteArrayToString(byte[] ba)
    {
        String hex = Hex.bytesToHexString(ba);
        return hex.replace("-", "");
    }
}
