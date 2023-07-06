package uz.mobile.id.utils.ext

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.DisplayMetrics
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import java.lang.Exception

fun Context.makeToast(message: String) {
    if (this is Activity)
        runOnUiThread { Toast.makeText(this, message, Toast.LENGTH_SHORT).show() }
}

fun Context.makeToast(@StringRes messageId: Int) {
    makeToast(getString(messageId))
}

fun Activity.alert(init: AlertDialog.Builder.() -> Unit): AlertDialog {
    val builder = androidx.appcompat.app.AlertDialog.Builder(this)
    builder.setCancelable(false)
    builder.init()
    return builder.create()
}

fun Activity.showAlert(title: Int, init: AlertDialog.Builder.() -> Unit) = alert {
    setTitle(title)
    init()
}.show()

fun Activity.showAlertMessage(title: String = "", init: AlertDialog.Builder.() -> Unit) = alert {
    setMessage(title)
    init()
}.show()

fun AppCompatActivity.makeToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

fun AppCompatActivity.makeToast(@StringRes messageId: Int) {
    makeToast(getString(messageId))
}

fun Fragment.alert(init: AlertDialog.Builder.() -> Unit) = activity?.alert(init)

fun Context.dpToPx(dp: Int): Int {
    val displayMetrics = resources.displayMetrics
    return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT))
}

fun Context.hideKeyboard(view: View) {
    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(view.windowToken, 0)
}

fun Context.toggleKeyboard(view: View) {
    val inputManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager;
    inputManager.toggleSoftInputFromWindow(
        view.applicationWindowToken,
        InputMethodManager.SHOW_FORCED,
        0
    );
}

fun Context.showKeyboard(view: View) {
    val inputManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager;
    inputManager.showSoftInput(
        view,
        InputMethodManager.SHOW_IMPLICIT
    )
}

fun Fragment.getFragment(fragmentTag: String): Fragment? {
    val c = Class.forName(fragmentTag)
    return c.newInstance() as Fragment?
}

fun Fragment.getStatusBarHeight(): Int {
    var statusBarHeight = 0
    val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
    if (resourceId > 0) {
        statusBarHeight = resources.getDimensionPixelSize(resourceId)
    }
    return statusBarHeight
}

fun Context.getImageByName(name: String): Drawable? {
    return ContextCompat.getDrawable(this,
        this.resources.getIdentifier(name, "drawable", this.packageName))
}

fun Context.callTo(number: String?) {
    if (number.isNullOrEmpty().not())
    startActivity(Intent(Intent.ACTION_DIAL,
        Uri.fromParts("tel", number?.split(",")?.first(), null)))
}

fun Context.getStringResourcesByName(name: String) : String {
    val packageName = packageName
    val resId = resources.getIdentifier(name, "string", packageName)
    try {
        return getString(resId)
    } catch (e: Exception) {
        return "" + name;
    }
}