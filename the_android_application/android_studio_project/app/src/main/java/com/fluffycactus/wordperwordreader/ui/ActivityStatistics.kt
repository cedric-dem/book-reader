package com.fluffycactus.wordperwordreader.ui

import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import com.fluffycactus.wordperwordreader.R
import com.fluffycactus.wordperwordreader.domain.Config
import com.fluffycactus.wordperwordreader.domain.model.extractChapterPagesFromEpub

class ActivityStatistics : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_statistics)

        val statisticsTextView = findViewById<TextView>(R.id.text_statistics_content)
        val backButton = findViewById<Button>(R.id.button_back_to_reader)

        val bookUri = intent.getStringExtra(Config.EXTRA_BOOK_URI)?.let { Uri.parse(it) }
        val chapterPagesWords = bookUri?.let { extractChapterPagesFromEpub(contentResolver, it) }.orEmpty()

        statisticsTextView.text = getStatisticsText(chapterPagesWords)

        backButton.setOnClickListener {
            finish()
        }
    }

    private fun getStatisticsText(chapterPagesWords: List<List<String>>): String {
        if (chapterPagesWords.isEmpty()) {
            return getString(R.string.statistics_empty)
        }

        return chapterPagesWords
            .mapIndexed { index, words ->
                getString(R.string.statistics_page_words, index + 1, words.size)
            }
            .joinToString(separator = "\n")
    }
}