package com.example.fishapp.model

enum class ReportStatus {
    RESOLVED, URGENT, PENDING, IN_REVIEW
}

enum class FreshnessStatus(val label: String) {
    FRESH("Fresh"),
    MODERATE("Moderate"),
    POOR("Poor")
}

enum class ReportType(val label: String) {
    POND_VERIFICATION("Pond Verification"),
    DISEASE_REPORT("Disease Report"),
    SCHEME_APPLICATION("Scheme Application")
}