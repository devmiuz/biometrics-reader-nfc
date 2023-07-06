package uz.mobile.id.utils

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

const val REQUEST_CAMERA = 11

fun hasCameraPermission(activity: Activity, requestCode: Int): Boolean {
  if (ContextCompat.checkSelfPermission(
      activity,
      Manifest.permission.CAMERA
    ) != PackageManager.PERMISSION_GRANTED
  ) {
    ActivityCompat.requestPermissions(activity,
      arrayOf(Manifest.permission.CAMERA),
      requestCode)
    return false
  }
  return true
}
