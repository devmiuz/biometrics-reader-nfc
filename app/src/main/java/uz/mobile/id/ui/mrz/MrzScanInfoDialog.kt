package uz.mobile.id.ui.mrz

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.mercuriete.mrz.reader.MrzData.ID_CARD
import com.mercuriete.mrz.reader.MrzData.PASSPORT
import com.mercuriete.mrz.reader.MrzData.TECH_PASSPORT
import com.yariksoffice.lingver.Lingver
import kotlinx.android.synthetic.main.dialog_mrz_info.btnNext
import kotlinx.android.synthetic.main.dialog_mrz_info.ivCardType
import kotlinx.android.synthetic.main.dialog_mrz_info.ivQrCode
import kotlinx.android.synthetic.main.dialog_mrz_info.tvTitleInfo
import kotlinx.android.synthetic.main.dialog_mrz_info.tvTitleQr
import uz.mobile.id.R
import uz.mobile.id.utils.ext.onClick
import uz.mobile.id.utils.ext.show
import java.util.Locale

class MrzScanInfoDialog(private val type: Int, private val nextAction: () -> Unit) :
    DialogFragment() {

    override fun onAttach(context: Context) {
        super.onAttach(context)
        Lingver.getInstance().setLocale(context, Locale.getDefault())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_mrz_info, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)

        ivCardType.setImageResource(
            when (type) {
                PASSPORT -> {
                    R.drawable.vector_passport_instruct
                }
                ID_CARD -> {
                    R.drawable.vector_idcard_scan_instruct
                }
                TECH_PASSPORT -> R.drawable.vector_techpassport_instruct
                else -> R.drawable.vector_driver_licence_instruct
            }
        )
        if (type == PASSPORT) {
            tvTitleInfo.text = getString(R.string.passport_nfc_instruct)
        } else {
            tvTitleQr.show()
            ivQrCode.show()
        }

        btnNext.onClick {
            dismiss()
            nextAction()
        }
    }

}