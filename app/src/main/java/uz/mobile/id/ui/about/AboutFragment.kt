package uz.mobile.id.ui.about

import android.os.Build
import android.os.Bundle
import android.text.Html
import android.view.View
import androidx.core.text.parseAsHtml
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_about.toolbar
import kotlinx.android.synthetic.main.fragment_about.tvAbout
import uz.mobile.id.R

class AboutFragment: Fragment(R.layout.fragment_about) {

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
      tvAbout.setText(Html.fromHtml(getString(R.string.about_iduz), Html.FROM_HTML_MODE_COMPACT));
    } else {
      tvAbout.setText(Html.fromHtml(getString(R.string.about_iduz)));
    }

    toolbar.setNavigationOnClickListener {
      findNavController().popBackStack()
    }
  }

}