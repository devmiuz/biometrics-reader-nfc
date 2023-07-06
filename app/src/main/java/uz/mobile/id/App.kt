package uz.mobile.id

import androidx.multidex.MultiDexApplication
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.yariksoffice.lingver.Lingver
import org.spongycastle.jce.provider.BouncyCastleProvider
import uz.mobile.id.utils.PreferenceUtil
import java.security.Security

class App : MultiDexApplication() {

  override fun onCreate() {
    super.onCreate()
    FirebaseApp.initializeApp(this)
    Lingver.init(this, PreferenceUtil.getInstance(this).getLanguage())
    Security.insertProviderAt(BouncyCastleProvider(), 1)
  }
}