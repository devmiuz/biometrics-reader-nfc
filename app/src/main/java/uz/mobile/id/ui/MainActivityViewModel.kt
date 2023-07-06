package uz.mobile.id.ui

import android.content.Intent
import androidx.lifecycle.ViewModel
import com.mercuriete.mrz.reader.MrzData
import uz.mobile.id.utils.SingleLiveEvent

class MainActivityViewModel: ViewModel() {
  val mrzDataEvent = SingleLiveEvent<MrzData>()
  val nfcIntent = SingleLiveEvent<Intent>()
  val languageChangeEvent = SingleLiveEvent<Unit>()
}