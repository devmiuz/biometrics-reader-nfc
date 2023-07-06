package uz.mobile.id.ui.mrz

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.annotation.FloatRange
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.mercuriete.mrz.reader.MrzData
import com.yariksoffice.lingver.Lingver
import kotlinx.android.synthetic.main.fragment_mrz_info.cvNext
import kotlinx.android.synthetic.main.fragment_mrz_info.toolbar
import kotlinx.android.synthetic.main.fragment_mrz_info.tvBirthDate
import kotlinx.android.synthetic.main.fragment_mrz_info.tvDocumentNumber
import kotlinx.android.synthetic.main.fragment_mrz_info.tvExpiryDate
import kotlinx.android.synthetic.main.fragment_mrz_info.tvTitleBirthDate
import kotlinx.android.synthetic.main.fragment_mrz_info.tvTitleDocumentNumber
import kotlinx.android.synthetic.main.fragment_mrz_info.tvTitleExpiredDate
import uz.mobile.id.R
import uz.mobile.id.ui.MainActivityViewModel
import uz.mobile.id.utils.ext.navigateSafe
import java.util.Locale

class MrzInfoFragment : Fragment(R.layout.fragment_mrz_info) {
    private val navController by lazy { findNavController() }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        Lingver.getInstance().setLocale(context, Locale.getDefault())
    }

    private val mainViewModel by lazy {
        ViewModelProvider(requireActivity()).get(MainActivityViewModel::class.java)
    }
    private val documentType by lazy { arguments?.getInt("documentType", MrzData.PASSPORT) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toolbar.setNavigationOnClickListener {
            mainViewModel.mrzDataEvent.value = null
            navController.popBackStack()
        }

        mainViewModel.mrzDataEvent.value.run {
            if (this == null) navController.navigateUp()
            tvDocumentNumber.text = this?.documentNumber
            tvBirthDate.text = this?.formattedBirthDate
            if (documentType == MrzData.TECH_PASSPORT) {
                tvExpiryDate.text = this?.licenseNumber
                tvTitleExpiredDate.text = getString(R.string.license_num_title)
                tvTitleBirthDate.text = getString(R.string.date_of_given_tech_passport)
                tvTitleDocumentNumber.text = getString(R.string.car_number)
            } else {
                tvExpiryDate.text = this?.formattedExpiryDate
            }
        }

        cvNext.setOnClickListener {
            navController.navigateSafe(R.id.action_mrzInfoFragment_to_nfcReadDialog, arguments)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mainViewModel.mrzDataEvent.value = null
    }
}