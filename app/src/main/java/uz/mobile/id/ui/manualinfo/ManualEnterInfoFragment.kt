package uz.mobile.id.ui.manualinfo

import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.os.LocaleList
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
import kotlinx.android.synthetic.main.fragment_manual_enter_info.btnNext
import kotlinx.android.synthetic.main.fragment_manual_enter_info.etDateOfBirth
import kotlinx.android.synthetic.main.fragment_manual_enter_info.etDateOfExpiry
import kotlinx.android.synthetic.main.fragment_manual_enter_info.etNumberDocument
import kotlinx.android.synthetic.main.fragment_manual_enter_info.ivPickerBirthDay
import kotlinx.android.synthetic.main.fragment_manual_enter_info.ivPickerExpiryDay
import kotlinx.android.synthetic.main.fragment_manual_enter_info.toolbar
import kotlinx.android.synthetic.main.fragment_manual_enter_info.tvDateOfBirthError
import kotlinx.android.synthetic.main.fragment_manual_enter_info.tvDateOfExpiryError
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

class ManualEnterInfoFragment : Fragment(R.layout.fragment_manual_enter_info) {

  private val mainViewModel by lazy { ViewModelProvider(requireActivity()).get(MainActivityViewModel::class.java) }
  private val vm by lazy { ViewModelProvider(this).get(ManualEnterInfoViewModel::class.java) }

  private val navController by lazy { findNavController() }

  private val birthDatePickerDialogListner = DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
    vm.birthDateCalendar.set(Calendar.YEAR, year)
    vm.birthDateCalendar.set(Calendar.MONTH, monthOfYear)
    vm.birthDateCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
    etDateOfBirth.setText(vm.maskedSimpleDateFormat.format(vm.birthDateCalendar.time))
  }
  private val expiryDatePickerDialogListner = DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
    vm.expiryDateCalendar.set(Calendar.YEAR, year)
    vm.expiryDateCalendar.set(Calendar.MONTH, monthOfYear)
    vm.expiryDateCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
    etDateOfExpiry.setText(vm.maskedSimpleDateFormat.format(vm.birthDateCalendar.time))
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
    toolbar.title = when(requireArguments().getInt("documentType")){
      MrzData.PASSPORT -> getString(R.string.passport)
      MrzData.ID_CARD -> getString(R.string.id_card)
      else -> getString(R.string.driver_license)
    }

    vm.documentNumberState.observe(viewLifecycleOwner, Observer {})
    vm.dateOfBirthState.observe(viewLifecycleOwner, Observer {
      if (it.first || it.second.length != 10) tvDateOfBirthError.invisible()
      else tvDateOfBirthError.showAnimWithFade()
    })
    vm.dateOfExpiryState.observe(viewLifecycleOwner, Observer {
      if (it.first || it.second.length != 10) tvDateOfExpiryError.invisible()
      else tvDateOfExpiryError.showAnimWithFade()
    })
    vm.nextValidateState.observe(viewLifecycleOwner, Observer {
      btnNext.setButtonEnabled(it)
    })

    etNumberDocument.keyListener = TextKeyListener.getInstance(false, CHARACTERS)
    etNumberDocument.addTextChangedListener(
      MaskedTextChangedListener(
        "[AA] [0000000]", etNumberDocument, vm.documentNumberValueListener
      )
    )
    etDateOfBirth.keyListener = DigitsKeyListener.getInstance("0123456789 .")
    etDateOfBirth.addTextChangedListener(MaskedTextChangedListener(
      "[00].[00].[0000]", etDateOfBirth, vm.dateOfBirthValueListener))

    etDateOfExpiry.keyListener = DigitsKeyListener.getInstance("0123456789 .")
    etDateOfExpiry.addTextChangedListener(MaskedTextChangedListener(
      "[00].[00].[0000]", etDateOfExpiry, vm.dateOfExpiryValueListener))

    btnNext.onClick {
      mainViewModel.mrzDataEvent.value = MrzData(
        arguments?.getInt("documentType") ?: MrzData.PASSPORT,
        vm.documentNumberState.value?.second,
        vm.birthDateCalendar.time,
        vm.expiryDateCalendar.time
      )
      navController.navigateSafe(R.id.action_manualEnterInfoFragment_to_mrzInfoFragment, arguments)
    }

    ivPickerBirthDay.onClick {
      DatePickerDialog(requireContext(), R.style.DatePickerTheme, birthDatePickerDialogListner,
        vm.birthDateCalendar.get(Calendar.YEAR),
        vm.birthDateCalendar.get(Calendar.MONTH),
        vm.birthDateCalendar.get(Calendar.DAY_OF_MONTH)).show()
    }
    ivPickerExpiryDay.onClick {
      DatePickerDialog(requireContext(), R.style.DatePickerTheme, expiryDatePickerDialogListner,
        vm.expiryDateCalendar.get(Calendar.YEAR),
        vm.expiryDateCalendar.get(Calendar.MONTH),
        vm.expiryDateCalendar.get(Calendar.DAY_OF_MONTH)).show()
    }

    etNumberDocument.postDelayed({etNumberDocument?.showKeyBoardImplicit()}, 300)
  }
}