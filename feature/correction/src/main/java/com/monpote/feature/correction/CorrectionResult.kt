package com.monpote.feature.correction

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CorrectionResult(
    val errors: List<CorrectionError>,
)

@JsonClass(generateAdapter = true)
data class CorrectionError(
    val type: String,
    val original: String,
    val correction: String,
    val explanation: String,
)
