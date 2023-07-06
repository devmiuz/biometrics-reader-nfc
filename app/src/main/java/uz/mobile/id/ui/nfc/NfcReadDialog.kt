package uz.mobile.id.ui.nfc

import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.IsoDep
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.airbnb.lottie.LottieAnimationView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.mercuriete.mrz.reader.MrzData
import org.jmrtd.BACKey
import uz.mobile.id.R
import uz.mobile.id.R.string
import uz.mobile.id.model.NfcData
import uz.mobile.id.ui.MainActivityViewModel
import uz.mobile.id.utils.CustomButton
import uz.mobile.id.utils.ext.hideAnimWithScale
import uz.mobile.id.utils.ext.inflate
import uz.mobile.id.utils.ext.invisible
import uz.mobile.id.utils.ext.isVisible
import uz.mobile.id.utils.ext.navigateSafe
import uz.mobile.id.utils.ext.onClick
import uz.mobile.id.utils.ext.show
import uz.mobile.id.utils.ext.showAnimWithScale
import uz.mobile.id.utils.ext.startLottieAnimation
import java.util.Arrays

class NfcReadDialog : BottomSheetDialogFragment() {
    private val mainViewModel by lazy {
        ViewModelProvider(requireActivity()).get(
            MainActivityViewModel::class.java
        )
    }
    private val vm by lazy { ViewModelProvider(this).get(NfcDialogViewModel::class.java) }
    private val documentType by lazy { arguments?.getInt("documentType", MrzData.PASSPORT) }

    private var progressNfcRead: View? = null
    private var btnCancel: CustomButton? = null
    private var flContainer: ViewGroup? = null
    private var llStartNfcScan: ViewGroup? = null

    override fun onStart() {
        super.onStart()
        dialog?.window?.setBackgroundDrawable(ColorDrawable(0))
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        (view?.parent as View).setBackgroundColor(Color.TRANSPARENT)
        mainViewModel.nfcIntent.value = null
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.dialog_nfc_read, container, false)

        progressNfcRead = view.findViewById(R.id.progressNfcRead)
        btnCancel = view.findViewById(R.id.btnCancel)
        llStartNfcScan = view.findViewById(R.id.llStartNfcScan)
        flContainer = view.findViewById(R.id.flContainer)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (view.parent as? View)?.setBackgroundColor(Color.TRANSPARENT)
        dialog?.setCanceledOnTouchOutside(false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            dialog?.window?.statusBarColor =
                ResourcesCompat.getColor(this.resources, android.R.color.white, null)
            dialog?.window?.navigationBarColor =
                ResourcesCompat.getColor(this.resources, android.R.color.white, null)
        }

        vm.nfcInfoEvent.observe(this, Observer {
            onSuccessNfcRead(it)
        })
        vm.nfcReadFailEvent.observe(this, Observer {
//      Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            onFailNfcRead()
        })
        if (mainViewModel.mrzDataEvent.value == null) dismissAllowingStateLoss()
    }

    override fun onResume() {
        super.onResume()
        mainViewModel.nfcIntent.observe(this, Observer {
            if (it != null && vm.nfcInfoEvent.value == null) {
                val tag: Tag = it.extras?.getParcelable(NfcAdapter.EXTRA_TAG)!!
                if (mutableListOf(*tag.techList).contains("android.nfc.tech.IsoDep")) {
                    with(mainViewModel.mrzDataEvent.value!!) {
                        when (documentType) {
                            MrzData.PASSPORT,
                            MrzData.ID_CARD -> {
                                vm.readPassport(
                                    IsoDep.get(tag),
                                    BACKey(documentNumber, birthDate, expiryDate)
                                )
                            }

                            MrzData.DRIVER_LICENSE -> {
                                vm.readDriverLicense(
                                    IsoDep.get(tag),
                                    BACKey(documentNumber, birthDate, expiryDate)
                                )
                            }

                            else -> {
                                vm.readTechPassport(
                                    IsoDep.get(tag),
                                    documentNumber,
                                    birthDate,
                                    licenseNumber
                                )
                            }
                        }

                        progressNfcRead?.show()
                        flContainer?.removeAllViews()
                        if (llStartNfcScan?.isVisible() == false) {
                            llStartNfcScan?.showAnimWithScale()
                        }
                        btnCancel?.setButtonText(R.string.cancel)
                        btnCancel?.setButtonEnabled(false)
                    }
                }
            }
        })
    }

    private fun onFailNfcRead() {
        progressNfcRead?.invisible()
        llStartNfcScan?.hideAnimWithScale(duration = 300, withEndInvisible = true)
        flContainer?.showAnimWithScale(duration = 300)
        flContainer?.inflate(R.layout.layout_nfc_fail, true)
        flContainer?.findViewById<LottieAnimationView>(R.id.lottieFail)?.startLottieAnimation {

        }

        btnCancel?.setButtonEnabled(true)
        btnCancel?.onClick {
            dismissAllowingStateLoss()
        }
    }

    private fun onSuccessNfcRead(data: NfcData) {
        progressNfcRead?.invisible()
        llStartNfcScan?.hideAnimWithScale(duration = 300, withEndInvisible = true)
        flContainer?.showAnimWithScale(duration = 300)
        flContainer?.inflate(R.layout.layout_nfc_success, true)
        flContainer?.findViewById<LottieAnimationView>(R.id.lottieSuccess)?.startLottieAnimation {
            mainViewModel.mrzDataEvent.value = null
            findNavController().navigateSafe(
                R.id.action_nfcReadDialog_to_fullInfoFragment,
                Bundle().apply {
                    putParcelable("data", data)
                })
        }

        btnCancel?.setButtonEnabled(true)
        btnCancel?.setButtonText(getString(string.ready))
        btnCancel?.onClick {
            dismissAllowingStateLoss()
            mainViewModel.mrzDataEvent.value = null
            findNavController().navigateSafe(
                R.id.action_nfcReadDialog_to_fullInfoFragment,
                Bundle().apply {
                    putParcelable("data", data)
                })
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        mainViewModel.nfcIntent.removeObservers(viewLifecycleOwner)
    }
}