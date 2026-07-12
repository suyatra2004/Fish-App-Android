package com.example.fishapp.ui.screens

import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.ViewGroup
import android.widget.Toast
import androidx.camera.core.CameraControl
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.fishapp.ui.theme.BrandGreen
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CameraScannerScreen(
    onClose: () -> Unit,
    onImageCaptured: (Uri) -> Unit = {}
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current

    // Flash control and alert popup state trackers
    var isFlashOn by remember { mutableStateOf(false) }
    var showGuidelinesDialog by remember { mutableStateOf(true) }

    // Initialize CameraX persistent core components inside stable state references
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val imageCapture = remember {
        ImageCapture.Builder()
            .setFlashMode(ImageCapture.FLASH_MODE_OFF)
            .build()
    }
    var cameraControl by remember { mutableStateOf<CameraControl?>(null) }

    // Synchronize user interaction changes with physical flash state parameters
    LaunchedEffect(isFlashOn) {
        imageCapture.flashMode = if (isFlashOn) ImageCapture.FLASH_MODE_ON else ImageCapture.FLASH_MODE_OFF
    }

    // --- POPUP SCANNING GUIDELINES ALERT DIALOG ---
    if (showGuidelinesDialog) {
        AlertDialog(
            onDismissRequest = { showGuidelinesDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = BrandGreen,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = "Optimal Scanning Guidelines",
                    color = Color.Black, // FIXED: High-contrast visibility toggle
                    fontWeight = FontWeight.Bold,
                    fontSize = 19.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "To ensure maximum classification accuracy from the AI model pipeline, keep these instructions in mind:",
                        color = Color.DarkGray, // FIXED: Clear readability rule configuration
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    GuidelineRow("🐟 Focus Entire Body", "Frame the complete lateral side of the specimen uniformly inside the camera scene view.")
                    GuidelineRow("✂️ Ignore Fins & Tails", "Focus primarily on the core scales and gill area—avoid covering these with fingers or packaging equipment.")
                    GuidelineRow("💡 Control Reflections", "Avoid capturing hard surface glares or deep shadows across the body of the fish.")
                }
            },
            shape = RoundedCornerShape(16.dp),
            containerColor = Color.White, // FIXED: Hardforces white layout backgrounds to combat device dark theme conflicts
            confirmButton = {
                Button(
                    onClick = { showGuidelinesDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandGreen),
                    modifier = Modifier.fillMaxWidth().height(46.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Got It, Let's Scan", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        )
    }

    // --- MAIN SCREEN CONTENT LAYOUT ---
    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {

        // 1. Camera Viewport Surface Provider Frame
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx).apply {
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }

                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }

                    try {
                        cameraProvider.unbindAll()
                        val camera = cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            CameraSelector.DEFAULT_BACK_CAMERA,
                            preview,
                            imageCapture
                        )
                        cameraControl = camera.cameraControl
                    } catch (exc: Exception) {
                        Log.e("CameraX", "Use case binding failed", exc)
                    }
                }, ContextCompat.getMainExecutor(ctx))

                previewView
            },
            modifier = Modifier.fillMaxSize()
        )

        // 2. Fixed Overlay Container Mapping Layer Interactions Correctly
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {

            // Top Action Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Dismiss Action Button Frame
                IconButton(
                    onClick = onClose,
                    modifier = Modifier.background(Color.Black.copy(alpha = 0.4f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close Camera Scanner",
                        tint = Color.White
                    )
                }

                // Hardware Flash Toggle Controller Frame
                IconButton(
                    onClick = { isFlashOn = !isFlashOn },
                    modifier = Modifier.background(Color.Black.copy(alpha = 0.4f), CircleShape)
                ) {
                    Icon(
                        imageVector = if (isFlashOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                        contentDescription = "Toggle Camera Device Flash",
                        tint = if (isFlashOn) Color.Yellow else Color.White
                    )
                }
            }

            // Centralized Capture Shutter Button Action Map
            Surface(
                onClick = {
                    val name = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
                    val photoFile = File(context.cacheDir, "AquaSense_Scan_$name.jpg")

                    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

                    imageCapture.takePicture(
                        outputOptions,
                        ContextCompat.getMainExecutor(context),
                        object : ImageCapture.OnImageSavedCallback {
                            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                                val savedUri = Uri.fromFile(photoFile)
                                onImageCaptured(savedUri)
                                onClose()
                            }

                            override fun onError(exception: ImageCaptureException) {
                                Toast.makeText(context, "Shutter failed: ${exception.localizedMessage}", Toast.LENGTH_SHORT).show()
                                Log.e("CameraX", "Failed to preserve frame stream binary map to storage files.", exception)
                            }
                        }
                    )
                },
                modifier = Modifier.size(82.dp).padding(bottom = 12.dp),
                shape = CircleShape,
                color = Color.White,
                border = BorderStroke(6.dp, Color.White.copy(alpha = 0.4f))
            ) {
                // Empty center placeholder creating shutter styling appearance
            }
        }
    }
}

@Composable
private fun GuidelineRow(label: String, detailedInstruction: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text("•", color = BrandGreen, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Column {
            // FIXED: Replaced customizable system text style constants with strict hardcoded dark contrast values
            Text(text = label, fontWeight = FontWeight.Bold, color = Color.Black, fontSize = 13.sp)
            Text(text = detailedInstruction, color = Color.DarkGray, fontSize = 12.sp, lineHeight = 16.sp)
        }
    }
}