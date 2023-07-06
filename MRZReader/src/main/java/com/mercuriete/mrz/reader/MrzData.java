package com.mercuriete.mrz.reader;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.util.Log;
import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Pattern;
import kotlin.BuilderInference;

public class MrzData implements Serializable {

    private @MrzDocumentType
    int documentType = PASSPORT;

    private String documentNumber;

    private String birthDate;

    private String expiryDate;

    private String birthDateFormatted;

    private String expiryDateFormatted;

    private String licenseNumber;

    @SuppressLint("SimpleDateFormat")
    private SimpleDateFormat maskedSimpleDateFormat = new SimpleDateFormat("dd.MM.yyyy");
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyMMdd");

    public MrzData(int documentType, String documentNumber, String birthDate, String expiryDate) {
        this.documentType = documentType;
        this.documentNumber = documentNumber;
        this.birthDate = birthDate;
        try {
            this.birthDateFormatted = maskedSimpleDateFormat.format(simpleDateFormat.parse(birthDate));
        } catch (ParseException e) {
            e.printStackTrace();
            this.birthDateFormatted = birthDate;
        }
        if (expiryDate.length() == 6) {
            this.expiryDate = expiryDate;
            try {
                this.expiryDateFormatted = maskedSimpleDateFormat.format(simpleDateFormat.parse(expiryDate));
            } catch (ParseException e) {
                e.printStackTrace();
                this.expiryDateFormatted = expiryDate;
            }
        } else {
            this.licenseNumber = expiryDate;
        }
    }

    public MrzData(int documentType, String documentNumber, Date birthDate, Date expiryDate) {
        this.documentType = documentType;
        this.documentNumber = documentNumber;
        this.birthDate = simpleDateFormat.format(birthDate);
        this.birthDateFormatted = maskedSimpleDateFormat.format(birthDate);
        this.expiryDate = simpleDateFormat.format(expiryDate);
        this.expiryDateFormatted = maskedSimpleDateFormat.format(expiryDate);
    }

    public MrzData(int documentType, String documentNumber, Date birthDate, String expiryDate) {
        this.documentType = documentType;
        this.documentNumber = documentNumber;
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(birthDate);
        this.birthDate = simpleDateFormat.format(birthDate);
        this.birthDateFormatted = maskedSimpleDateFormat.format(birthDate);
        this.licenseNumber = expiryDate;
    }

    public MrzData(String data) throws MrzDataException {
        Log.d("sssss", data);
        if (data.contains("I<"))
            data = data.substring(data.indexOf("I<"));
        else if (data.contains("P<"))
            data = data.substring(data.indexOf("P<"));
//        else throw new MrzDataException();

        String[] lines = data.split("\n");
        documentType = MrzDataUtil.parseDocumentType(lines[0]);
        documentNumber = MrzDataUtil.parseDocumentNumber(lines, documentType);
        birthDate = MrzDataUtil.parseBirthDate(lines, documentType);
        try {
            this.birthDateFormatted = maskedSimpleDateFormat.format(simpleDateFormat.parse(birthDate));
        } catch (ParseException e) {
            e.printStackTrace();
            this.birthDateFormatted = birthDate;
        }
        expiryDate = MrzDataUtil.parseExpiryDate(lines, documentType);
        try {
            this.expiryDateFormatted = maskedSimpleDateFormat.format(simpleDateFormat.parse(expiryDate));
        } catch (ParseException e) {
            e.printStackTrace();
            this.expiryDateFormatted = expiryDate;
        }
    }

    public @MrzDocumentType
    int getDocumentType() {
        return documentType;
    }

    public void setBirthDate(final String birthDate) {
        this.birthDate = birthDate;
    }

    public void setDocumentNumber(final String documentNumber) {
        this.documentNumber = documentNumber;
    }

    public void setDocumentType(final int documentType) {
        this.documentType = documentType;
    }

    public void setExpiryDate(final String expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getDocumentNumber() {
        return documentNumber;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public String getFormattedBirthDate() {
        return birthDateFormatted;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public String getFormattedExpiryDate() {
        return expiryDateFormatted;
    }

    public String getLicenseNumber() {
        return licenseNumber;
    }

    public void setLicenseNumber(final String licenseNumber) {
        this.licenseNumber = licenseNumber;
    }

    @IntDef({ID_CARD, PASSPORT, DRIVER_LICENSE, TECH_PASSPORT})
    public @interface MrzDocumentType { }

    public static final int ID_CARD = 10;

    public static final int PASSPORT = 11;

    public static final int DRIVER_LICENSE = 12;

    public static final int TECH_PASSPORT = 13;

    @Override
    public String toString() {
        return "MrzData{" +
                "documentType=" + documentType +
                ", documentNumber='" + documentNumber + '\'' +
                ", birthDate='" + birthDate + '\'' +
                ", expiryDate='" + expiryDate + '\'' +
                ", licenseNumber='" + licenseNumber + '\'' +
                '}';
    }
}
