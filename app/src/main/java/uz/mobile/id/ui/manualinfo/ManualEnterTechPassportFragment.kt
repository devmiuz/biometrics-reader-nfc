package uz.mobile.id.ui.manualinfo

import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.text.method.DigitsKeyListener
import android.text.method.TextKeyListener
import android.text.method.TextKeyListener.Capitalize.CHARACTERS
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.mercuriete.mrz.reader.MrzData
import com.redmadrobot.inputmask.MaskedTextChangedListener
import com.yariksoffice.lingver.Lingver
import kotlinx.android.synthetic.main.fragment_manual_enter_tech_passport.btnNext
import kotlinx.android.synthetic.main.fragment_manual_enter_tech_passport.etCarNumber
import kotlinx.android.synthetic.main.fragment_manual_enter_tech_passport.etDateOfGiven
import kotlinx.android.synthetic.main.fragment_manual_enter_tech_passport.etLicenseNumber
import kotlinx.android.synthetic.main.fragment_manual_enter_tech_passport.ivPickerGivenDay
import kotlinx.android.synthetic.main.fragment_manual_enter_tech_passport.toolbar
import kotlinx.android.synthetic.main.fragment_manual_enter_tech_passport.tvDateOfBirthError
import uz.mobile.id.R
import uz.mobile.id.ui.MainActivityViewModel
import uz.mobile.id.utils.ext.hideKeyboard
import uz.mobile.id.utils.ext.invisible
import uz.mobile.id.utils.ext.navigateSafe
import uz.mobile.id.utils.ext.onClick
import uz.mobile.id.utils.ext.showAnimWithFade
import uz.mobile.id.utils.ext.showKeyBoardImplicit
import java.util.Calendar
import java.util.Locale

class ManualEnterTechPassportFragment: Fragment(R.layout.fragment_manual_enter_tech_passport) {
  private val navController by lazy { findNavController() }

  private val mainViewModel by lazy { ViewModelProvider(requireActivity()).get(MainActivityViewModel::class.java) }
  private val vm by lazy { ViewModelProvider(this).get(ManualEnterTechPassportViewModel::class.java) }
  private val givenDatePickerDialogListner = DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
    vm.givenDateCalendar.set(Calendar.YEAR, year)
    vm.givenDateCalendar.set(Calendar.MONTH, monthOfYear)
    vm.givenDateCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
    etDateOfGiven.setText(vm.maskedSimpleDateFormat.format(vm.givenDateCalendar.time))
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    Lingver.getInstance().setLocale(context, Locale.getDefault())
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    toolbar.setNavigationOnClickListener {
      context?.hideKeyboard(it)
      navController.navigateUp()
    }

    vm.carNumberState.observe(viewLifecycleOwner, Observer {})
    vm.dateOfGivenState.observe(viewLifecycleOwner, Observer {
      if (it.first || it.second.length != 10) tvDateOfBirthError.invisible()
      else tvDateOfBirthError.showAnimWithFade()
    })
    vm.licenseNumberState.observe(viewLifecycleOwner, Observer {})
    vm.nextValidateState.observe(viewLifecycleOwner, Observer {
      btnNext.setButtonEnabled(it)
    })

    etCarNumber.keyListener = TextKeyListener.getInstance(false, CHARACTERS)
    etCarNumber.addTextChangedListener(
      MaskedTextChangedListener(
        "[00] [------]", etCarNumber, vm.carNumberListener
      )
    )

    etLicenseNumber.keyListener = TextKeyListener.getInstance(false, CHARACTERS)
    etLicenseNumber.addTextChangedListener(
      MaskedTextChangedListener(
        "[AAA] [0000000]", etLicenseNumber, vm.licenseNumberListener
      )
    )

    etDateOfGiven.keyListener = DigitsKeyListener.getInstance("0123456789 .")
    etDateOfGiven.addTextChangedListener(MaskedTextChangedListener(
      "[00].[00].[0000]", etDateOfGiven, vm.dateOfGivenValueListener))
    ivPickerGivenDay.onClick {
      DatePickerDialog(requireContext(), R.style.DatePickerTheme, givenDatePickerDialogListner,
        vm.givenDateCalendar.get(Calendar.YEAR),
        vm.givenDateCalendar.get(Calendar.MONTH),
        vm.givenDateCalendar.get(Calendar.DAY_OF_MONTH)).show()
    }

    btnNext.onClick {
      mainViewModel.mrzDataEvent.value = MrzData(
        MrzData.TECH_PASSPORT,
        vm.carNumberState.value?.second,
        vm.givenDateCalendar.time,
        vm.licenseNumberState.value?.second
      )
      navController.navigateSafe(R.id.action_manualEnterTechPassport_to_mrzInfoFragment, arguments)
    }

    etCarNumber.postDelayed({etCarNumber?.showKeyBoardImplicit()}, 300)
  }
}