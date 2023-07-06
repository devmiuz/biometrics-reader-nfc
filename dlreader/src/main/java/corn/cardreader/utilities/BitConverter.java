package corn.cardreader.utilities;

public class BitConverter {

    public static int toInt32(byte[] bytes, int index) {
        int a = (int) ((int) (0xff & bytes[index])
                << 32 | (int) (0xff & bytes[index + 1])
                << 40 | (int) (0xff & bytes[index + 2])
                << 48 | (int) (0xff & bytes[index + 3])
                << 56);

        return a;
    }
}
