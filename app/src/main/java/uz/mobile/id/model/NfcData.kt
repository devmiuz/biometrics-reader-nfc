package uz.mobile.id.model

import android.graphics.Bitmap
import android.os.Parcelable
import corn.cardreader.model.dgFiles.DLicenseDG1File
import corn.cardreader.model.dgFiles.DLicenseDG2File
import kotlinx.android.parcel.Parcelize
import org.jmrtd.lds.MRZInfo

@Parcelize
data class NfcData(
    val documentCode: String,
    val documentNumber: String,
    val issuingState: String,
    val birthDate: String,
    val dateOfExpiration: String,
    val firstName: String,
    val lastName: String,
    val birthPlace: String? = null,
    val residenceAddress: String? = null,
    val dateOfIssue: String? = null,
    val gender: String? = null,
    val nationality: String? = null,
    val userBitmap: Bitmap? = null,
    val signBitmap: Bitmap? = null,
    val techInfos: Map<String, String>? = null,
    val pinfl: String? = null
) : Parcelable {
    companion object {
        fun makeTechInfos(data: Map<String, String>): NfcData {
            return NfcData(
                "", "",
                "", "", "",
                "", "", "", "",
                "", techInfos = data
            )
        }
    }
}

fun MRZInfo.mapToNfcData(
    userBitmap: Bitmap?,
    permanentAddress: String? = null,
    birthPlace: String? = null,
    signBitmap: Bitmap?
) = NfcData(
    documentCode = documentCode,
    documentNumber = documentNumber,
    issuingState = issuingState,
    birthDate = dateOfBirth,
    birthPlace = birthPlace,
    residenceAddress = permanentAddress,
    dateOfExpiration = dateOfExpiry,
    firstName = secondaryIdentifier.replace("<", ""),
    lastName = primaryIdentifier.replace("<", ""),
    gender = gender.toString(),
    nationality = nationality,
    userBitmap = userBitmap,
    pinfl = optionalData1,
    signBitmap = signBitmap
)

fun DLicenseDG1File.mapToNfcData(userBitmap: Bitmap?, signBitmap: Bitmap?) = NfcData(
    documentCode = "DL",
    documentNumber = licenceNumber,
    issuingState = "Uzb",
    birthDate = dateOfBirth,
    dateOfExpiration = dateOfExpiration,
    dateOfIssue = dateOfIssue,
    firstName = firstName,
    lastName = lastName,
    userBitmap = userBitmap,
    signBitmap = signBitmap
)

fun DLicenseDG2File.mapToNfcData(nfcData: NfcData) =
    nfcData.copy(birthPlace = birthPlace, residenceAddress = residenceAddress)