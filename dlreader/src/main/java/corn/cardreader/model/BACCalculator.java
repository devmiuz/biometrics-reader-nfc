package corn.cardreader.model;

import corn.cardreader.model.crypto.AES;
import corn.cardreader.model.crypto.DesEncrypter;
import corn.cardreader.model.crypto.TripleDES;

import java.io.ByteArrayOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Random;

public class BACCalculator {
    private static boolean bacEstablished = false;

    private static byte[] kenc = null;

    private static byte[] kmac = null;

    private static byte[] ksenc = null;

    private static byte[] ksmac = null;

    private static byte[] ssc = new byte[8];

    private static byte[] rndicc = null;

    private static byte[] rndifd = null;

    private static byte[] kicc = null;

    private static byte[] kifd = null;


    /**
     * Constants that help in determining whether or not a byte array is parity
     * <p>
     * adjusted.
     */

    private static byte[] PARITY = {8, 1, 0, 8, 0, 8, 8, 0, 0, 8, 8, 0,

            8, 0, 2, 8, 0, 8, 8, 0, 8, 0, 0, 8, 8, 0, 0, 8, 0, 8, 8, 3, 0, 8,

            8, 0, 8, 0, 0, 8, 8, 0, 0, 8, 0, 8, 8, 0, 8, 0, 0, 8, 0, 8, 8, 0,

            0, 8, 8, 0, 8, 0, 0, 8, 0, 8, 8, 0, 8, 0, 0, 8, 8, 0, 0, 8, 0, 8,

            8, 0, 8, 0, 0, 8, 0, 8, 8, 0, 0, 8, 8, 0, 8, 0, 0, 8, 8, 0, 0, 8,

            0, 8, 8, 0, 0, 8, 8, 0, 8, 0, 0, 8, 0, 8, 8, 0, 8, 0, 0, 8, 8, 0,

            0, 8, 0, 8, 8, 0, 0, 8, 8, 0, 8, 0, 0, 8, 8, 0, 0, 8, 0, 8, 8, 0,

            8, 0, 0, 8, 0, 8, 8, 0, 0, 8, 8, 0, 8, 0, 0, 8, 8, 0, 0, 8, 0, 8,

            8, 0, 0, 8, 8, 0, 8, 0, 0, 8, 0, 8, 8, 0, 8, 0, 0, 8, 8, 0, 0, 8,

            0, 8, 8, 0, 8, 0, 0, 8, 0, 8, 8, 0, 0, 8, 8, 0, 8, 0, 0, 8, 0, 8,

            8, 0, 8, 0, 0, 8, 8, 0, 0, 8, 0, 8, 8, 0, 4, 8, 8, 0, 8, 0, 0, 8,

            8, 0, 0, 8, 0, 8, 8, 0, 8, 5, 0, 8, 0, 8, 8, 0, 0, 8, 8, 0, 8, 0,

            6, 8};


    public static byte[] CalcBAC_Res(byte[] _rndIcc, byte[] key) throws Exception {
        if (key == null) {
            key = "A290654395164273X970030110709303".getBytes("UTF-8");
        }

        rndicc = _rndIcc;

        kenc = calculateKENC(key);


        String RES_ENC = ByteArrayToString(kenc);

        kmac = calculateKMAC(key);

        String RES_MAC = ByteArrayToString(kmac);

        byte[] Mutual = getMutualAuthenticationCommand();


        return Mutual;
    }

    private static byte[] getMutualAuthenticationCommand() throws Exception {

        // 2. Generate an 8 byte random and a 16 byte random.

        rndifd = new byte[8];

        kifd = new byte[16];

        Random rand = new Random();

        rand.nextBytes(rndifd); // fill rndifd with random bytes


        rand.nextBytes(kifd); // fill kifd with random bytes


        // 3. Concatenate RND.IFD, RND.ICC and KIFD:

        byte[] s = new byte[32];

        System.arraycopy(rndifd, 0, s, 0, rndifd.length);

        System.arraycopy(rndicc, 0, s, 8, rndicc.length);

        System.arraycopy(kifd, 0, s, 16, kifd.length);

        String s_enc = ByteArrayToString(s);

        // 4. Encrypt S with TDES key Kenc:
        String K_enc = ByteArrayToString(kenc);

        byte[] eifd = encrypt3DES(kenc, s);

        String RES3 = ByteArrayToString(eifd);

        String K_mac = ByteArrayToString(kmac);

        // 5. Compute MAC over eifd with TDES key Kmac:
        byte[] mifd = getCC_MACNbytes(kmac, eifd);

        String Mifd = ByteArrayToString(mifd);


        // 6. Construct command data for MUTUAL AUTHENTICATE and send command
        // APDU to the MRTD's chip:
        byte[] mu_data = new byte[eifd.length + mifd.length];
        System.arraycopy(eifd, 0, mu_data, 0, eifd.length);
        System.arraycopy(mifd, 0, mu_data, eifd.length, mifd.length);

        String RES_Final = ByteArrayToString(mu_data);

        return mu_data;
    }

    private static byte[] getCC_MACNbytes(byte[] Key_MAC, byte[] eIFD) {
        byte[] Kmac = Key_MAC;


        // Split the 16 byte MAC key into two keys
        byte[] key1 = new byte[8];
        System.arraycopy(Kmac, 0, key1, 0, 8);

        byte[] key2 = new byte[8];
        System.arraycopy(Kmac, 8, key2, 0, 8);


        ByteArrayOutputStream os = new ByteArrayOutputStream();
        os.write(eIFD, 0, eIFD.length);
        os.write(0x80);
        while (os.size() % 8 != 0) {
            os.write(0x00);
        }

        byte[] eIfd_padded = os.toByteArray();
        int N_bytes = eIfd_padded.length / 8;

        byte[] d1 = new byte[8];
        byte[] dN = new byte[8];
        byte[] hN = new byte[8];
        byte[] intN = new byte[8];

        // MAC Algorithm 3
        // Initial Transformation 1
        System.arraycopy(eIfd_padded, 0, d1, 0, 8);
        hN = DesEncrypter.encrypt(d1, key1);

        // Split the blocks
        // Iteration on the rest of blocks
        for (int j = 1; j < N_bytes; j++) {
            System.arraycopy(eIfd_padded, (8 * j), dN, 0, 8);
            // XOR
            for (int i = 0; i < 8; i++)
                intN[i] = (byte) (hN[i] ^ dN[i]);

            // Encrypt
            hN = DesEncrypter.encrypt(intN, key1);
        }

        // Output Transformation 3
        byte[] hNdecrypt = DesEncrypter.encrypt(hN, key2);


        byte[] mIfd = DesEncrypter.encrypt(hNdecrypt, key1);

        //  Get check Sum CC
        return mIfd;
    }

    private static byte[] calculateSHA1(byte[] input) {
        try {
            MessageDigest msdDigest = MessageDigest.getInstance("SHA-1");
            msdDigest.update(input, 0, input.length);
            return msdDigest.digest();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return null;
    }

    private static byte[] calculateKSeed(byte[] mrzInfoBytes) {
        byte[] hash = calculateSHA1(mrzInfoBytes);

        byte[] kseed = new byte[16];

        for (int i = 0; i < 16; i++)

            kseed[i] = hash[i];

        return kseed;
    }

    private static void adjustParity(byte[] key, int offset) {
        for (int i = offset; i < 8; i++) {
            key[i] ^= (PARITY[key[i] & 0xff] == 8) ? (byte) 1 : (byte) 0;
        }
    }

    private static byte[] computeKey(byte[] kseed, byte[] c) {
        byte[] d = new byte[20];

        System.arraycopy(kseed, 0, d, 0, kseed.length);

        System.arraycopy(c, 0, d, 16, c.length);

        byte[] hd = calculateSHA1(d);

        byte[] ka = new byte[8];

        byte[] kb = new byte[8];

        System.arraycopy(hd, 0, ka, 0, ka.length);

        System.arraycopy(hd, 8, kb, 0, kb.length);

        // Adjust Parity-Bits

        //adjustParity(ka, 0);

        //adjustParity(kb, 0);

        byte[] key = new byte[16];

        System.arraycopy(ka, 0, key, 0, 8);

        System.arraycopy(kb, 0, key, 8, 8);

        //Array.Copy(ka, 0, key, 16, 8);

        return key;

    }

    private static byte[] encryptDES(byte[] key, byte[] data) throws Exception {
        if (key.length != 0x08 &&
                key.length != 0x10 &&
                key.length != 0x18)
            throw new Exception("Invalid key length");

        return DesEncrypter.encrypt(data, key);
    }

    private static byte[] encrypt3DES(byte[] key, byte[] data) throws Exception {
        if (key.length != 0x08 &&
                key.length != 0x10 &&
                key.length != 0x18)
            throw new Exception("Invalid key length");

        return TripleDES.encrypt(data, key);
    }

    private static String ByteArrayToString(byte[] ba) {
        String hex = bytesToHex(ba);
        return hex.replace("-", "");
    }

    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static byte[] calculateKMAC(byte[] mrzInfo) {

        //byte[] mrzinfobytes = Encoding.UTF8.GetBytes(mrzInfo);

        byte[] kseed = calculateKSeed(mrzInfo);

        return computeKey(kseed, new byte[]{0, 0, 0, 2});

    }

    /// <summary>
    ///
    /// </summary>
    /// <param name="mrzInfo"></param>
    /// <returns></returns>
    public static byte[] calculateKENC(byte[] mrzInfo) {

        //byte[] mrzInfoBytes = Encoding.UTF8.GetBytes(mrzInfo);

        byte[] kseed = calculateKSeed(mrzInfo);

        return computeKey(kseed, new byte[]{0, 0, 0, 1});

    }

    private static byte[] padByteArray(byte[] data) {

        int i = 0;
        byte[] tempdata = new byte[data.length + 8];

        for (i = 0; i < data.length; i++) {
            tempdata[i] = data[i];
        }

        tempdata[i] = (byte) 0x80;

        for (i = i + 1; ((i) % 8) != 0; i++) {
            tempdata[i] = (byte) 0;
        }

        byte[] filledArray = new byte[i];

        System.arraycopy(tempdata, 0, filledArray, 0, i);

        return filledArray;
    }

    private static byte[] removePadding(byte[] b) {
        byte[] rd = null;
        int i = b.length - 1;
        do {
            i--;
        } while (b[i] == (byte) 0x00);

        if (b[i] == (byte) 0x80) {
            rd = new byte[i];
            System.arraycopy(b, 0, rd, 0, rd.length);
            return rd;
        }
        return b;
    }

    private static byte[] exclusiveOR(byte[] key, byte[] PAN) {
        if (key.length == PAN.length) {
            byte[] result = new byte[key.length];
            for (int i = 0; i < key.length; i++) {
                result[i] = (byte) (key[i] ^ PAN[i]);
            }
            String hex = ByteArrayToString(result).replace("-", "");
            return result;
        } else {
            throw new IllegalArgumentException();
        }
    }

    private static byte[] xorArray(byte[] a, byte[] b) {
        if (b.length < a.length)
            throw new IllegalArgumentException(
                    "length of byte [] b must be >= byte [] a");

        byte[] c = new byte[a.length];

        for (int i = 0; i < a.length; i++) {
            c[i] = (byte) (a[i] ^ b[i]);
        }
        return c;
    }

    private static void calculateSessionKeys(byte[] kifd, byte[] kicc) {

        byte[] kseed = xorArray(kicc, kifd);

        // 18.Calculate Session Keys (KS_ENC and KS_MAC):

        ksenc = computeKey(kseed, new byte[]{0, 0, 0, 1});

        ksmac = computeKey(kseed, new byte[]{0, 0, 0, 2});

    }

    private static byte[] decrypt3DES(byte[] key, byte[] data) throws Exception {
        if (key.length != 0x08 &&
                key.length != 0x10 &&
                key.length != 0x18)
            throw new Exception("Invalid key length");


        return TripleDES.decrypt(data, key);
    }

    private static byte[] encryptAES(byte[] key, byte[] data) throws Exception {
        if (key.length != 0x08 &&
                key.length != 0x10 &&
                key.length != 0x18)
            throw new Exception("Invalid key length");

        return AES.encrypt(key, data);
    }

    private static byte[] Algorithm3(byte[] key, byte[] data) throws Exception {
        if (data.length % 8 != 0) {
            throw new IllegalArgumentException("Data must be padded to 8-byte blocks.");
        }

        int numBlocks = data.length / 8;

        byte[] iv = new byte[8];

        if (numBlocks > 1) {
            byte[] firstBlocks = Arrays.copyOfRange(data, 0, (numBlocks - 1) * 8 );
            byte[] encFirstBlocks = encryptDES(Arrays.copyOfRange(data, 0, 8), firstBlocks);

            System.arraycopy(encFirstBlocks, encFirstBlocks.length - 8, iv, 0, 8);
            //iv = encFirstBlocks.TakeLast(8).ToArray();
        } else {
            //iv = icv;
        }

        byte[] lastBlock = new byte[8];
        System.arraycopy(data, data.length - 8, lastBlock, 0, 8);


        byte[] encLastBlock = encrypt3DES(key, lastBlock);
        byte[] mac = new byte[8];
        System.arraycopy(encLastBlock, encLastBlock.length - 8, mac, 0, 8);

        return mac;
    }

    private static byte[] CalcDesMac(byte[] key, byte[] data) {
        byte[] encryption = DesEncrypter.encrypt(data, key);

        byte[] mac = new byte[8];

        System.arraycopy(encryption, encryption.length - 8, mac, 0, 8);

        //PrintByteArray(encryption);

        return mac;
    }
}
