package com.example.fishapp.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*

// ─── LOCAL THEME DEFINITIONS ────────────────────────────────────────────────
private val LocalBrandGreen = Color(0xFF2ECC71)
private val LocalBrandGreenDark = Color(0xFF27AE60)
private val LocalFreshLight = Color(0xFFECFDF5)
private val LocalFreshGreen = Color(0xFF10B981)
private val LocalDangerLight = Color(0xFFFEF2F2)
private val LocalDangerRed = Color(0xFFEF4444)
private val LocalWarningLight = Color(0xFFFFFBEB)
private val LocalWarningAmber = Color(0xFFF59E0B)
private val LocalPendingLight = Color(0xFFF5F3FF)
private val LocalPendingPurple = Color(0xFF8B5CF6)
private val LocalTextPrimary = Color(0xFF2D3436)
private val LocalTextSecondary = Color(0xFF636E72)
private val LocalCardBorder = Color(0xFFE9ECEF)

// ─── 1. Gradient Header ──────────────────────────────────────────────────────
@Composable
fun GradientHeader(
    gradientColors: List<Color> = listOf(LocalBrandGreen, LocalBrandGreenDark),
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.linearGradient(colors = gradientColors))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 24.dp),
            content = content
        )
    }
}

// ─── 2. Aqua Top Bar (FIXED & STRUCTURALLY REWIRED) ──────────────────────────
@Composable
fun AquaTopBar(
    title: String,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null
) {
    GradientHeader {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding() // Ensures the bar layouts sit safely beneath phone system drop-downs
        ) {
            if (onBack != null) {
                IconButton(
                    onClick = onBack, // FIXED: Securely triggers your navigation controller stack pop!
                    modifier = Modifier
                        .size(48.dp) // Standard Android accessibility interaction target size
                        .padding(end = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Navigate Back",
                        tint = Color.White
                    )
                }
            } else {
                // Adds a clean structural indent if there is no back navigation button active
                Spacer(modifier = Modifier.width(8.dp))
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 2.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

// ─── 3. Stat Chip ─────────────────────────────────────────────────────────────
@Composable
fun StatChip(value: String, label: String, valueColor: Color) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        border = BorderStroke(1.dp, LocalCardBorder)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = value, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = valueColor)
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = LocalTextSecondary)
        }
    }
}

// ─── 4. Status Badge ─────────────────────────────────────────────────────────
@Composable
fun StatusBadge(status: com.example.fishapp.model.ReportStatus) {
    val bg = when (status) {
        com.example.fishapp.model.ReportStatus.RESOLVED  -> LocalFreshLight
        com.example.fishapp.model.ReportStatus.URGENT    -> LocalDangerLight
        com.example.fishapp.model.ReportStatus.PENDING   -> LocalPendingLight
        com.example.fishapp.model.ReportStatus.IN_REVIEW -> LocalWarningLight
    }
    val textColor = when (status) {
        com.example.fishapp.model.ReportStatus.RESOLVED  -> LocalFreshGreen
        com.example.fishapp.model.ReportStatus.URGENT    -> LocalDangerRed
        com.example.fishapp.model.ReportStatus.PENDING   -> LocalPendingPurple
        com.example.fishapp.model.ReportStatus.IN_REVIEW -> LocalWarningAmber
    }
    val label = when (status) {
        com.example.fishapp.model.ReportStatus.RESOLVED  -> "Verified"
        com.example.fishapp.model.ReportStatus.URGENT    -> "Urgent"
        com.example.fishapp.model.ReportStatus.PENDING   -> "Pending"
        com.example.fishapp.model.ReportStatus.IN_REVIEW -> "In Review"
    }

    Surface(shape = RoundedCornerShape(20.dp), color = bg) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

// ─── 5. Freshness Badge ──────────────────────────────────────────────────────
@Composable
fun FreshnessBadge(status: com.example.fishapp.model.FreshnessStatus) {
    val bg = when (status) {
        com.example.fishapp.model.FreshnessStatus.FRESH    -> LocalFreshLight
        com.example.fishapp.model.FreshnessStatus.MODERATE -> LocalWarningLight
        com.example.fishapp.model.FreshnessStatus.POOR     -> LocalDangerLight
    }
    val fg = when (status) {
        com.example.fishapp.model.FreshnessStatus.FRESH    -> LocalFreshGreen
        com.example.fishapp.model.FreshnessStatus.MODERATE -> LocalWarningAmber
        com.example.fishapp.model.FreshnessStatus.POOR     -> LocalDangerRed
    }
    Surface(shape = RoundedCornerShape(20.dp), color = bg) {
        Text(
            text = status.label,
            style = MaterialTheme.typography.labelSmall,
            color = fg,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

// ─── 6. Aqua Card ─────────────────────────────────────────────────────────────
@Composable
fun AquaCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(16.dp)
    val cardModifier = modifier
        .clip(shape)
        .background(Color.White)
        .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
        .border(0.5.dp, LocalCardBorder, shape)

    Column(
        modifier = cardModifier.padding(16.dp),
        content = content
    )
}

// ─── 7. Dashed Upload Box ─────────────────────────────────────────────────────
@Composable
fun DashedUploadBox(
    icon: @Composable () -> Unit,
    title: String,
    subtitle: String,
    onCamera: () -> Unit,
    onGallery: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(LocalBrandGreen.copy(alpha = 0.05f))
            .drawBehind {
                val stroke = Stroke(
                    width = 1.5.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
                )
                drawRoundRect(
                    color = LocalBrandGreen.copy(alpha = 0.4f),
                    cornerRadius = CornerRadius(16.dp.toPx()),
                    style = stroke
                )
            }
            .padding(20.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(LocalBrandGreen.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) { icon() }

            Text(text = title, style = MaterialTheme.typography.titleMedium, color = LocalTextPrimary)
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = LocalTextSecondary,
                textAlign = TextAlign.Center
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onCamera,
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = LocalBrandGreen)
                ) { Text("Camera", color = Color.White) }

                OutlinedButton(
                    onClick = onGallery,
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.5.dp, LocalBrandGreen)
                ) { Text("Gallery", color = LocalBrandGreen) }
            }
        }
    }
}

// ─── 8. Section Label ─────────────────────────────────────────────────────────
@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = LocalTextPrimary,
        fontWeight = FontWeight.Bold,
        modifier = modifier
    )
}