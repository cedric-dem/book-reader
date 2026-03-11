package com.fluffycactus.wordperwordreader.ui

import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import com.fluffycactus.wordperwordreader.R
import com.fluffycactus.wordperwordreader.domain.Config
import com.fluffycactus.wordperwordreader.domain.model.convertSecondsToHMS
import com.fluffycactus.wordperwordreader.domain.model.extractChapterPagesFromEpub
import com.fluffycactus.wordperwordreader.domain.model.formatInt


class ActivityStatistics : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_statistics)


        val backButton = findViewById<Button>(R.id.button_back_to_reader)

        val numberOfPagesTextView = findViewById<TextView>(R.id.text_number_pages)
        val numberOfWordsTextView = findViewById<TextView>(R.id.text_number_words)
        val timeTakenTextView = findViewById<TextView>(R.id.text_time_taken)

        val statisticsTextView = findViewById<TextView>(R.id.text_words_per_page)

        val bookUri = intent.getStringExtra(Config.EXTRA_BOOK_URI)?.let { Uri.parse(it) }

        val chapterPagesWords = bookUri?.let { extractChapterPagesFromEpub(contentResolver, it) }.orEmpty()

        val totalWords = chapterPagesWords.sumOf { it.size }
        val timeTaken = getEstimateOfTime(totalWords)

        numberOfPagesTextView.text = formatInt(chapterPagesWords.size)
        numberOfWordsTextView.text = formatInt(totalWords)
        timeTakenTextView.text = convertSecondsToHMS(timeTaken)

        statisticsTextView.text = getStatisticsText(chapterPagesWords)

        backButton.setOnClickListener {
            finish()
        }
    }

    private fun getEstimateOfTime(totalWords: Int): Int { // returns number of seconds taken to read all those words
        //take number of words, counts 190 words per minute, return number of minutes
        //in future, iterate trough every words, use the computedelay function for exact accurarcy
        return (60 * totalWords / 190).toInt()
    }

    private fun getStatisticsText(chapterPagesWords: List<List<String>>): String {
        if (chapterPagesWords.isEmpty()) {
            return getString(R.string.statistics_empty)
        }

        return chapterPagesWords
            .mapIndexed { index, words ->
                getString(
                    R.string.statistics_page_words,
                    formatInt(index + 1),
                    formatInt(words.size)
                )
            }
            .joinToString(separator = "\n")
    }


}