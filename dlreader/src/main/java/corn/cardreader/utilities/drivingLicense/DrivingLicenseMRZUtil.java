package corn.cardreader.utilities.drivingLicense;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import corn.cardreader.model.dgFiles.DLicenseDG1File;
import corn.cardreader.model.LicenseCategory;
import corn.cardreader.model.dgFiles.DLicenseDG2File;
import corn.cardreader.utilities.BitConverter;
import corn.cardreader.utilities.MRZUtil;

import java.nio.charset.StandardCharsets;
import net.sf.scuba.util.Hex;
//import net.sourceforge.scuba.util.Hex;

public class DrivingLicenseMRZUtil {

    private static final String TAG = DrivingLicenseMRZUtil.class.getName();

    public static DLicenseDG1File parseDG1(byte[] DG1) {
        String Hex2Str = Hex.bytesToHexString(DG1);
        Log.d(TAG, "DG1 = " + Hex2Str);

        int n = 0, m = 0, k = 0;

        if (Hex2Str.substring(2, 4).equals("81")) {
            n = 6;
            m = 7;
            k = 8;
        } else if (Hex2Str.substring(2, 4).equals("82")) {
            n = 7;
            m = 8;
            k = 9;
        } else {
            n = 5;
            m = 6;
            k = 7;
        }

        byte[] demografLenth = new byte[4];
        demografLenth[0] = DG1[n]; //6 in Correct mode
        int idemografLenth = BitConverter.toInt32(demografLenth, 0);

        byte[] NameLenth = new byte[4];
        NameLenth[0] = DG1[m]; //7 in Correct mode
        int iNameLenth = BitConverter.toInt32(NameLenth, 0);
        byte[] Name = new byte[iNameLenth];
        System.arraycopy(DG1, k, Name, 0, iNameLenth); // k in correct mode
        String resultLastName = new String(Name, StandardCharsets.UTF_8);
        String lastName = resultLastName;

        byte[] FullNameLength = new byte[4];
        FullNameLength[0] = DG1[k + iNameLenth];
        int iFullLenth = BitConverter.toInt32(FullNameLength, 0);
        byte[] FullName = new byte[iFullLenth];
        System.arraycopy(DG1, k + iNameLenth + 1, FullName, 0, iFullLenth);
        String resultFullName = new String(FullName, StandardCharsets.UTF_8);
        Log.d(TAG, resultFullName);
        String _first_name = "";
        String _middle_name;
        String splitted[] = resultFullName.split("_");
        _first_name = splitted[1];
        if (splitted.length >= 3) {
            _middle_name = splitted[2];
        }

        int currectPos = k + iNameLenth + 1 + iFullLenth;
        byte[] dateOfBirth = new byte[4];
        dateOfBirth[0] = DG1[currectPos + 0];
        dateOfBirth[1] = DG1[currectPos + 1];
        dateOfBirth[2] = DG1[currectPos + 2];
        dateOfBirth[3] = DG1[currectPos + 3];
        String dateOfBirthStr = Hex.bytesToHexString(dateOfBirth);


        byte[] dateOfIssue = new byte[4];
        dateOfIssue[0] = DG1[currectPos + 4];
        dateOfIssue[1] = DG1[currectPos + 5];
        dateOfIssue[2] = DG1[currectPos + 6];
        dateOfIssue[3] = DG1[currectPos + 7];
        String dateOfIssueStr = Hex.bytesToHexString(dateOfIssue);

        byte[] dateOfExpire = new byte[4];
        dateOfExpire[0] = DG1[currectPos + 8];
        dateOfExpire[1] = DG1[currectPos + 9];
        dateOfExpire[2] = DG1[currectPos + 10];
        dateOfExpire[3] = DG1[currectPos + 11];
        String dateOfExpireStr = Hex.bytesToHexString(dateOfExpire);

        currectPos += 11;

        byte[] Country = new byte[3];
        Country[0] = DG1[currectPos + 1];
        Country[1] = DG1[currectPos + 2];
        Country[2] = DG1[currectPos + 3];
        String CountryName = new String(Country, StandardCharsets.UTF_8);

        //IA get Data
        byte[] IALenth = new byte[4];
        IALenth[0] = DG1[currectPos + 4];
        int IALenthLenthL = BitConverter.toInt32(IALenth, 0);
        currectPos += 4;
        byte[] IAFullName = new byte[IALenthLenthL];
        System.arraycopy(DG1, currectPos + 1, IAFullName, 0, IALenthLenthL);
        String IAFullName1 = new String(IAFullName, StandardCharsets.UTF_8);


        //License Number Get
        currectPos += IALenthLenthL;
        byte[] LicenseNumberLenth = new byte[4];
        LicenseNumberLenth[0] = DG1[currectPos + 1];
        int LicenseNumLL = BitConverter.toInt32(LicenseNumberLenth, 0);
        byte[] LicenseNumName = new byte[LicenseNumLL];
        System.arraycopy(DG1, currectPos + 2, LicenseNumName, 0, LicenseNumLL);
        String LicenseN = new String(LicenseNumName, StandardCharsets.UTF_8);

        currectPos += 2;
        currectPos += LicenseNumLL;

        ///////////////////////////////////////////////////////////
        /// LicenseCategory
        /// ///////////////////////////////////////////////////////

        byte[] CategorySiklLenth = new byte[4];
        CategorySiklLenth[0] = DG1[currectPos + 5];
        int CategorySiklLenthLL = BitConverter.toInt32(CategorySiklLenth, 0);
        currectPos += 5;

        LicenseCategory[] _categories = new LicenseCategory[CategorySiklLenthLL];

        Log.d(TAG, "categories size = " + CategorySiklLenth);
        for (int j = 0; j < CategorySiklLenthLL; j++) {
            currectPos += 2;

            byte[] FirstCategoryLen = new byte[4];

            FirstCategoryLen[0] = DG1[currectPos];

            int FirstCategoryLenhLL = BitConverter.toInt32(FirstCategoryLen, 0);

            byte[] FirstCategoryData = new byte[FirstCategoryLenhLL];

            System.arraycopy(DG1, currectPos + 1, FirstCategoryData, 0, FirstCategoryLenhLL);

            String FirstCategoryDataStr = new String(FirstCategoryData, StandardCharsets.UTF_8);

            String[] FirstCategoryItems = FirstCategoryDataStr.split(";");

            LicenseCategory lc = new LicenseCategory();
            if (FirstCategoryItems.length == 3) {
                lc.setName(FirstCategoryItems[0]);
                Log.d(TAG, j + " - " + lc.getName());
                lc.setIssueDate(FirstCategoryItems[1]);
                lc.setExpDate(FirstCategoryItems[2]);
            }

            if(FirstCategoryItems.length == 4)
                lc.setAdditionalInfo(FirstCategoryItems[3]);

            _categories[j] = lc;
            currectPos += FirstCategoryLenhLL;
        }

        DLicenseDG1File CustomDG1File = new DLicenseDG1File(
                _first_name,
                lastName,
                null,
                dateOfBirthStr,
                CountryName,
                LicenseN,
                IAFullName1,
                dateOfExpireStr,
                dateOfIssueStr);
        CustomDG1File.setCategories(_categories);

        return CustomDG1File;
    }

    public static DLicenseDG2File parseDG2(byte[] DG2) {
        String Hex2Str = Hex.bytesToHexString(DG2);

        byte[] brthPlsLenth = new byte[4];

        brthPlsLenth[0] = DG2[47];
        int birthPlaceLenth = BitConverter.toInt32(brthPlsLenth, 0);
        byte[] Name = new byte[birthPlaceLenth];
        System.arraycopy(DG2, 48, Name, 0, birthPlaceLenth);
        String resultLastName = new String(Name, StandardCharsets.UTF_8);
        String[] splitted = resultLastName.split(";");
        String _region_name_birth = splitted[0];
        String _pinfl = splitted[1];

        int currentPos = 48 + birthPlaceLenth + 2;

        byte[] resPlsLenth = new byte[4];
        resPlsLenth[0] = DG2[currentPos];
        int resPlaceLenth = BitConverter.toInt32(resPlsLenth, 0);
        byte[] ResName = new byte[resPlaceLenth];
        System.arraycopy(DG2, currentPos + 1, ResName, 0, resPlaceLenth);
        String resultResLastName = new String(ResName, StandardCharsets.UTF_8);
        String[] splittedRes = resultResLastName.split(";");

        String _address = splittedRes[0];
        String _region_name = "";
        String _rayon_name = "";

        if (splittedRes.length > 1)
            _region_name = splittedRes[2];

        if (splittedRes.length > 2)
            _rayon_name = splittedRes[3];

        DLicenseDG2File dg2File = new DLicenseDG2File(
                _region_name_birth, _pinfl, _address,
                _region_name, _rayon_name);

        return dg2File;
    }

    public static Bitmap parseDG4(byte[] DG4) {
        Bitmap bitmap = parseImageFile(DG4, 28);
        Log.d(TAG, "DG4 successfully parsed");
        return bitmap;
    }

    public static Bitmap parseDG5(byte[] DG5) {
        Bitmap bitmap = parseImageFile(DG5, 12);
        Log.d(TAG, "DG5 successfully parsed");
        return bitmap;
    }

    private static Bitmap parseImageFile(byte[] dgFile, int position){
        byte[] Trimmed = MRZUtil.decode(dgFile);
        byte[] ImageBytes = new byte[Trimmed.length - position];
        System.arraycopy(Trimmed, position, ImageBytes, 0, Trimmed.length - position);

        Bitmap bitmap = BitmapFactory.decodeByteArray(ImageBytes, 0, ImageBytes.length);

        return bitmap;
    }

}
