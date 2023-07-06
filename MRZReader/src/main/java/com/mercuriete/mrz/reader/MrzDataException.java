package com.mercuriete.mrz.reader;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class MrzDataException extends Exception {

    @Nullable
    @Override
    public String getMessage() {
        return "Document Type is not valid!!!";
    }

    @NonNull
    @Override
    public String toString() {
        return "Document Type is not valid!!!";
    }
}
