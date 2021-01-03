package com.healthtrack.app.data.models

enum class Trigger(
    val displayName: String,
    val confirmName: String
) {

    WAKE_UP("I wake up", "wake up"),
    BREAKFAST("I eat breakfast", "breakfast"),
    LUNCH("I eat lunch", "lunch"),
    DINNER("I eat dinner", "dinner"),
    MEAL("I eat any meal", "meal"),
    BED_TIME("I get ready for bed", "getting ready for bed"),
    INTERVENTION ("I do one of my other interventions", "an unknown intervention");

    companion object {
        val MEALS: Set<Trigger> = setOf(BREAKFAST, LUNCH, DINNER)
        val TRIGGERABLE: List<Trigger> = values().filterNot { it == INTERVENTION }
    }
}
