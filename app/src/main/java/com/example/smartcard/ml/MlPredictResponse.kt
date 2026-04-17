package com.example.smartcard.ml

data class DetectionDto(
    val `class`: String? = null,
    val confidence: Double? = null,
    val bbox: List<Double>? = null,
    val yolo_class: String? = null,
    val yolo_confidence: Double? = null
)

data class MlPredictResponse(
    val detected_products: List<String>? = null,
    val detected_product: String? = null,
    val confidence: Double? = null,
    val detections: List<DetectionDto>? = null,
    val status: String? = null
)