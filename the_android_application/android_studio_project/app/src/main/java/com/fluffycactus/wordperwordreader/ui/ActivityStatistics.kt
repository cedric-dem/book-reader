package com.fluffycactus.wordperwordreader.ui

import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import com.fluffycactus.wordperwordreader.R
import com.fluffycactus.wordperwordreader.domain.Config
import java.io.InputStream
import java.util.zip.ZipInputStream

class ActivityStatistics : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_statistics)

        val statisticsTextView = findViewById<TextView>(R.id.text_statistics_content)
        val backButton = findViewById<Button>(R.id.button_back_to_reader)

        val bookUri = intent.getStringExtra(Config.EXTRA_BOOK_URI)?.let { Uri.parse(it) }
        val chapterPagesWords = bookUri?.let { extractChapterPagesFromEpub(it) }.orEmpty()

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

    private fun extractChapterPagesFromEpub(uri: Uri): List<List<String>> {
        return try {
            contentResolver.openInputStream(uri)?.use { inputStream ->
                extractWordsByChapterPageFromStream(inputStream)
            } ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun extractWordsByChapterPageFromStream(inputStream: InputStream): List<List<String>> {
        val chapterPagesWords = mutableListOf<List<String>>()

        ZipInputStream(inputStream).use { zip ->
            var entry = zip.nextEntry
            while (entry != null) {
                val entryName = entry.name.lowercase()
                if (!entry.isDirectory && (entryName.endsWith(".xhtml") || entryName.endsWith(".html") || entryName.endsWith(".htm"))) {
                    val rawText = zip.readBytes().toString(Charsets.UTF_8)
                    val cleanedText = rawText
                        .replace(Regex("<[^>]+>"), " ")
                        .replace(Regex("&(rsquo|lsquo|apos);", RegexOption.IGNORE_CASE), "'")
                        .replace(Regex("&#39;|&#x27;", RegexOption.IGNORE_CASE), "'")
                        .replace(Regex("&[a-zA-Z#0-9]+;"), " ")

                    val words = cleanedText
                        .split(Regex("\\s+"))
                        .filter { it.isNotBlank() }

                    if (words.isNotEmpty()) {
                        chapterPagesWords.add(words)
                    }
                }
                zip.closeEntry()
                entry = zip.nextEntry
            }
        }

        return chapterPagesWords
    }
}