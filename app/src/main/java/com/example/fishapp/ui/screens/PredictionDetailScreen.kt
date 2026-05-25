package com.example.fishapp.ui.screens

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.fishapp.ui.components.AquaCard
import com.example.fishapp.ui.components.AquaTopBar
import com.example.fishapp.ui.theme.*
import com.example.fishapp.viewmodel.FishViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun PredictionDetailScreen(
    navController: NavHostController,
    viewModel: FishViewModel
) {
    val context = LocalContext.current
    val item = viewModel.currentDetailItem.value
    val scope = rememberCoroutineScope()
    var isDownloading by remember { mutableStateOf(false) }

    // DYNAMIC BASE ADDRESS HOST CONFIGURATION:
    // Matches your production network config exactly!
    val backendHostAddress = "192.168.0.176:8000"

    Scaffold(
        topBar = {
            AquaTopBar(
                title = "Scan Report Detail",
                subtitle = "Archived AI pipeline calculation nodes",
                onBack = { navController.popBackStack() }
            )
        },
        containerColor = AquaBackground
    ) { paddingValues ->
        if (item == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Report context unavailable.", color = TextSecondary)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // FIXED: Reads the header token directly from your updated RetrofitClient setup
                val authenticatedImagePainter = rememberAsyncImagePainter(
                    model = ImageRequest.Builder(context)
                        .data("http://$backendHostAddress/predictions/${item.id}/image") // Endpoint 5 integration mapping
                        .addHeader("Authorization", com.example.fishapp.api.RetrofitClient.getAuthToken() ?: "") // Attaches active JWT bearer verification
                        .crossfade(true)
                        .build()
                )

                // Styled Box placeholder container during network loading passes
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFE2E8F0)), // Clean slate fallback color fill
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = authenticatedImagePainter,
                        contentDescription = "Historical scanned image object stream",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                AquaCard {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Classification Node Metrics", fontWeight = FontWeight.Bold, color = BrandGreen, fontSize = 16.sp)
                        HorizontalDivider(color = BrandGreen.copy(alpha = 0.1f))

                        DetailRow("Report Reference ID", "#${item.id}")
                        DetailRow("Identified Species Class", item.species)
                        DetailRow("Species Classifier Confidence", item.species_confidence_percent)
                        DetailRow("YOLO Detection Accuracy", item.yolo_confidence_percent)
                        DetailRow("File Name Registry", item.filename)
                        DetailRow("Timestamp Logged", item.created_at.take(19).replace("T", " "))
                    }
                }

                val isHealthy = item.disease_status.contains("HEALTHY", ignoreCase = true) || item.disease_status.contains("FRESH", ignoreCase = true)
                val accentColor = if (isHealthy) BrandGreen else Color.Red

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(accentColor.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("DISEASE ANALYSIS EVALUATION VERDICT", fontSize = 11.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(item.disease_status.uppercase(), fontSize = 22.sp, color = accentColor, fontWeight = FontWeight.Black)
                        Text("Confidence Score Accuracy: ${item.disease_confidence_percent}", fontSize = 12.sp, color = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Downloader Button Intersecting Endpoint 5 Binary Streams
                Button(
                    onClick = {
                        isDownloading = true
                        scope.launch(Dispatchers.IO) {
                            try {
                                val response = com.example.fishapp.api.RetrofitClient.instance.getPredictionImage(item.id, "")
                                if (response.isSuccessful && response.body() != null) {
                                    val byteStream = response.body()!!.byteStream()
                                    val bitmap = BitmapFactory.decodeStream(byteStream)

                                    if (bitmap != null) {
                                        saveImageToGallery(context, bitmap, "AquaSense_Scan_${item.id}")
                                        withContext(Dispatchers.Main) {
                                            Toast.makeText(context, "Image saved to Gallery successfully!", Toast.LENGTH_LONG).show()
                                        }
                                    } else {
                                        withContext(Dispatchers.Main) {
                                            Toast.makeText(context, "Failed to compile image stream format.", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                } else {
                                    withContext(Dispatchers.Main) {
                                        Toast.makeText(context, "Server error downloading image (Code: ${response.code()})", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            } catch (e: Exception) {
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(context, "Download failed: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                                }
                            } finally {
                                isDownloading = false
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandGreen),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isDownloading
                ) {
                    if (isDownloading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Download, contentDescription = "Download Handle")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Download Fish Scan Image", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Android Native Scoped Storage File Writer Engine
 */
private fun saveImageToGallery(context: Context, bitmap: Bitmap, title: String) {
    val filename = "$title.jpg"

    val contentValues = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
        put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/AquaSense Scans")
            put(MediaStore.MediaColumns.IS_PENDING, 1)
        }
    }

    val itemUri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

    try {
        if (itemUri != null) {
            context.contentResolver.openOutputStream(itemUri)?.use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.clear()
                contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                context.contentResolver.update(itemUri, contentValues, null, null)
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = TextSecondary, fontSize = 14.sp)
        Text(value, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
    }
}