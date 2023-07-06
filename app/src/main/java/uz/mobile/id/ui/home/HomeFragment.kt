package uz.mobile.id.ui.home

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.mercuriete.mrz.reader.CaptureActivity
import com.mercuriete.mrz.reader.MrzData
import com.mercuriete.mrz.reader.MrzData.DRIVER_LICENSE
import com.mercuriete.mrz.reader.MrzData.ID_CARD
import com.mercuriete.mrz.reader.MrzData.MrzDocumentType
import com.mercuriete.mrz.reader.MrzData.PASSPORT
import com.mercuriete.mrz.reader.MrzData.TECH_PASSPORT
import com.yariksoffice.lingver.Lingver
import kotlinx.android.synthetic.main.fragment_home.cvDriverLicence
import kotlinx.android.synthetic.main.fragment_home.cvIdCard
import kotlinx.android.synthetic.main.fragment_home.cvPassport
import kotlinx.android.synthetic.main.fragment_home.cvTexPassport
import kotlinx.android.synthetic.main.fragment_home.ivInfo
import kotlinx.android.synthetic.main.fragment_home.ivSettings
import uz.mobile.id.R
import uz.mobile.id.ui.MainActivityViewModel
import uz.mobile.id.ui.mrz.MrzScanInfoDialog
import uz.mobile.id.utils.REQUEST_CAMERA
import uz.mobile.id.utils.ext.navigateSafe
import uz.mobile.id.utils.ext.onClick
import uz.mobile.id.utils.hasCameraPermission
import java.util.Locale

class HomeFragment : Fragment(R.layout.fragment_home) {

  override fun onAttach(context: Context) {
    super.onAttach(context)
    Lingver.getInstance().setLocale(context, Locale.getDefault())
  }

  private var type: Int = ID_CARD

  private val mainViewModel by lazy {
    ViewModelProvider(requireActivity()).get(MainActivityViewModel::class.java)
  }

  private val navController by lazy { findNavController() }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    initClicks()

    mainViewModel.mrzDataEvent.removeObservers(viewLifecycleOwner)
    mainViewModel.mrzDataEvent.observe(viewLifecycleOwner, Observer {
      view.post {
        it?.run { navController.navigateSafe(R.id.action_homeFragment_to_mrzInfoFragment) }
      }
    })
    mainViewModel.languageChangeEvent.observe(viewLifecycleOwner, Observer {
      parentFragmentManager
        .beginTransaction()
        .detach(this)
        .attach(this)
        .commitAllowingStateLoss();
    })
  }

  override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    if (requestCode == REQUEST_CAMERA && grantResults.isNotEmpty()) {
      openInstruct(type)
    }
  }

  private fun initClicks() {
    ivInfo.setOnClickListener {
      findNavController().navigateSafe(R.id.action_homeFragment_to_AboutFragment)
    }
    cvIdCard.setOnClickListener {
      openInstruct(ID_CARD)
    }
    cvPassport.setOnClickListener {
      openInstruct(PASSPORT)
    }
    cvDriverLicence.onClick {
      openInstruct(DRIVER_LICENSE)
    }
    cvTexPassport.onClick {
      openInstruct(TECH_PASSPORT)
    }
    ivSettings.onClick {
      SettingsDialogFragment()
        .show(parentFragmentManager, "settings")
    }
  }

  private fun openInstruct(@MrzDocumentType type: Int) {
    this.type = type
    val bundle = bundleOf("documentType" to type)
    if (type == DRIVER_LICENSE) {
      findNavController().navigateSafe(R.id.action_homeFragment_to_manualEnterInfoFragment, bundle)
    } else if (type == TECH_PASSPORT) {
      findNavController().navigateSafe(R.id.action_homeFragment_to_manualEnterTechPassport, bundle)
    } else {
      MrzScanInfoDialog(type) {
        Handler().postDelayed({
          if (hasCameraPermission(requireActivity(), type)) {
            if (type == ID_CARD) {
              CaptureActivity.start(requireActivity(), ID_CARD)
            } else if (type == PASSPORT) {
              CaptureActivity.start(requireActivity(), PASSPORT)
            }
          }
        }, 100)
      }.show(parentFragmentManager, "info")
    }
  }
}