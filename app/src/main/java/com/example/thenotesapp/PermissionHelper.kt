package com.example.thenotesapp

import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class PermissionHelper(private val activity: AppCompatActivity) {

    private val requestPermissionLauncher =
        activity.registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                onPermissionGranted?.invoke()
            } else {
                onPermissionDenied?.invoke()
            }
        }

    var onPermissionGranted: (() -> Unit)? = null
    var onPermissionDenied: (() -> Unit)? = null

    fun checkAndRequestPermission(permission: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED -> {
                    onPermissionGranted?.invoke()
                }
                else -> {
                    requestPermissionLauncher.launch(permission)
                }
            }
        } else {
            onPermissionGranted?.invoke()
        }
    }
}
