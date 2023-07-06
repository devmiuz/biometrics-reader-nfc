package uz.mobile.id.ui.home

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.yariksoffice.lingver.Lingver
import kotlinx.android.synthetic.main.dialog_settings.btnOk
import kotlinx.android.synthetic.main.dialog_settings.ivCheckEnglish
import kotlinx.android.synthetic.main.dialog_settings.ivCheckKrill
import kotlinx.android.synthetic.main.dialog_settings.ivCheckLotin
import kotlinx.android.synthetic.main.dialog_settings.ivCheckRussian
import kotlinx.android.synthetic.main.dialog_settings.llEnglish
import kotlinx.android.synthetic.main.dialog_settings.llKrill
import kotlinx.android.synthetic.main.dialog_settings.llLotin
import kotlinx.android.synthetic.main.dialog_settings.llRussian
import uz.mobile.id.R
import uz.mobile.id.ui.MainActivityViewModel
import uz.mobile.id.utils.PreferenceUtil
import uz.mobile.id.utils.ext.hide
import uz.mobile.id.utils.ext.onClick
import uz.mobile.id.utils.ext.show
import java.util.Locale

class SettingsDialogFragment: DialogFragment() {

  private val preferenceUtil by lazy { PreferenceUtil.getInstance(requireContext()) }

  private val mainViewModel by lazy {
    ViewModelProvider(requireActivity()).get(MainActivityViewModel::class.java)
  }

  private lateinit var selectedLanguage: String

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?)
                            = inflater.inflate(R.layout.dialog_settings, container, false)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)

    selectedLanguage = preferenceUtil.getLanguage()

    fun onSelectLanguage() {
      when(selectedLanguage) {
        "uz" -> {
          ivCheckLotin.show()
          ivCheckKrill.hide()
          ivCheckRussian.hide()
          ivCheckEnglish.hide()
        }
        "fr" -> {
          ivCheckLotin.hide()
          ivCheckKrill.show()
          ivCheckRussian.hide()
          ivCheckEnglish.hide()
        }
        "ru" -> {
          ivCheckLotin.hide()
          ivCheckKrill.hide()
          ivCheckRussian.show()
          ivCheckEnglish.hide()
        }
        else -> {
          ivCheckLotin.hide()
          ivCheckKrill.hide()
          ivCheckRussian.hide()
          ivCheckEnglish.show()
        }
      }
    }

    onSelectLanguage()

    llLotin.onClick {
      selectedLanguage = "uz"
      onSelectLanguage()
    }

    llKrill.onClick {
      selectedLanguage = "fr"
      onSelectLanguage()
    }

    llRussian.onClick {
      selectedLanguage = "ru"
      onSelectLanguage()
    }

    llEnglish.onClick {
      selectedLanguage = "en"
      onSelectLanguage()
    }

    btnOk.onClick {
      preferenceUtil.setLanguage(selectedLanguage)
      preferenceUtil.setIsFirstOpen(false)
      Lingver.getInstance().setLocale(requireContext(), selectedLanguage)

      updateResources(requireContext(), selectedLanguage)
      mainViewModel.languageChangeEvent.value = Unit
      dismissAllowingStateLoss()
    }
  }

  private fun updateResources(context: Context, language: String): Context {
    return Configuration(context.resources.configuration).run {
      Locale.setDefault(Locale(language).also { locale ->
        setLocale(locale)
        setLayoutDirection(locale)
      }).let {
        context.createConfigurationContext(this)
      }
    }
  }
}