package uz.mobile.id.ui.manualinfo

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.redmadrobot.inputmask.MaskedTextChangedListener.ValueListener
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.regex.Pattern

class ManualEnterInfoViewModel : ViewModel() {

  val documentNumberState = MutableLiveData<Pair<Boolean, String>>()
  val dateOfBirthState = MutableLiveData<Pair<Boolean, String>>()
  val dateOfExpiryState = MutableLiveData<Pair<Boolean, String>>()
  val nextValidateState = MutableLiveData<Boolean>()

  val birthDateCalendar = Calendar.getInstance()
  val expiryDateCalendar = Calendar.getInstance()

  val maskedSimpleDateFormat = SimpleDateFormat("dd.MM.yyyy")
  private val simpleDateFormat = SimpleDateFormat("yyMMdd")
  private val datePattern: Pattern =
    Pattern.compile("^(([0-2][0-9])|[3][0-1])[.](([0][0-9])|[1][0-2])[.]([1][9]|[2][0])[0-9][0-9]\$")

  val documentNumberValueListener = object : ValueListener {
    override fun onTextChanged(
      maskFilled: Boolean,
      extractedValue: String,
      formattedValue: String
    ) {
      documentNumberState.value = maskFilled to extractedValue
      checkValidation()
    }
  }

  val dateOfBirthValueListener = object : ValueListener {
    override fun onTextChanged(
      maskFilled: Boolean,
      extractedValue: String,
      formattedValue: String
    ) {
      dateOfBirthState.value = (maskFilled && datePattern.matcher(formattedValue).matches()) to formattedValue
      if (maskFilled) {
        birthDateCalendar.time = maskedSimpleDateFormat.parse(formattedValue)
      }
      checkValidation()
    }
  }

  val dateOfExpiryValueListener = object : ValueListener {
    override fun onTextChanged(
      maskFilled: Boolean,
      extractedValue: String,
      formattedValue: String
    ) {
      dateOfExpiryState.value = (maskFilled && datePattern.matcher(formattedValue).matches()) to formattedValue
      if (maskFilled) {
        expiryDateCalendar.time = maskedSimpleDateFormat.parse(formattedValue)
      }
      checkValidation()
    }
  }

  private fun checkValidation() {
    nextValidateState.value = documentNumberState.value?.first == true
      && dateOfBirthState.value?.first == true
      && dateOfExpiryState.value?.first == true
  }
}