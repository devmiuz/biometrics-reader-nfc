package uz.mobile.id.ui.main

import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.yariksoffice.lingver.Lingver
import kotlinx.android.synthetic.main.fragment_select_language.btnEnglish
import kotlinx.android.synthetic.main.fragment_select_language.btnKrill
import kotlinx.android.synthetic.main.fragment_select_language.btnLotin
import kotlinx.android.synthetic.main.fragment_select_language.btnRussian
import uz.mobile.id.R
import uz.mobile.id.utils.PreferenceUtil
import uz.mobile.id.utils.ext.navigateSafe
import uz.mobile.id.utils.ext.onClick

class SelectLanguageFragment : Fragment(R.layout.fragment_select_language) {

  private val preferenceUtil by lazy { PreferenceUtil.getInstance(requireContext()) }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    fun onSelectLanguage(language: String) {
      preferenceUtil.setLanguage(language)
      preferenceUtil.setIsFirstOpen(false)
      Lingver.getInstance().setLocale(requireContext(), language)
      findNavController().navigateSafe(R.id.action_selectLanguageFragment_to_homeFragment)
    }

    btnLotin.onClick { onSelectLanguage("uz") }
    btnKrill.onClick { onSelectLanguage("fr") }
    btnRussian.onClick { onSelectLanguage("ru") }
    btnEnglish.onClick { onSelectLanguage("en") }
  }
}