package uz.mobile.id.ui.nfc

import android.app.Application
import android.graphics.Bitmap
import android.nfc.tech.IsoDep
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.tananaev.passportreader.ImageUtil
import corn.cardreader.model.dgFiles.DLicenseDG1File
import corn.cardreader.model.dgFiles.DLicenseDG2File
import corn.cardreader.tech_card.util.TechCardBACKey
import corn.cardreader.tech_card.util.TechCardReaderDelegate
import corn.cardreader.tech_card.util.TechCardReaderService
import corn.cardreader.utilities.drivingLicense.DLicenseReaderDelegate
import corn.cardreader.utilities.drivingLicense.DrivingLicenseReaderService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jmrtd.BACKey
import org.jmrtd.BACKeySpec
import org.jmrtd.PassportService
import org.jmrtd.lds.CardAccessFile
import org.jmrtd.lds.DG1File
import org.jmrtd.lds.DG2File
import org.jmrtd.lds.DG3File
import org.jmrtd.lds.DG7File
import org.jmrtd.lds.DisplayedImageInfo
import org.jmrtd.lds.FaceImageInfo
import org.jmrtd.lds.FingerImageInfo
import org.jmrtd.lds.LDS
import org.jmrtd.lds.PACEInfo
import uz.mobile.id.App
import uz.mobile.id.model.NfcData
import uz.mobile.id.model.mapToNfcData
import java.io.ByteArrayInputStream
import java.io.DataInputStream
import java.io.InputStream
import java.util.ArrayList

class NfcDialogViewModel(app: Application) : AndroidViewModel(app) {

    val nfcInfoEvent = MutableLiveData<NfcData>()
    val nfcReadFailEvent = MutableLiveData<String>()

    private var userBitmap: Bitmap? = null
    private var signBitmap: Bitmap? = null

    fun readTechPassport(
        isoDep: IsoDep,
        regNumber: String,
        givenDate: String,
        licenseNumber: String
    ) {
        val bacKey = TechCardBACKey(regNumber, givenDate, licenseNumber)

        TechCardReaderService.parseIntent(isoDep, bacKey, object : TechCardReaderDelegate {
            override fun onFinish(data: Map<String, String>) {
                nfcInfoEvent.postValue(NfcData.makeTechInfos(data))
            }

            override fun onError(errorID: Int) {
                nfcReadFailEvent.postValue(getApplication<App>().getString(errorID))
            }
        })
    }

    fun readDriverLicense(isoDep: IsoDep, bacKey: BACKeySpec) {
        DrivingLicenseReaderService.parseIntent(isoDep, bacKey, object : DLicenseReaderDelegate {

            override fun onFinish(
                customDG1File: DLicenseDG1File?,
                customDG2File: DLicenseDG2File?
            ) {
                nfcInfoEvent.value = customDG2File?.mapToNfcData(
                    requireNotNull(customDG1File?.mapToNfcData(userBitmap, signBitmap))
                )
            }

            override fun onSignImageRead(signBitmap: Bitmap?) {
                this@NfcDialogViewModel.signBitmap = signBitmap
            }

            override fun onUserImageRead(userImageBitmap: Bitmap?) {
                this@NfcDialogViewModel.userBitmap = userImageBitmap
            }

            override fun onError(errorID: Int) {
                nfcReadFailEvent.value = getApplication<App>().getString(errorID)
            }
        })
    }

    fun readPassport(isoDep: IsoDep, bacKey: BACKey) {
        viewModelScope.launch(Dispatchers.IO) {
            val dg1File: DG1File?
            val dg2File: DG2File?

            try {
                val cardService: net.sf.scuba.smartcards.CardService =
                    net.sf.scuba.smartcards.CardService.getInstance(isoDep)
                cardService.open()

                val passportService = PassportService(cardService)
                passportService.open()

                var paceSucceeded = false
                try {
                    val cardAccessFile =
                        CardAccessFile(passportService.getInputStream(PassportService.EF_CARD_ACCESS))
                    val paceInfos: Collection<PACEInfo> = cardAccessFile.paceInfos
                    paceSucceeded = if (paceInfos.isNotEmpty()) {
                        val paceInfo = paceInfos.iterator().next()
                        passportService.doPACE(
                            bacKey,
                            paceInfo.objectIdentifier,
                            PACEInfo.toParameterSpec(paceInfo.parameterId)
                        )
                        true
                    } else {
                        true
                    }
                } catch (e: Exception) {
                    Log.d("sssss", "" + e.message)
                }

                passportService.sendSelectApplet(paceSucceeded)

                if (!paceSucceeded) {
                    try {
                        passportService.getInputStream(PassportService.EF_COM).read()
                    } catch (e: Exception) {
                        passportService.doBAC(bacKey)
                    }
                }

                val lds = LDS()

                val dg1In: net.sf.scuba.smartcards.CardFileInputStream =
                    passportService.getInputStream(PassportService.EF_DG1)
                lds.add(PassportService.EF_DG1, dg1In, dg1In.length)
                dg1File = lds.dG1File

                val dg2In: net.sf.scuba.smartcards.CardFileInputStream =
                    passportService.getInputStream(PassportService.EF_DG2)
                lds.add(PassportService.EF_DG2, dg2In, dg2In.length)
                dg2File = lds.dG2File

                val allFaceImageInfo: MutableList<FaceImageInfo> = ArrayList()
                val faceInfos = dg2File.faceInfos
                for (faceInfo in faceInfos) {
                    allFaceImageInfo.addAll(faceInfo.faceImageInfos)
                }

                if (allFaceImageInfo.isNotEmpty()) {
                    val faceImageInfo = allFaceImageInfo.iterator().next()

                    val imageLength = faceImageInfo.imageLength
                    val dataInputStream = DataInputStream(faceImageInfo.imageInputStream)
                    val buffer = ByteArray(imageLength)
                    dataInputStream.readFully(buffer, 0, imageLength)
                    val inputStream: InputStream = ByteArrayInputStream(buffer, 0, imageLength)

                    userBitmap = ImageUtil.decodeImage(
                        getApplication(), faceImageInfo.mimeType, inputStream
                    )
                }

//                getFingerPrint(passportService,lds)

                getSignature(passportService, lds)


                var permanentAddress: String? = null
                var birthPlace: String? = null
                try {
                    val dg11In: net.sf.scuba.smartcards.CardFileInputStream =
                        passportService.getInputStream(PassportService.EF_DG11)
                    lds.add(PassportService.EF_DG11, dg11In, dg11In.length)
                    permanentAddress = lds.dG11File.permanentAddress.joinToString()
                    birthPlace = lds.dG11File.placeOfBirth.joinToString()
                } catch (e: Exception) {
                }

                nfcInfoEvent.postValue(
                    dg1File.mrzInfo.mapToNfcData(
                        userBitmap,
                        permanentAddress,
                        birthPlace,
                        signBitmap
                    )
                )
            } catch (e: Exception) {
                nfcReadFailEvent.postValue(exceptionStack(e))
            }
        }
    }

    private fun getFingerPrint(passportService: PassportService, lds: LDS) {

        val fingerprintInputStream: net.sf.scuba.smartcards.CardFileInputStream = passportService.getInputStream(PassportService.EF_DG3)

        lds.add(PassportService.EF_DG3, fingerprintInputStream, fingerprintInputStream.length)

        val fingerprintFile: DG3File = lds.dG3File ?: return

        val fingerprintImageInfoList: MutableList<FingerImageInfo> = ArrayList()
        val fingerprintInfoList = fingerprintFile.fingerInfos

        for (faceInfo in fingerprintInfoList) {
            fingerprintImageInfoList.addAll(faceInfo.fingerImageInfos)
        }

        if (fingerprintImageInfoList.isNotEmpty()) {
            val fingerprintImageInfo = fingerprintImageInfoList.iterator().next()

            val imageLength = fingerprintImageInfo.imageLength
            val dataInputStream = DataInputStream(fingerprintImageInfo.imageInputStream)
            val buffer = ByteArray(imageLength)
            dataInputStream.readFully(buffer, 0, imageLength)
            val inputStream: InputStream = ByteArrayInputStream(buffer, 0, imageLength)

            userBitmap = ImageUtil.decodeImage(
                getApplication(), fingerprintImageInfo.mimeType, inputStream
            )
        }
    }

    private fun getSignature(passportService: PassportService, lds: LDS) {

        val signatureInputStream: net.sf.scuba.smartcards.CardFileInputStream = passportService.getInputStream(PassportService.EF_DG7)

        lds.add(PassportService.EF_DG7, signatureInputStream, signatureInputStream.length)

        val signatureFile: DG7File = lds.dG7File ?: return

        val fingerprintImageInfoList: MutableList<DisplayedImageInfo> = ArrayList()
        val fingerprintInfoList = signatureFile.images

        for (faceInfo in fingerprintInfoList) {
            fingerprintImageInfoList.add(faceInfo)
        }

        if (fingerprintImageInfoList.isNotEmpty()) {
            val fingerprintImageInfo = fingerprintImageInfoList.iterator().next()

            val imageLength = fingerprintImageInfo.imageLength
            val dataInputStream = DataInputStream(fingerprintImageInfo.imageInputStream)
            val buffer = ByteArray(imageLength)
            dataInputStream.readFully(buffer, 0, imageLength)
            val inputStream: InputStream = ByteArrayInputStream(buffer, 0, imageLength)

            signBitmap = ImageUtil.decodeImage(
                getApplication(), fingerprintImageInfo.mimeType, inputStream
            )
        }
    }


    fun exceptionStack(exception: Throwable): String {
        val s = StringBuilder()
        val exceptionMsg = exception.message
        if (exceptionMsg != null) {
            s.append(exceptionMsg)
            s.append(" - ")
        }
        s.append(exception.javaClass.simpleName)
        val stack = exception.stackTrace
        if (stack.size > 0) {
            var count = 3
            var first = true
            var skip = false
            var file = ""
            s.append(" (")
            for (element in stack) {
                if (count > 0 && element.className.startsWith("com.tananaev")) {
                    if (!first) {
                        s.append(" < ")
                    } else {
                        first = false
                    }
                    if (skip) {
                        s.append("... < ")
                        skip = false
                    }
                    if (file == element.fileName) {
                        s.append("*")
                    } else {
                        file = element.fileName
                        s.append(file.substring(0, file.length - 5)) // remove ".java"
                        count -= 1
                    }
                    s.append(":").append(element.lineNumber)
                } else {
                    skip = true
                }
            }
            if (skip) {
                if (!first) {
                    s.append(" < ")
                }
                s.append("...")
            }
            s.append(")")
        }
        return s.toString()
    }
}