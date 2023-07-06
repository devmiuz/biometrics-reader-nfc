package uz.mobile.id.ui.fullinfo

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat.PNG
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.dialog_photo.ivAvatar
import kotlinx.android.synthetic.main.dialog_photo.ivShare
import uz.mobile.id.R
import uz.mobile.id.utils.ext.onClick
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class PhotoFragment : Fragment(R.layout.dialog_photo) {

  private val photo by lazy { arguments?.getParcelable("photo") as? Bitmap }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    photo?.let {
      ivAvatar.setImageBitmap(it)
    }
    ivShare.onClick {
      saveImage()

      val imagePath = File(requireContext().cacheDir, "images")
      val newFile = File(imagePath, "image.png")
      val contentUri: Uri = FileProvider.getUriForFile(requireContext(),
        "uz.mobile.id.fileprovider", newFile)

      val shareIntent = Intent()
      shareIntent.action = Intent.ACTION_SEND
      shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) // temp permission for receiving app to read this file
      shareIntent.setDataAndType(contentUri, requireContext().contentResolver.getType(contentUri))
      shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri)
      startActivity(Intent.createChooser(shareIntent, "Choose an app"))
    }
  }

  private fun saveImage() {
    try {
      val cachePath = File(requireContext().cacheDir, "images")
      cachePath.mkdirs() // don't forget to make the directory
      val stream =
        FileOutputStream(cachePath.toString() + "/image.png") // overwrites this image every time
      photo?.compress(PNG, 100, stream)
      stream.close()
    } catch (e: IOException) {
      e.printStackTrace()
    }
  }
}