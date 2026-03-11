package com.fluffycactus.wordperwordreader.domain.model

fun convertSecondsToHMS(seconds: Int): String {
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val secs = seconds % 60

    return String.format("%dh %02dmin %02dsec", hours, minutes, secs)
}

fun formatInt(n: Int): String {
    return "%,d".format(n).replace(',', ' ')
}
