package com.example.fishapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.example.fishapp.ui.theme.FishAppTheme
import com.example.fishapp.navigation.AquaSenseNavGraph

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FishAppTheme {
                // Check permissions every time the Composition starts
                LocationPermissionHandler()

                AquaSenseNavGraph()
            }
        }
    }
}

@Composable
fun LocationPermissionHandler() {
    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false)
        val cameraGranted = permissions.getOrDefault(Manifest.permission.CAMERA, false)

        if (!fineGranted || !cameraGranted) {
            Toast.makeText(context, "Permissions are required for full app features.", Toast.LENGTH_LONG).show()
        }
    }

    // Use LaunchedEffect with a key that changes or Unit to trigger on every fresh launch
    LaunchedEffect(Unit) {
        val permissionsToRequest = mutableListOf<String>()

        // Check Location
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
            permissionsToRequest.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        }

        // Check Camera (Since you mentioned it earlier)
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.CAMERA)
        }

        if (permissionsToRequest.isNotEmpty()) {
            launcher.launch(permissionsToRequest.toTypedArray())
        }
    }
}