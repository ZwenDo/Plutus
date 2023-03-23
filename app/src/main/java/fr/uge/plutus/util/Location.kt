package fr.uge.plutus.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.content.ContextCompat
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.OnTokenCanceledListener
import fr.uge.plutus.MainActivity

fun getLocation(context: Context, onError: (String) -> Unit, onSuccess: (Location) -> Unit) {
    if (!isAccessGranted(context)) {
        MainActivity.requestLocationPermission()
    } else {
        getLocation(onError, onSuccess)
    }
}

private fun isAccessGranted(context: Context): Boolean {
    return ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_COARSE_LOCATION,
    ) == PackageManager.PERMISSION_GRANTED
}

private fun getLocation(onError: (String) -> Unit, onSuccess: (Location) -> Unit) {
    try {
        MainActivity.locationProvider()
            .getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, object : CancellationToken() {
                override fun onCanceledRequested(listener: OnTokenCanceledListener) =
                    CancellationTokenSource().token

                override fun isCancellationRequested() = false
            }).addOnSuccessListener { location: Location? ->
                if (location != null) {
                    onSuccess(location)
                } else {
                    onError("Unknown location")
                }
            }
    } catch (e: SecurityException) {
        onError("Permission not granted")
    }
}
