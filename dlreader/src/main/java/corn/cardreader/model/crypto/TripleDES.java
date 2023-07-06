package corn.cardreader.model.crypto;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class TripleDES {

    public static byte[] encrypt(byte data[], byte[] keyBytes) throws Exception {
        final SecretKey key = new SecretKeySpec(keyBytes, "DESede");
        byte[] IV = new byte[]{0, 0, 0, 0, 0, 0, 0, 0};
        final IvParameterSpec iv = new IvParameterSpec(IV);
        final Cipher cipher = Cipher.getInstance("DESede/CBC/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, key, iv);

        final byte[] cipherText = cipher.doFinal(data);

        return cipherText;
    }

    public static byte[] decrypt(byte[] data, byte[] keyBytes) throws Exception {
        final SecretKey key = new SecretKeySpec(keyBytes, "DESede");
        final IvParameterSpec iv = new IvParameterSpec(new byte[8]);
        final Cipher decipher = Cipher.getInstance("DESede/CBC/NoPadding");
        decipher.init(Cipher.DECRYPT_MODE, key, iv);

        // final byte[] encData = new
        // sun.misc.BASE64Decoder().decodeBuffer(message);
        final byte[] plainText = decipher.doFinal(data);

        return plainText;
    }
}
