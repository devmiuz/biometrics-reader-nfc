package corn.cardreader.model.crypto;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.spec.AlgorithmParameterSpec;

public class DesEncrypter {

    public static byte[] encrypt(byte[] data, byte[] keyByte) {
        try {
            SecretKey key;
            Cipher ecipher;
            // generate secret key using DES algorithm
            key = new SecretKeySpec(keyByte, "DES");
            byte[] iv = {0, 0, 0, 0, 0 ,0, 0, 0};

            ecipher = Cipher.getInstance("DES/CBC/NoPadding");

            AlgorithmParameterSpec paramSpec = new IvParameterSpec(iv);
            // initialize the ciphers with the given key
            ecipher.init(Cipher.ENCRYPT_MODE, key, paramSpec);

            // storing the result into a new byte array.
            byte[] utf8 = data;

            byte[] enc = ecipher.doFinal(utf8);

            return enc;

        } catch (Exception e) {

            e.printStackTrace();

        }

        return null;

    }


}