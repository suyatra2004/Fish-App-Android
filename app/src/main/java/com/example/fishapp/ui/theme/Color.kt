package com.example.fishapp.ui.theme

import androidx.compose.ui.graphics.Color

// --- New Brand Palette (Spring Green Theme) ---
val BrandGreen = Color(0xFF2ECC71)      // The bright green from your image
val BrandGreenDark = Color(0xFF27AE60)  // For gradients and depth
val BrandGreenLight = Color(0xFFDAF7E8) // For subtle highlights

// --- Neutral Backgrounds ---
val AquaBackground = Color(0xFFF8FAFB)  // Almost white, very clean
val SurfaceWhite = Color(0xFFFFFFFF)
val CardBorder = Color(0xFFE9ECEF)

// --- Text Colors (Premium Grays) ---
val TextPrimary = Color(0xFF2D3436)    // Dark gray for readability
val TextSecondary = Color(0xFF636E72)  // Soft gray for hints/subtitles
val TextOnGradient = Color(0xFFFFFFFF) // Pure white for buttons on green

// --- Status Colors (Kept for Fish Freshness logic) ---
val FreshGreen = Color(0xFF10B981)
val DangerRed = Color(0xFFEF4444)
val WarningAmber = Color(0xFFF59E0B)

// --- Navigation/Primary Logic ---
// We alias 'Primary' to 'BrandGreen' so your existing buttons don't break
val Primary = BrandGreen