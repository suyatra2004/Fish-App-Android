package com.example.fishapp.model

import com.example.fishapp.api.ClassProbability

data class PredictionHistoryItem(
    val id: Int,
    val filename: String,
    val species: String,
    val species_confidence: Double,
    val species_confidence_percent: String,
    val yolo_confidence: Double,
    val yolo_confidence_percent: String,
    val is_valid_detection: Boolean,
    val all_class_probabilities: List<ClassProbability>,
    val disease_status: String,
    val disease_confidence: Double,
    val disease_confidence_percent: String,
    val message: String,
    val detection_count: Int,
    val created_at: String,
    val image_url: String
)