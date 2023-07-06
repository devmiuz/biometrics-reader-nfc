package uz.mobile.id.utils.ext

import android.os.Bundle
import androidx.annotation.IdRes
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.Navigator
import java.lang.Exception

fun NavController.navigateSafe(
  @IdRes resId: Int,
  args: Bundle? = null,
  navOptions: NavOptions? = null,
  navExtras: Navigator.Extras? = null
) {
  try {
    val action = currentDestination?.getAction(resId) ?: graph.getAction(resId)
    if (action != null && currentDestination?.id != action.destinationId) {
      navigate(resId, args, navOptions, navExtras)
    }
  } catch (e: Exception) {}
}
