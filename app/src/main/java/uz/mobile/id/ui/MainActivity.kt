package uz.mobile.id.ui

import android.app.Activity
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.nfc.NfcAdapter
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.mercuriete.mrz.reader.CaptureActivity
import com.mercuriete.mrz.reader.MrzData
import com.yariksoffice.lingver.Lingver
import uz.mobile.id.R
import uz.mobile.id.R.layout
import uz.mobile.id.utils.ext.navigateSafe
import java.util.Locale

class MainActivity : AppCompatActivity(layout.activity_main) {

  private val vm by lazy { ViewModelProvider(this).get(MainActivityViewModel::class.java) }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    Lingver.getInstance().setLocale(this, Locale.getDefault())
    FirebaseAnalytics.getInstance(this)
  }

  override fun onResume() {
    super.onResume()
    val adapter = NfcAdapter.getDefaultAdapter(this)
    if (adapter != null) {
      val intent = Intent(getApplicationContext(), this.javaClass)
      intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
      val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
      val filter =
        arrayOf(arrayOf("android.nfc.tech.IsoDep"))
      adapter.enableForegroundDispatch(this, pendingIntent, null, filter)
    }
  }

  override fun onPause() {
    super.onPause()
    val adapter = NfcAdapter.getDefaultAdapter(this)
    adapter?.disableForegroundDispatch(this)
  }

  override fun onNewIntent(intent: Intent?) {
    super.onNewIntent(intent)
    if (NfcAdapter.ACTION_TECH_DISCOVERED == intent?.action) {
      vm.nfcIntent.value = intent
    }
  }

  override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
      Handler().postDelayed({
        CaptureActivity.start(this, requestCode)
      }, 100)
    }
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    if((requestCode == MrzData.PASSPORT || requestCode == MrzData.ID_CARD)
      && resultCode == Activity.RESULT_OK && data != null) {
      if(data.getBooleanExtra("manual_enter", false)) {
        findNavController(R.id.nav_host_container)
          .navigateSafe(R.id.action_homeFragment_to_manualEnterInfoFragment,
            bundleOf("documentType" to requestCode))
      } else vm.mrzDataEvent.value = data.getSerializableExtra("data") as MrzData
    }
  }
}