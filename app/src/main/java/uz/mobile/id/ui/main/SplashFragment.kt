package uz.mobile.id.ui.main

import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_splash.lottieView
import uz.mobile.id.R
import uz.mobile.id.utils.PreferenceUtil
import uz.mobile.id.utils.ext.navigateSafe
import uz.mobile.id.utils.ext.startLottieAnimation

class SplashFragment : Fragment(R.layout.fragment_splash) {

  private val preferenceUtil by lazy { PreferenceUtil.getInstance(requireContext()) }

  override fun onResume() {
    super.onResume()

    lottieView.startLottieAnimation {
      findNavController().navigateSafe(
        if (preferenceUtil.isFirstOpen()) R.id.action_splashFragment_to_selectLanguageFragment
        else R.id.action_splashFragment_to_homeFragment
      )
    }
  }
}