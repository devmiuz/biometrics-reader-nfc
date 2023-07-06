package corn.cardreader.tech_card.util;

import android.util.Log;
import corn.cardreader.utilities.BitConverter;
import corn.cardreader.utilities.MRZUtil;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import net.sf.scuba.util.Hex;
//import net.sourceforge.scuba.util.Hex;

// TODO: 31/01/19 refactor the class
public class TechCardMRZUtil {

    private static final String TAG = TechCardMRZUtil.class.getName();

    private static final String _pinfl = "pinfl";
    private static final String issue_date = "issue_date";
    private static final String _ubdd_name = "ubdd_name";
    private static final String license_num = "license_num";
    private static final String reg_num = "reg_num";
    private static final String model_name = "model_name";
    private static final String color = "color";
    private static final String manufacture_year = "man_year";
    private static final String gross_weight = "gross_weight";
    private static final String curb_weight = "curb_weight";
    private static final String _engine_number = "engine_num";
    private static final String _engine_power = "engine_power";
    private static final String _fuel_type = "fuel_type";
    private static final String _number_of_seats = "num_seats";
    private static final String _number_of_standees = "num_standees";
    private static final String _special_marks = "special_marks";
    private static final String comp_name = "comp_name";
    private static final String comp_inn = "comp_inn";
    private static final String address = "address";
    private static final String region_name = "reg_name";
    private static final String district_name = "dist_name";
    private static final String expire_date = "expire_date";
    private static final String comp_type = "comp_type";
    private static final String last_name = "last_name";
    private static final String first_name = "first_name";
    private static final String middle_name = "middle_name";
    private static final String _mark_name = "mark_name";
    private static final String vehicle_type = "vehicle_type";
    private static final String _vehicle_identification_number_kuzov = "kuzov_ID";
    private static final String _vehicle_identification_number_shassi = "shassi_ID";
    private static final String _engine_measurement = "engine_measurement";


    public static Map<String, String> parseDG1(byte[] DG1) {
        HashMap<String, String> dict = ParseVR(DG1);

        Log.d(TAG, "Technical card of the car");
        for (String key : dict.keySet()) {
            Log.d(TAG, key + " - " + dict.get(key));
        }
        return dict;
    }


    public static String ConvertByte2MetaInfoCode(byte code) {
        String retbyte = "";

        switch (code) {
            case (byte) 0xA1:
                retbyte = issue_date;
                break;
            case (byte) 0xA2:
                retbyte = _ubdd_name;
                break;
            case (byte) 0xA3:
                retbyte = _pinfl;
                break;
            case (byte) 0xA4:
                retbyte = license_num;
                break;
            case (byte) 0xA5:
                retbyte = reg_num;
                break;
            case (byte) 0xA6:
                retbyte = model_name;
                break;
            case (byte) 0xA7:
                retbyte = color;
                break;
            case (byte) 0xA8:
                retbyte = manufacture_year;
                break;
            case (byte) 0xA9:
                retbyte = gross_weight;
                break;

            ////////////////////////////////////////////////////////
            case (byte) 0xB1:
                retbyte = curb_weight;
                break;
            case (byte) 0xB2:
                retbyte = _engine_number;
                break;
            case (byte) 0xB3:
                retbyte = _engine_power;
                break;
            case (byte) 0xB4:
                retbyte = _fuel_type;
                break;
            case (byte) 0xB5:
                retbyte = _number_of_seats;
                break;
            case (byte) 0xB6:
                retbyte = _number_of_standees;
                break;
            case (byte) 0xB7:
                retbyte = _special_marks;
                break;
            case (byte) 0xB8:
                retbyte = comp_name;
                break;
            case (byte) 0xB9:
                retbyte = comp_inn;
                break;

            ////////////////////////////////////////////////////////
            case (byte) 0xC1:
                retbyte = address;
                break;
            case (byte) 0xC2:
                retbyte = region_name;
                break;
            case (byte) 0xC3:
                retbyte = district_name;
                break;
            case (byte) 0xC4:
                retbyte = expire_date;
                break;
            case (byte) 0xC5:
                retbyte = comp_type;
                break;
            case (byte) 0xC6:
                retbyte = last_name;
                break;
            case (byte) 0xC7:
                retbyte = first_name;
                break;
            case (byte) 0xC8:
                retbyte = middle_name;
                break;
            case (byte) 0xC9:
                retbyte = _mark_name;
                break;

            ////////////////////////////////////////////////////////
            case (byte) 0xD1:
                retbyte = vehicle_type;
                break;
            case (byte) 0xD2:
                retbyte = _vehicle_identification_number_kuzov;
                break;
            case (byte) 0xD3:
                retbyte = _vehicle_identification_number_shassi;
                break;
            case (byte) 0xD4:
                retbyte = _engine_measurement;
                break;
            case (byte) 0xD5:
                retbyte = "D5";
                break;
            case (byte) 0xD6:
                retbyte = "D6";
                break;
            case (byte) 0xD7:
                retbyte = "D7";
                break;
            case (byte) 0xD8:
                retbyte = "D8";
                break;
            case (byte) 0xD9:
                retbyte = "D9";
                break;

            ////////////////////////////////////////////////////////
            case (byte) 0xE1:
                retbyte = "E1";
                break;
            case (byte) 0xE2:
                retbyte = "E2";
                break;
            case (byte) 0xE3:
                retbyte = "E3";
                break;
            case (byte) 0xE4:
                retbyte = "E4";
                break;
            case (byte) 0xE5:
                retbyte = "E5";
                break;
            case (byte) 0xE6:
                retbyte = "E6";
                break;
            case (byte) 0xE7:
                retbyte = "E7";
                break;
            case (byte) 0xE8:
                retbyte = "E8";
                break;
            case (byte) 0xE9:
                retbyte = "E9";
                break;

            ////////////////////////////////////////////////////////
            case (byte) 0xF1:
                retbyte = "F1";
                break;
            case (byte) 0xF2:
                retbyte = "F2";
                break;
            case (byte) 0xF3:
                retbyte = "F3";
                break;
            case (byte) 0xF4:
                retbyte = "F4";
                break;
            case (byte) 0xF5:
                retbyte = "F5";
                break;
            case (byte) 0xF6:
                retbyte = "F6";
                break;
            case (byte) 0xF7:
                retbyte = "F7";
                break;
            case (byte) 0xF8:
                retbyte = "F8";
                break;
            case (byte) 0xF9:
                retbyte = "F9";
                break;
        }

        return retbyte;
    }

    public static HashMap<String, String> ParseVR(byte[] Vr) {
        Log.d(TAG, Hex.bytesToHexString(Vr));

        if (Vr.length == 0)
            return null;

        HashMap<String, String> resDict = new HashMap<>();

        byte[] TrimmedVr = MRZUtil.decode(Vr);
        String ResultinStr = MRZUtil.ByteArrayToString(TrimmedVr);

        int StartReadNumber = 4;

        if ((TrimmedVr[1] & 0xFF) == 0x81) {
            StartReadNumber = 3;
        } else if ((TrimmedVr[1] & 0xFF) == 0x82) {
            StartReadNumber = 4;
        } else {
            StartReadNumber = 2;
        }

        boolean valueCountingBegin = false;
        boolean tagGetting = true;
        boolean lengthGetting = false;
        boolean valueCountingFinished = false;

        byte tag = 0x00;
        byte[] value = null;
        int length = 0;
        int endOfValue = 0;

        for (int j = 0; j < TrimmedVr.length - StartReadNumber; j++) {
            //Action
            if (tagGetting) {
                tag = TrimmedVr[j + StartReadNumber];
            } else if (lengthGetting) {
                if ((tag & 0xFF) == 0xB7) {
                    byte[] lenArray = new byte[4];

                    int kCounter = 1;
                    while (true) {
                        if (((TrimmedVr[j + StartReadNumber + kCounter] & 0xFF) == 0xC5)
                                && ((TrimmedVr[j + StartReadNumber + kCounter + 1] & 0xFF) == 0x01)) {
                            break;
                        }

                        kCounter++;
                    }

                    length = kCounter - 1;

                    if (length == 0) {
                        lengthGetting = false;
                        tagGetting = false;
                        valueCountingBegin = false;
                        valueCountingFinished = true;
                    }
                } else if ((tag & 0xFF) == 0xD2) {
                    byte[] lenArray = new byte[4];

                    int kCounter = 1;
                    while (true) {
                        if ((TrimmedVr[j + StartReadNumber + kCounter] & 0xFF) == 0xD3) {
                            break;
                        }

                        kCounter++;
                    }

                    length = kCounter - 1;

                    if (length == 0) {
                        lengthGetting = false;
                        tagGetting = false;
                        valueCountingBegin = false;
                        valueCountingFinished = true;
                    }
                } else {
                    byte[] lenArray = new byte[4];

                    lenArray[0] = TrimmedVr[j + StartReadNumber];

                    length = BitConverter.toInt32(lenArray, 0);

                    if (length == 0) {
                        lengthGetting = false;
                        tagGetting = false;
                        valueCountingBegin = false;
                        valueCountingFinished = true;
                    }
                }

            } else if (valueCountingBegin) {

                value = new byte[length];

                if (((j + StartReadNumber) + length) > TrimmedVr.length) {
                    length = TrimmedVr.length - (j + StartReadNumber);
                    System.arraycopy(TrimmedVr, j + StartReadNumber, value, 0, length);
                } else {
                    System.arraycopy(TrimmedVr, j + StartReadNumber, value, 0, length);
                }

                //SetData(tag, value);
                String tagStr = ConvertByte2MetaInfoCode(tag);

                String valueStr = new String(value, StandardCharsets.UTF_8);
                resDict.put(tagStr, valueStr);

                endOfValue = j + StartReadNumber + length - 1;

                if ((j + StartReadNumber) >= endOfValue) {
                    lengthGetting = false;
                    tagGetting = true;
                    valueCountingBegin = false;
                    valueCountingFinished = false;

                    tag = 0x00;
                    value = null;
                    length = 0;
                    endOfValue = 0;

                    continue;
                }

            } else if (valueCountingFinished) {
                if ((j + StartReadNumber) >= endOfValue) {
                    tag = 0x00;
                    value = null;
                    length = 0;
                    endOfValue = 0;
                } else {
                    continue;
                }
            }


            //Setting
            if (tagGetting) {
                lengthGetting = true;
                tagGetting = false;
                valueCountingBegin = false;
                valueCountingFinished = false;
            } else if (lengthGetting) {
                lengthGetting = false;
                tagGetting = false;
                valueCountingBegin = true;
                valueCountingFinished = false;
            } else if (valueCountingBegin) {
                lengthGetting = false;
                tagGetting = false;
                valueCountingBegin = false;
                valueCountingFinished = true;
            } else if (valueCountingFinished) {
                lengthGetting = false;
                tagGetting = true;
                valueCountingBegin = false;
                valueCountingFinished = false;
            }
        }

        return resDict;
    }

}