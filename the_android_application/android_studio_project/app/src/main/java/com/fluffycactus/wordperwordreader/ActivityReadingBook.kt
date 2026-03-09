package com.fluffycactus.wordperwordreader

import android.os.Bundle
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge

class ActivityReadingBook : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_reading_book)

        val previewText = intent.getStringExtra(EXTRA_PREVIEW_TEXT) ?: getString(R.string.no_preview_available)
        findViewById<TextView>(R.id.text_book_content).text = previewText
    }

    companion object {
        const val EXTRA_PREVIEW_TEXT = "extra_preview_text"
    }
}