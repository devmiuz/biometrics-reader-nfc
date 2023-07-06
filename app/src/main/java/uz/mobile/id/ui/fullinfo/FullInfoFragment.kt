package uz.mobile.id.ui.fullinfo

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.LinearLayout.LayoutParams
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_full_info.*
import uz.mobile.id.R
import uz.mobile.id.model.NfcData
import uz.mobile.id.utils.ext.dpToPx
import uz.mobile.id.utils.ext.getStringResourcesByName
import uz.mobile.id.utils.ext.hide
import uz.mobile.id.utils.ext.isVisible
import uz.mobile.id.utils.ext.navigateSafe
import uz.mobile.id.utils.ext.onClick
import uz.mobile.id.utils.ext.views
import java.lang.Exception
import java.text.SimpleDateFormat

class FullInfoFragment : Fragment(R.layout.fragment_full_info) {

  private val navController by lazy { findNavController() }

  private val data by lazy { requireArguments().getParcelable<NfcData>("data") }
  private val maskedSimpleDateFormat = SimpleDateFormat("dd.MM.yyyy")
  private val simpleDateFormat = SimpleDateFormat("yyMMdd")


  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    toolbar.setNavigationOnClickListener { navController.navigateUp() }

    with(data!!) {
      if (techInfos == null) {
        if (userBitmap == null) ivAvatar.hide()
        else userBitmap.run {
          ivAvatar.setImageBitmap(userBitmap)
          ivAvatar.onClick {
            findNavController().navigateSafe(R.id.action_fullInfoFragment_to_photoFragment,
              Bundle().apply { putParcelable("photo", userBitmap) })
          }
        }
        if (signBitmap == null) llSign.hide()
        else signBitmap.run {
          ivSign.setImageBitmap(signBitmap)
          ivSign.onClick {
            findNavController().navigateSafe(R.id.action_fullInfoFragment_to_photoFragment,
              Bundle().apply { putParcelable("photo", signBitmap) })
          }
        }
        tvFirstName.text = firstName
        tvLastName.text = lastName
        tvCountry.text = issuingState
        tvDocumentNumber.text = documentNumber
        try {
          tvDateOfExpiry.text = maskedSimpleDateFormat.format(simpleDateFormat.parse(dateOfExpiration))
          tvDateOfBirth.text = maskedSimpleDateFormat.format(simpleDateFormat.parse(birthDate))
        } catch (e: Exception) {
          tvDateOfExpiry.text = dateOfExpiration
          tvDateOfBirth.text = birthDate
        }
        tvDocumentType.text = documentCode
        if (gender.isNullOrEmpty()) llSex.hide()
        else tvGender.text = gender.toString()
        if (nationality.isNullOrEmpty()) llNationality.hide()
        else tvNationality.text = nationality
        if (dateOfIssue.isNullOrEmpty()) llDateOfIssue.hide()
        else tvDateOfIssue.text = dateOfIssue
        if (birthPlace.isNullOrEmpty()) llBirthPlace.hide()
        else tvBirthPlace.text = birthPlace
        if (residenceAddress.isNullOrEmpty()) llResidenceAddress.hide()
        else tvResidenceAddress.text = residenceAddress

        if (pinfl.isNullOrEmpty()) tvUserPinfl.hide() else tvUserPinfl.text = pinfl
      } else {
        setViewsTechInfos()
      }
      llContainer.views().filter { it.isVisible() }.forEachIndexed { index, view ->
        run {
          view.setBackgroundResource(if (index % 2 == 0) android.R.color.white else R.color.grey_light)
        }
      }
    }
  }

  private fun setViewsTechInfos() {
    llContainer.removeAllViews()
    val padding: Int = requireContext().dpToPx(12)
    data?.techInfos?.asIterable()?.forEachIndexed { index, entry -> run {
      if (entry.value.isNullOrEmpty().not()) {
        val layout = LinearLayout(context).apply {
          this.orientation = LinearLayout.HORIZONTAL
        }
        val tvTitle = TextView(context).apply {
          text = requireContext().getStringResourcesByName(entry.key)
          setPadding((padding*1.6).toInt(), padding, (padding*1.6).toInt(), padding)
          setTextColor(Color.BLACK)
        }
        val tvValue = TextView(context).apply {
          text = entry.value
          setPadding((padding * 1.6).toInt(), padding, (padding * 1.6).toInt(), padding)
          setTextColor(Color.BLACK)
          setTypeface(getTypeface(), Typeface.BOLD)
          gravity = Gravity.RIGHT
        }
        layout.addView(tvTitle)
        layout.addView(tvValue, LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 1F))
        llContainer.addView(layout)
      }
    } }
  }
}