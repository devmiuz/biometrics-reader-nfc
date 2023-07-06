package com.mercuriete.mrz.reader;

import static com.mercuriete.mrz.reader.MrzData.ID_CARD;
import static com.mercuriete.mrz.reader.MrzData.PASSPORT;

import com.mercuriete.mrz.reader.MrzData.MrzDocumentType;
import java.text.SimpleDateFormat;
import java.util.regex.Pattern;

class MrzDataUtil {
    private static final Pattern passportPattern
            = Pattern.compile("[A-Z]{2}[0-9]{7}");
    private static final Pattern numberPattern
            = Pattern.compile("^[0-9]+$");
    private static final Pattern simpleDataPattern
            = Pattern.compile("^[0-9][0-9](([0][0-9])|[1][0-2])(([0-2][0-9])|[3][0-1])$");
    private static final Pattern idCardPattern
            = Pattern.compile("^[I]([A-Z]|[<])[U][Z][B].*$");

    static @MrzDocumentType int parseDocumentType(String line1) throws MrzDataException {
        if (idCardPattern.matcher(line1).matches()) {
            return ID_CARD;
        }
        if (line1.startsWith("P<")) {
            return PASSPORT;
        }
        throw new MrzDataException();
    }

    static String parseDocumentNumber(String [] lines, int docType) throws MrzDataException {
        String docNumber;
        String partOfNumber;
        if (docType == ID_CARD) {
            try {
                docNumber = lines[0].substring(5, 14);
                partOfNumber = docNumber.substring(2);
                docNumber = docNumber.replace(partOfNumber, charToDigits(partOfNumber));
                if (passportPattern.matcher(docNumber).matches())
                    return docNumber;
                throw new MrzDataException();
            } catch (Exception e) {
                throw new MrzDataException();
            }
        } else {
            try {
                docNumber = lines[1].substring(0, 9);
                partOfNumber = docNumber.substring(2);
                docNumber = docNumber.replace(partOfNumber, charToDigits(partOfNumber));
                if (passportPattern.matcher(docNumber).matches())
                    return docNumber;
                throw new MrzDataException();
            } catch (Exception e) {
                throw new MrzDataException();
            }
        }
    }

    static String parseBirthDate(String [] lines, int docType) throws MrzDataException {
        String birthDate;
        if (docType == ID_CARD) {
            try {
                birthDate = charToDigits(lines[1].substring(0, 6));
                if (numberPattern.matcher(birthDate).matches() && checkParseDate(birthDate))
                    return birthDate;
                throw new MrzDataException();
            } catch (Exception e) {
                throw new MrzDataException();
            }
        } else {
            try {
                birthDate = charToDigits(lines[1].substring(13, 19));
                if (numberPattern.matcher(birthDate).matches() && checkParseDate(birthDate))
                    return birthDate;
                throw new MrzDataException();
            } catch (Exception e) {
                throw new MrzDataException();
            }
        }
    }

    static String parseExpiryDate(String [] lines, int docType) throws MrzDataException {
        String expiryDate;
        if (docType == ID_CARD) {
            try {
                expiryDate = charToDigits(lines[1].substring(8, 14));
                if (numberPattern.matcher(expiryDate).matches() && checkParseDate(expiryDate))
                    return expiryDate;
                throw new MrzDataException();
            } catch (Exception e) {
                throw new MrzDataException();
            }
        } else {
            try {
                expiryDate = charToDigits(lines[1].substring(21, 27));
                if (numberPattern.matcher(expiryDate).matches() && checkParseDate(expiryDate))
                    return expiryDate;
                throw new MrzDataException();
            } catch (Exception e) {
                throw new MrzDataException();
            }
        }
    }

    private static String charToDigits(String data) {
        return data
                .replace("O", "0")
                .replace("U", "0")
                .replace("Z", "2")
                .replace("I", "1")
                .replace("D", "0")
                .replace("Y", "7")
                .replace("T", "7")
                .replace("B", "8");
    }

    private static boolean checkParseDate(String s) {
        return simpleDataPattern.matcher(s).matches();
    }
}