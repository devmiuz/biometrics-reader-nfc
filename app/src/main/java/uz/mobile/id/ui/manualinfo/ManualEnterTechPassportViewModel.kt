package uz.mobile.id.ui.manualinfo

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.redmadrobot.inputmask.MaskedTextChangedListener.ValueListener
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.regex.Pattern

class ManualEnterTechPassportViewModel : ViewModel() {

  val carNumberState = MutableLiveData<Pair<Boolean, String>>()
  val dateOfGivenState = MutableLiveData<Pair<Boolean, String>>()
  val licenseNumberState = MutableLiveData<Pair<Boolean, String>>()
  val nextValidateState = MutableLiveData<Boolean>()
  val givenDateCalendar = Calendar.getInstance()

  val maskedSimpleDateFormat = SimpleDateFormat("dd.MM.yyyy")
  private val simpleDateFormat = SimpleDateFormat("yyyyMMdd")
  private val datePattern: Pattern =
    Pattern.compile("^(([0-2][0-9])|[3][0-1])[.](([0][0-9])|[1][0-2])[.]([1][9]|[2][0])[0-9][0-9]\$")

  val carNumberListener = object : ValueListener {
    override fun onTextChanged(
      maskFilled: Boolean,
      extractedValue: String,
      formattedValue: String
    ) {
      carNumberState.value = maskFilled to extractedValue
      checkValidation()
    }
  }

  val dateOfGivenValueListener = object : ValueListener {
    override fun onTextChanged(
      maskFilled: Boolean,
      extractedValue: String,
      formattedValue: String
    ) {
      if (maskFilled) {
        givenDateCalendar.time = maskedSimpleDateFormat.parse(formattedValue)
      }
      dateOfGivenState.value = (maskFilled && datePattern.matcher(formattedValue).matches()) to formattedValue
      checkValidation()
    }
  }

  val licenseNumberListener = object : ValueListener {
    override fun onTextChanged(
      maskFilled: Boolean,
      extractedValue: String,
      formattedValue: String
    ) {
      licenseNumberState.value = maskFilled to extractedValue
      checkValidation()
    }
  }

  private fun checkValidation() {
    nextValidateState.value = carNumberState.value?.first == true
      && dateOfGivenState.value?.first == true
      && licenseNumberState.value?.first == true
  }

  fun getGivenDateExtractValue() = simpleDateFormat.format(maskedSimpleDateFormat.parse(dateOfGivenState.value?.second))

}